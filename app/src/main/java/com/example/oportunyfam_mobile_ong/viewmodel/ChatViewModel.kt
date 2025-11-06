package com.example.oportunyfam_mobile_ong.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oportunyfam_mobile_ong.Service.FirebaseMensagemService
import com.example.oportunyfam_mobile_ong.Service.MensagemService
import com.example.oportunyfam_mobile_ong.model.Mensagem
import com.example.oportunyfam_mobile_ong.model.MensagemRequest
import com.example.oportunyfam_mobile_ong.Service.InstituicaoService
import com.example.oportunyfam_mobile_ong.Service.RetrofitFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConversaUI(
    val id: Int,
    val nome: String,
    val ultimaMensagem: String,
    val hora: String,
    val imagem: Int,
    val online: Boolean,
    val mensagensNaoLidas: Int = 0,
    val pessoaId: Int
)

class ChatViewModel(
    private val instituicaoId: Int = 6 // TODO: Pegar do AuthDataStore
) : ViewModel() {
    private val instituicaoService: InstituicaoService = RetrofitFactory().getInstituicaoService()
    private val mensagemService: MensagemService = RetrofitFactory().getMensagemService()
    private val firebaseMensagemService: FirebaseMensagemService = FirebaseMensagemService()

    private val _conversas = MutableStateFlow<List<ConversaUI>>(emptyList())
    val conversas: StateFlow<List<ConversaUI>> = _conversas.asStateFlow()

    private val _mensagens = MutableStateFlow<List<Mensagem>>(emptyList())
    val mensagens: StateFlow<List<Mensagem>> = _mensagens.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Flag para controlar se já carregou conversas
    private var conversasCarregadas = false

    /**
     * Carrega conversas apenas na primeira vez que entra na tela
     */
    fun carregarConversas(forcarRecarregar: Boolean = false) {
        if (conversasCarregadas && !forcarRecarregar) {
            Log.d("ChatViewModel", "Conversas já carregadas, pulando requisição")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    instituicaoService.buscarPorId(instituicaoId).execute()
                }

                if (response.isSuccessful) {
                    val instituicao = response.body()?.instituicao
                    val conversasInstituicao = instituicao?.conversas ?: emptyList()

                    _conversas.value = conversasInstituicao.map { conversa ->
                        ConversaUI(
                            id = conversa.id_conversa,
                            nome = conversa.outro_participante.nome,
                            ultimaMensagem = conversa.ultima_mensagem?.descricao ?: "Sem mensagens",
                            hora = formatarHora(conversa.ultima_mensagem?.data_envio),
                            imagem = com.example.oportunyfam_mobile_ong.R.drawable.perfil,
                            online = false,
                            mensagensNaoLidas = 0,
                            pessoaId = conversa.outro_participante.id
                        )
                    }
                    conversasCarregadas = true
                    Log.d("ChatViewModel", "Conversas carregadas pela primeira vez: ${_conversas.value.size}")
                } else {
                    _errorMessage.value = "Erro ao carregar conversas: ${response.message()}"
                    Log.e("ChatViewModel", "Erro: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao conectar: ${e.message}"
                Log.e("ChatViewModel", "Erro ao carregar conversas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Inicia escuta em tempo real do Firebase para uma conversa
     * Substitui completamente o polling por listener do Firebase
     */
    fun iniciarEscutaMensagens(conversaId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Primeiro, carrega mensagens do backend e sincroniza com Firebase
                val response = mensagemService.listarPorConversa(conversaId)
                if (response.isSuccessful) {
                    val mensagensBackend = response.body()?.mensagens ?: emptyList()
                    firebaseMensagemService.sincronizarMensagens(conversaId, mensagensBackend)
                    Log.d("ChatViewModel", "Mensagens sincronizadas com Firebase: ${mensagensBackend.size}")
                }

                _isLoading.value = false

                // Agora escuta mudanças em tempo real do Firebase
                firebaseMensagemService.observarMensagens(conversaId).collect { mensagensAtualizadas ->
                    _mensagens.value = mensagensAtualizadas
                    Log.d("ChatViewModel", "📱 Mensagens atualizadas EM TEMPO REAL: ${mensagensAtualizadas.size}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao conectar Firebase: ${e.message}"
                Log.e("ChatViewModel", "Erro ao iniciar escuta de mensagens", e)
                _isLoading.value = false
            }
        }
    }

    /**
     * Envia mensagem: primeiro salva no backend, depois no Firebase
     * O Firebase notifica todos os listeners automaticamente
     */
    fun enviarMensagem(conversaId: Int, pessoaId: Int, texto: String) {
        viewModelScope.launch {
            try {
                // 1. Envia para o backend (fonte da verdade)
                val request = MensagemRequest(
                    id_conversa = conversaId,
                    id_pessoa = pessoaId,
                    descricao = texto
                )
                val response = mensagemService.criar(request)

                if (response.isSuccessful) {
                    val mensagemCriada = response.body()?.mensagem

                    if (mensagemCriada != null) {
                        // 2. Envia para o Firebase (notifica em tempo real)
                        firebaseMensagemService.enviarMensagem(mensagemCriada)
                        Log.d("ChatViewModel", "✅ Mensagem enviada: ${mensagemCriada.id}")
                    }
                } else {
                    _errorMessage.value = "Erro ao enviar mensagem"
                    Log.e("ChatViewModel", "Erro: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao enviar: ${e.message}"
                Log.e("ChatViewModel", "Erro ao enviar mensagem", e)
            }
        }
    }

    fun limparErro() {
        _errorMessage.value = null
    }

    private fun formatarHora(dataHora: String?): String {
        if (dataHora == null) return "Agora"
        return try {
            // Formato: "2025-11-06 12:49:09.000000"
            val partes = dataHora.split(" ")
            if (partes.size > 1) {
                val hora = partes[1].substring(0, 5)
                hora
            } else {
                "Agora"
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erro ao formatar hora: $dataHora", e)
            "Agora"
        }
    }
}

