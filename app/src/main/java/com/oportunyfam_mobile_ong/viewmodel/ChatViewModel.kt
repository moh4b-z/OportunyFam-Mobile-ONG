package com.oportunyfam_mobile_ong.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile_ong.Service.FirebaseMensagemService
import com.oportunyfam_mobile_ong.Service.MensagemService
import com.oportunyfam_mobile_ong.Service.ConversaService
import com.oportunyfam_mobile_ong.Service.InstituicaoService
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.model.Mensagem
import com.oportunyfam_mobile_ong.model.MensagemRequest
import com.oportunyfam_mobile_ong.model.Aluno
import com.oportunyfam_mobile_ong.model.ConversaRequest
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
    private val instituicaoService: InstituicaoService = RetrofitFactory().getInstituicaoService()
    private val criancaService = RetrofitFactory().getCriancaService()
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

    // ID da instituição logada
    private val _instituicaoId = MutableStateFlow<Int?>(null)
    val instituicaoId: StateFlow<Int?> = _instituicaoId.asStateFlow()

    // Lista de alunos da instituição
    private val _alunos = MutableStateFlow<List<Aluno>>(emptyList())
    val alunos: StateFlow<List<Aluno>> = _alunos.asStateFlow()

    private val _isLoadingAlunos = MutableStateFlow(false)
    val isLoadingAlunos: StateFlow<Boolean> = _isLoadingAlunos.asStateFlow()

    // Flag para controlar se já carregou conversas
    private var conversasCarregadas = false

    init {
        // Carrega o ID da pessoa ao inicializar o ViewModel
        viewModelScope.launch {
            val instituicao = authDataStore.loadInstituicao()
            _pessoaId.value = instituicao?.pessoa_id
            _instituicaoId.value = instituicao?.instituicao_id
            Log.d("ChatViewModel", "Pessoa logada: ID=${instituicao?.pessoa_id}, Nome=${instituicao?.nome}, Instituição ID=${instituicao?.instituicao_id}")

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

    private var firebaseJob: kotlinx.coroutines.Job? = null
    private var firebaseConversaObservadaId: Int? = null

    fun iniciarEscutaMensagens(conversaId: Int) {
        // evita abrir múltiplas escutas para mesma conversa
        if (firebaseConversaObservadaId == conversaId && firebaseJob?.isActive == true) return

        // cancela qualquer escuta anterior
        pararEscutaMensagens()
        firebaseConversaObservadaId = conversaId

        firebaseJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1) Carrega do backend (fonte da verdade)
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    mensagemService.listarPorConversa(conversaId)
                }

                if (response.isSuccessful) {
                    val mensagensBackend = response.body()?.mensagens ?: emptyList()
                    _mensagens.value = mensagensBackend.sortedBy { it.criado_em }
                    // Sincroniza com firebase (sem apagar) em background
                    launch(kotlinx.coroutines.Dispatchers.IO) {
                        firebaseMensagemService.sincronizarMensagens(conversaId, mensagensBackend)
                    }
                }

                _isLoading.value = false

                // 2) Escuta eventos incrementais do Firebase
                firebaseMensagemService.observarMensagensEventos(conversaId).collect { event ->
                    when (event.type) {
                        "added" -> addOrUpdateMensagem(event.mensagem)
                        "changed" -> addOrUpdateMensagem(event.mensagem)
                        "removed" -> removeMensagem(event.mensagem.id)
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Erro ao carregar mensagens: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pararEscutaMensagens() {
        firebaseJob?.cancel()
        firebaseJob = null
        firebaseConversaObservadaId = null
    }

    private fun addOrUpdateMensagem(m: Mensagem) {
        // mantém unicidade por ID e ordena por criado_em
        val map = _mensagens.value.associateBy { it.id }.toMutableMap()
        map[m.id] = m
        _mensagens.value = map.values.sortedBy { it.criado_em }
    }

    private fun removeMensagem(mensagemId: Int) {
        _mensagens.value = _mensagens.value.filterNot { it.id == mensagemId }
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
                        addOrUpdateMensagem(mensagemCriada)
                        Log.d("ChatViewModel", "✅ Mensagem enviada e adicionada localmente: ${mensagemCriada.id}")

                        // 3. Envia para o Firebase (notifica em tempo real)
                        launch(kotlinx.coroutines.Dispatchers.IO) {
                            val result = firebaseMensagemService.enviarMensagem(mensagemCriada)
                            if (result.isFailure) {
                                Log.e("ChatViewModel", "⚠️ Falha ao notificar Firebase: ${result.exceptionOrNull()}")
                                // Não mostra erro para o usuário, apenas loga
                            }
                        }

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

    /**
     * Carrega todos os alunos da instituição usando o endpoint /instituicoes/alunos/
     */
    fun carregarAlunos() {
        viewModelScope.launch {
            _isLoadingAlunos.value = true
            _errorMessage.value = null

            try {
                val instituicaoId = _instituicaoId.value
                if (instituicaoId == null) {
                    _errorMessage.value = "ID da instituição não encontrado"
                    Log.e("ChatViewModel", "❌ Instituição ID não carregado")
                    _isLoadingAlunos.value = false
                    return@launch
                }

                Log.d("ChatViewModel", "🔄 Carregando alunos da instituição ID=$instituicaoId")

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    instituicaoService.buscarAlunos(
                        instituicao_id = instituicaoId,
                        atividade_id = null,
                        status_id = null
                    ).execute()
                }

                if (response.isSuccessful) {
                    val alunosResponse = response.body()?.alunos ?: emptyList()

                    // Agrupa alunos por crianca_id para evitar duplicatas
                    val alunosUnicos = alunosResponse
                        .groupBy { it.crianca_id }
                        .map { (_, alunos) -> alunos.first() }

                    _alunos.value = alunosUnicos
                    Log.d("ChatViewModel", "✅ ${alunosUnicos.size} alunos únicos carregados")
                } else {
                    _errorMessage.value = "Erro ao carregar alunos: ${response.message()}"
                    Log.e("ChatViewModel", "❌ Erro API: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao conectar: ${e.message}"
                Log.e("ChatViewModel", "❌ Erro ao carregar alunos", e)
            } finally {
                _isLoadingAlunos.value = false
            }
        }
    }

    /**
     * Cria ou encontra uma conversa existente com um aluno
     * Retorna o ID da conversa (existente ou nova)
     * @param criancaIdOrPessoaId pode ser tanto o crianca_id quanto o pessoa_id
     */
    suspend fun criarOuBuscarConversa(criancaIdOrPessoaId: Int): Int? {
        return try {
            val pessoaId = _pessoaId.value
            if (pessoaId == null) {
                Log.e("ChatViewModel", "❌ Pessoa ID institucional não encontrado")
                return null
            }

            // Tenta buscar a pessoa_id correta da criança
            var alunoPessoaId = criancaIdOrPessoaId

            // Se não temos certeza se é pessoa_id ou crianca_id, busca os detalhes da criança
            Log.d("ChatViewModel", "🔄 Buscando detalhes da criança ID=$criancaIdOrPessoaId")
            val criancaResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                criancaService.buscarPorId(criancaIdOrPessoaId).execute()
            }

            if (criancaResponse.isSuccessful) {
                val crianca = criancaResponse.body()?.crianca
                if (crianca != null) {
                    alunoPessoaId = crianca.pessoa_id
                    Log.d("ChatViewModel", "✅ Criança encontrada: pessoa_id=${crianca.pessoa_id}")
                }
            } else {
                // Se falhar, assume que o ID fornecido já é o pessoa_id
                Log.w("ChatViewModel", "⚠️ Não foi possível buscar detalhes da criança, usando ID fornecido")
            }

            // Verifica se já existe uma conversa com esse aluno
            val conversaExistente = _conversas.value.find { it.pessoaId == alunoPessoaId }
            if (conversaExistente != null) {
                Log.d("ChatViewModel", "✅ Conversa existente encontrada: ID=${conversaExistente.id}")
                return conversaExistente.id
            }

            // Se não existe, cria uma nova conversa
            Log.d("ChatViewModel", "🔄 Criando nova conversa com aluno pessoa_id=$alunoPessoaId")
            val request = ConversaRequest(participantes = listOf(pessoaId, alunoPessoaId))

            val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                conversaService.criar(request)
            }

            if (response.isSuccessful) {
                val novaConversa = response.body()?.conversa
                if (novaConversa != null) {
                    Log.d("ChatViewModel", "✅ Nova conversa criada: ID=${novaConversa.id}")
                    // Recarrega a lista de conversas para incluir a nova
                    carregarConversas(forcarRecarregar = true)
                    return novaConversa.id
                }
            } else {
                Log.e("ChatViewModel", "❌ Erro ao criar conversa: ${response.errorBody()?.string()}")
                _errorMessage.value = "Erro ao criar conversa com o aluno"
            }

            null
        } catch (e: Exception) {
            Log.e("ChatViewModel", "❌ Erro ao criar/buscar conversa", e)
            _errorMessage.value = "Erro ao conectar: ${e.message}"
            null
        }
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
