package com.oportunyfam_mobile_ong.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile_ong.Service.FirebaseMensagemService
import com.oportunyfam_mobile_ong.Service.MensagemService
import com.oportunyfam_mobile_ong.model.Mensagem
import com.oportunyfam_mobile_ong.model.MensagemRequest
import com.oportunyfam_mobile_ong.Service.InstituicaoService
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.data.InstituicaoAuthDataStore
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

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val instituicaoService: InstituicaoService = RetrofitFactory().getInstituicaoService()
    private val mensagemService: MensagemService = RetrofitFactory().getMensagemService()
    private val firebaseMensagemService: FirebaseMensagemService = FirebaseMensagemService()
    private val authDataStore = InstituicaoAuthDataStore(application)

    private val _conversas = MutableStateFlow<List<ConversaUI>>(emptyList())
    val conversas: StateFlow<List<ConversaUI>> = _conversas.asStateFlow()

    private val _mensagens = MutableStateFlow<List<Mensagem>>(emptyList())
    val mensagens: StateFlow<List<Mensagem>> = _mensagens.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ID da instituição logada (carregado do AuthDataStore)
    private val _instituicaoId = MutableStateFlow<Int?>(null)
    val instituicaoId: StateFlow<Int?> = _instituicaoId.asStateFlow()

    // Flag para controlar se já carregou conversas
    private var conversasCarregadas = false

    init {
        // Carrega o ID da instituição ao inicializar o ViewModel
        viewModelScope.launch {
            val instituicao = authDataStore.loadInstituicao()
            _instituicaoId.value = instituicao?.instituicao_id
            Log.d("ChatViewModel", "Instituição logada: ID=${instituicao?.instituicao_id}, Nome=${instituicao?.nome}")
        }
    }

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
                val instId = _instituicaoId.value
                if (instId == null) {
                    _errorMessage.value = "Usuário não está logado"
                    Log.e("ChatViewModel", "Instituição não está logada")
                    _isLoading.value = false
                    return@launch
                }

                Log.d("ChatViewModel", "Carregando conversas para instituição ID=$instId")

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    instituicaoService.buscarPorId(instId).execute()
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
                    Log.d("ChatViewModel", "✅ Conversas carregadas: ${_conversas.value.size}")
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
     * Primeiro carrega do backend, depois mantém sincronização em tempo real
     */
    fun iniciarEscutaMensagens(conversaId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Primeiro, carrega mensagens do backend
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    mensagemService.listarPorConversa(conversaId)
                }

                if (response.isSuccessful) {
                    val mensagensBackend = response.body()?.mensagens ?: emptyList()
                    _mensagens.value = mensagensBackend
                    Log.d("ChatViewModel", "Mensagens carregadas do backend: ${mensagensBackend.size}")

                    // Sincroniza com Firebase em background (não bloqueia a UI)
                    launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            firebaseMensagemService.sincronizarMensagens(conversaId, mensagensBackend)
                            Log.d("ChatViewModel", "Mensagens sincronizadas com Firebase: ${mensagensBackend.size}")
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "Erro ao sincronizar mensagens", e)
                        }
                    }
                } else {
                    _errorMessage.value = "Erro ao carregar mensagens"
                    Log.e("ChatViewModel", "Erro: ${response.errorBody()?.string()}")
                }

                _isLoading.value = false

                // Agora escuta mudanças em tempo real do Firebase
                firebaseMensagemService.observarMensagens(conversaId).collect { mensagensAtualizadas ->
                    if (mensagensAtualizadas.isNotEmpty()) {
                        _mensagens.value = mensagensAtualizadas
                        Log.d("ChatViewModel", "📱 Mensagens atualizadas EM TEMPO REAL: ${mensagensAtualizadas.size}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar mensagens: ${e.message}"
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
