package com.oportunyfam_mobile_ong.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile_ong.Service.FirebaseMensagemService
import com.oportunyfam_mobile_ong.Service.MensagemService
import com.oportunyfam_mobile_ong.Service.ConversaService
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.model.Mensagem
import com.oportunyfam_mobile_ong.model.MensagemRequest
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
    private val conversaService: ConversaService = RetrofitFactory().getConversaService()
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

    // ID da pessoa logada (pessoa_id da instituição)
    private val _pessoaId = MutableStateFlow<Int?>(null)
    val pessoaId: StateFlow<Int?> = _pessoaId.asStateFlow()

    // Flag para controlar se já carregou conversas
    private var conversasCarregadas = false

    init {
        // Carrega o ID da pessoa ao inicializar o ViewModel
        viewModelScope.launch {
            val instituicao = authDataStore.loadInstituicao()
            _pessoaId.value = instituicao?.pessoa_id
            Log.d("ChatViewModel", "Pessoa logada: ID=${instituicao?.pessoa_id}, Nome=${instituicao?.nome}")

            // Carrega conversas do cache offline primeiro (se existir)
            instituicao?.pessoa_id?.let { id ->
                val conversasCache = authDataStore.loadConversasCache(id)
                if (!conversasCache.isNullOrEmpty()) {
                    _conversas.value = conversasCache.map { conversa ->
                        ConversaUI(
                            id = conversa.id_conversa,
                            nome = conversa.outro_participante.nome,
                            ultimaMensagem = conversa.ultima_mensagem?.descricao ?: "Sem mensagens",
                            hora = formatarHora(conversa.ultima_mensagem?.data_envio),
                            imagem = com.oportunyfam_mobile_ong.R.drawable.perfil,
                            online = false,
                            mensagensNaoLidas = 0,
                            pessoaId = conversa.outro_participante.id
                        )
                    }
                    Log.d("ChatViewModel", "✅ Conversas carregadas do CACHE: ${_conversas.value.size}")
                }
            }
        }
    }

    /**
     * Carrega conversas da API usando o endpoint /conversas/pessoa/:id
     * Com cache offline: primeiro carrega do cache, depois atualiza da API
     * Se falhar (sem internet), mantém o cache
     */
    fun carregarConversas(forcarRecarregar: Boolean = false) {
        if (conversasCarregadas && !forcarRecarregar) {
            Log.d("ChatViewModel", "Conversas já carregadas, pulando requisição")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null // Limpa erro anterior

            try {
                // ✅ Aguarda a pessoa estar carregada (pode estar sendo carregado do DataStore)
                var pessoaId = _pessoaId.value

                // Se ainda não carregou, aguarda um pouco (máximo 2 segundos)
                var tentativas = 0
                while (pessoaId == null && tentativas < 20) {
                    kotlinx.coroutines.delay(100) // Aguarda 100ms
                    pessoaId = _pessoaId.value
                    tentativas++
                }

                if (pessoaId == null) {
                    _errorMessage.value = "Erro ao carregar dados do usuário. Faça login novamente."
                    Log.e("ChatViewModel", "❌ Pessoa não carregada após aguardar")
                    _isLoading.value = false
                    return@launch
                }

                Log.d("ChatViewModel", "🔄 Carregando conversas da API para pessoa ID=$pessoaId")

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    conversaService.buscarPorIdPessoa(pessoaId)
                }

                if (response.isSuccessful) {
                    val conversasInstituicao = response.body()?.getConversasList() ?: emptyList()

                    Log.d("ChatViewModel", "📋 API retornou ${conversasInstituicao.size} conversas")

                    // Log detalhado de cada conversa
                    conversasInstituicao.forEachIndexed { index, conversa ->
                        Log.d("ChatViewModel", "  [$index] ID conversa=${conversa.id_conversa}, " +
                                "Pessoa=${conversa.outro_participante.nome} (ID=${conversa.outro_participante.id}), " +
                                "Última msg: ${conversa.ultima_mensagem?.descricao ?: "sem mensagens"}")
                    }

                    // ✅ FILTRAR: Mostrar TODAS as conversas únicas (diferentes pessoas OU diferentes IDs de conversa)
                    // Se duas pessoas têm o mesmo nome, mantém ambas as conversas mais recentes
                    val conversasUnicas = conversasInstituicao
                        .groupBy { it.outro_participante.id } // Agrupa pelo ID da pessoa
                        .flatMap { (pessoaId, conversasGrupo) ->
                            Log.d("ChatViewModel", "  📂 Grupo pessoa_id=$pessoaId: ${conversasGrupo.size} conversa(s)")

                            // Se tem mais de uma conversa com a mesma pessoa, pega só a mais recente
                            if (conversasGrupo.size > 1) {
                                val maisRecente = conversasGrupo.maxByOrNull { conversa ->
                                    conversa.ultima_mensagem?.data_envio ?: "1970-01-01 00:00:00"
                                }
                                Log.d("ChatViewModel", "    ⚠️ ${conversasGrupo.size} duplicatas! Mantendo apenas ID=${maisRecente?.id_conversa}")
                                listOfNotNull(maisRecente)
                            } else {
                                conversasGrupo
                            }
                        }

                    Log.d("ChatViewModel", "🔹 Após remover duplicatas: ${conversasUnicas.size} conversas únicas")
                    conversasUnicas.forEach { conversa ->
                        Log.d("ChatViewModel", "  ✅ ID=${conversa.id_conversa}, ${conversa.outro_participante.nome} (pessoa_id=${conversa.outro_participante.id})")
                    }

                    // Atualiza as conversas na UI (apenas únicas)
                    _conversas.value = conversasUnicas.map { conversa ->
                        ConversaUI(
                            id = conversa.id_conversa,
                            nome = conversa.outro_participante.nome,
                            ultimaMensagem = conversa.ultima_mensagem?.descricao ?: "Sem mensagens",
                            hora = formatarHora(conversa.ultima_mensagem?.data_envio),
                            imagem = com.oportunyfam_mobile_ong.R.drawable.perfil,
                            online = false,
                            mensagensNaoLidas = 0,
                            pessoaId = conversa.outro_participante.id
                        )
                    }

                    // Salva no cache para uso offline (conversas únicas)
                    authDataStore.saveConversasCache(pessoaId, conversasUnicas)

                    conversasCarregadas = true
                    Log.d("ChatViewModel", "✅ Conversas carregadas e filtradas: ${_conversas.value.size}")
                } else {
                    _errorMessage.value = "Erro ao carregar conversas: ${response.message()}"
                    Log.e("ChatViewModel", "❌ Erro API: ${response.errorBody()?.string()}")

                    // Se falhar, mantém o cache existente (já carregado no init)
                    Log.d("ChatViewModel", "📦 Mantendo conversas do cache offline")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Sem conexão. Mostrando conversas salvas."
                Log.e("ChatViewModel", "❌ Erro ao conectar: ${e.message}", e)

                // Se falhar, mantém o cache existente (já carregado no init)
                Log.d("ChatViewModel", "📦 Mantendo conversas do cache offline")
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
                _errorMessage.value = null

                // ✅ SEMPRE carrega mensagens do backend (fonte da verdade)
                Log.d("ChatViewModel", "🔄 Carregando mensagens da API (conversa $conversaId)...")
                Log.d("ChatViewModel", "📍 Endpoint: GET /v1/oportunyfam/conversas/$conversaId/mensagens")

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    mensagemService.listarPorConversa(conversaId)
                }

                Log.d("ChatViewModel", "📡 Resposta API: code=${response.code()}, success=${response.isSuccessful}")

                if (response.isSuccessful) {
                    val mensagensBackend = response.body()?.mensagens ?: emptyList()
                    _mensagens.value = mensagensBackend
                    Log.d("ChatViewModel", "✅ ${mensagensBackend.size} mensagens carregadas do backend")

                    // Log detalhado das mensagens
                    mensagensBackend.forEachIndexed { index, msg ->
                        Log.d("ChatViewModel", "  [$index] ID=${msg.id}, de pessoa=${msg.id_pessoa}, texto='${msg.descricao}'")
                    }

                    // Sincroniza com Firebase em background (não bloqueia a UI)
                    launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            firebaseMensagemService.sincronizarMensagens(conversaId, mensagensBackend)
                            Log.d("ChatViewModel", "📱 Mensagens sincronizadas com Firebase")
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "⚠️ Erro ao sincronizar Firebase (não crítico)", e)
                        }
                    }
                } else if (response.code() == 404) {
                    // ✅ Conversa nova sem mensagens - NÃO é erro!
                    _mensagens.value = emptyList()
                    Log.d("ChatViewModel", "✅ Conversa nova (404). Iniciando sem mensagens.")
                } else {
                    _errorMessage.value = "Erro ao carregar mensagens"
                    val errorBody = response.errorBody()?.string()
                    Log.e("ChatViewModel", "❌ Erro API (${response.code()}): $errorBody")
                }

                _isLoading.value = false

                // Agora escuta mudanças em tempo real do Firebase
                firebaseMensagemService.observarMensagens(conversaId).collect { mensagensFirebase ->
                    if (mensagensFirebase.isNotEmpty()) {
                        // MERGE: Combina mensagens do backend com as novas do Firebase
                        val mensagensExistentes = _mensagens.value
                        val idsExistentes = mensagensExistentes.map { it.id }.toSet()

                        // Adiciona apenas mensagens novas que não existem
                        val mensagensNovas = mensagensFirebase.filter { it.id !in idsExistentes }

                        if (mensagensNovas.isNotEmpty()) {
                            _mensagens.value = mensagensExistentes + mensagensNovas
                            Log.d("ChatViewModel", "📱 ${mensagensNovas.size} mensagem(ns) nova(s) do Firebase. Total: ${_mensagens.value.size}")
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // ✅ Coroutine cancelada por navegação - NÃO é erro
                Log.d("ChatViewModel", "⏹️ Carregamento de mensagens cancelado (navegação)")
                throw e // Re-throw para propagar o cancelamento corretamente
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar mensagens: ${e.message}"
                Log.e("ChatViewModel", "❌ Erro crítico ao carregar mensagens", e)
                _isLoading.value = false
            }
        }
    }

    /**
     * Recarrega mensagens da API (útil para refresh manual)
     */
    fun recarregarMensagens(conversaId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "🔄 Recarregando mensagens da API...")

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    mensagemService.listarPorConversa(conversaId)
                }

                if (response.isSuccessful) {
                    val mensagensBackend = response.body()?.mensagens ?: emptyList()
                    _mensagens.value = mensagensBackend
                    Log.d("ChatViewModel", "✅ ${mensagensBackend.size} mensagens recarregadas")
                } else if (response.code() == 404) {
                    _mensagens.value = emptyList()
                    Log.d("ChatViewModel", "✅ Nenhuma mensagem na conversa")
                } else {
                    Log.e("ChatViewModel", "❌ Erro ao recarregar: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "❌ Erro ao recarregar mensagens", e)
            }
        }
    }

    /**
     * Envia mensagem: primeiro salva no backend, depois no Firebase
     * O Firebase notifica todos os listeners automaticamente
     * Após enviar, atualiza a lista de conversas
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
                        // 2. Adiciona mensagem localmente para aparecer imediatamente
                        val mensagensAtuais = _mensagens.value.toMutableList()
                        mensagensAtuais.add(mensagemCriada)
                        _mensagens.value = mensagensAtuais
                        Log.d("ChatViewModel", "✅ Mensagem enviada e adicionada: ${mensagemCriada.id}")

                        // 3. Envia para o Firebase (notifica em tempo real)
                        firebaseMensagemService.enviarMensagem(mensagemCriada)

                        // 4. Atualiza a lista de conversas em background
                        launch {
                            carregarConversas(forcarRecarregar = true)
                        }
                    }
                } else {
                    _errorMessage.value = "Erro ao enviar mensagem"
                    Log.e("ChatViewModel", "Erro: ${response.errorBody()?.string()}")
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // ✅ Coroutine cancelada - NÃO é erro
                Log.d("ChatViewModel", "⏹️ Envio de mensagem cancelado")
                throw e // Re-throw para propagar o cancelamento
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
