package com.oportunyfam_mobile_ong.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.data.AtividadeFotoDataStore
import com.oportunyfam_mobile_ong.model.AtividadeResponse
import com.oportunyfam_mobile_ong.model.AtividadesListResponse
import com.oportunyfam_mobile_ong.model.AtividadeUnicaResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * ViewModel para gerenciar o estado das atividades
 */
class AtividadeViewModel(private val context: Context? = null) : ViewModel() {

    private val atividadeService = RetrofitFactory().getAtividadeService()

    // Estado das atividades
    private val _atividadesState = MutableStateFlow<AtividadesState>(AtividadesState.Loading)
    val atividadesState: StateFlow<AtividadesState> = _atividadesState.asStateFlow()

    // Estado de uma atividade específica
    private val _atividadeDetalheState = MutableStateFlow<AtividadeDetalheState>(AtividadeDetalheState.Idle)
    val atividadeDetalheState: StateFlow<AtividadeDetalheState> = _atividadeDetalheState.asStateFlow()

    // Estado de criação de atividade
    private val _criarAtividadeState = MutableStateFlow<CriarAtividadeState>(CriarAtividadeState.Idle)
    val criarAtividadeState: StateFlow<CriarAtividadeState> = _criarAtividadeState.asStateFlow()

    /**
     * Buscar todas as atividades
     */
    fun buscarTodasAtividades() {
        _atividadesState.value = AtividadesState.Loading

        viewModelScope.launch {
            try {
                atividadeService.buscarTodasAtividades().enqueue(object : Callback<AtividadesListResponse> {
                    override fun onResponse(
                        call: Call<AtividadesListResponse>,
                        response: Response<AtividadesListResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val atividades = response.body()!!.atividades
                            Log.d("AtividadeViewModel", "Atividades carregadas: ${atividades.size}")

                            // Carregar fotos salvas
                            viewModelScope.launch {
                                carregarFotosSalvas(atividades)
                                _atividadesState.value = AtividadesState.Success(atividades)
                            }
                        } else {
                            Log.e("AtividadeViewModel", "Erro na resposta: ${response.code()}")
                            _atividadesState.value = AtividadesState.Error("Erro ao carregar atividades: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<AtividadesListResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "Falha na requisição", t)
                        _atividadesState.value = AtividadesState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "Exceção ao buscar atividades", e)
                _atividadesState.value = AtividadesState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Buscar atividades por instituição
     */
    fun buscarAtividadesPorInstituicao(instituicaoId: Int) {
        _atividadesState.value = AtividadesState.Loading

        Log.d("AtividadeViewModel", "🔍 Buscando atividades da instituição ID: $instituicaoId")

        viewModelScope.launch {
            try {
                atividadeService.buscarAtividadesPorInstituicao(instituicaoId).enqueue(object : Callback<AtividadesListResponse> {
                    override fun onResponse(
                        call: Call<AtividadesListResponse>,
                        response: Response<AtividadesListResponse>
                    ) {
                        Log.d("AtividadeViewModel", "📡 Resposta recebida - Código: ${response.code()}")

                        when {
                            response.isSuccessful && response.body() != null -> {
                                val atividades = response.body()!!.atividades
                                Log.d("AtividadeViewModel", "✅ ${atividades.size} atividades carregadas")

                                // Carregar fotos salvas
                                viewModelScope.launch {
                                    carregarFotosSalvas(atividades)
                                    _atividadesState.value = AtividadesState.Success(atividades)
                                }
                            }
                            response.code() == 500 -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("AtividadeViewModel", "❌ Erro 500 do servidor")
                                Log.e("AtividadeViewModel", "Resposta: $errorBody")

                                // Tentar buscar todas as atividades como fallback
                                Log.d("AtividadeViewModel", "🔄 Tentando fallback: buscar todas e filtrar localmente")
                                buscarTodasEFiltrar(instituicaoId)
                            }
                            response.code() == 404 -> {
                                Log.w("AtividadeViewModel", "⚠️ Instituição não encontrada ou sem atividades")
                                _atividadesState.value = AtividadesState.Success(emptyList())
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("AtividadeViewModel", "❌ Erro ${response.code()}: $errorBody")
                                _atividadesState.value = AtividadesState.Error(
                                    "Erro ao carregar atividades (${response.code()})"
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<AtividadesListResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "❌ Falha na conexão: ${t.message}", t)
                        _atividadesState.value = AtividadesState.Error(
                            "Erro de conexão: Verifique sua internet"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "❌ Exceção: ${e.message}", e)
                _atividadesState.value = AtividadesState.Error("Erro inesperado: ${e.message}")
            }
        }
    }

    /**
     * Buscar detalhes de uma atividade específica
     */
    fun buscarAtividadePorId(atividadeId: Int) {
        _atividadeDetalheState.value = AtividadeDetalheState.Loading

        viewModelScope.launch {
            try {
                atividadeService.buscarAtividadePorId(atividadeId).enqueue(object : Callback<AtividadeUnicaResponse> {
                    override fun onResponse(
                        call: Call<AtividadeUnicaResponse>,
                        response: Response<AtividadeUnicaResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val atividade = response.body()!!.atividade
                            Log.d("AtividadeViewModel", "Atividade carregada: ${atividade.titulo}")

                            // Carregar foto salva desta atividade
                            viewModelScope.launch {
                                carregarFotosSalvas(listOf(atividade))
                                _atividadeDetalheState.value = AtividadeDetalheState.Success(atividade)
                            }
                        } else {
                            Log.e("AtividadeViewModel", "Erro na resposta: ${response.code()}")
                            _atividadeDetalheState.value = AtividadeDetalheState.Error("Erro ao carregar atividade: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<AtividadeUnicaResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "Falha na requisição", t)
                        _atividadeDetalheState.value = AtividadeDetalheState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "Exceção ao buscar atividade", e)
                _atividadeDetalheState.value = AtividadeDetalheState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Limpar estado de detalhe
     */
    fun limparDetalhe() {
        _atividadeDetalheState.value = AtividadeDetalheState.Idle
    }

    /**
     * Carregar fotos salvas das atividades
     */
    suspend fun carregarFotosSalvas(atividades: List<AtividadeResponse>) {
        if (context == null) {
            Log.w("AtividadeViewModel", "⚠️ Contexto é null, não pode carregar fotos salvas")
            return
        }

        Log.d("AtividadeViewModel", "📷 Carregando fotos salvas para ${atividades.size} atividades")
        val fotoDataStore = AtividadeFotoDataStore(context)
        atividades.forEach { atividade ->
            val fotoSalva = fotoDataStore.buscarFotoAtividade(atividade.atividade_id)
            if (fotoSalva != null) {
                atividade.atividade_foto = fotoSalva
                Log.d("AtividadeViewModel", "📷 Foto carregada para atividade ${atividade.atividade_id} (${atividade.titulo}): $fotoSalva")
            } else {
                Log.d("AtividadeViewModel", "📷 Atividade ${atividade.atividade_id} (${atividade.titulo}) sem foto salva")
            }
        }
        Log.d("AtividadeViewModel", "✅ Carregamento de fotos concluído")
    }

    /**
     * Fallback: busca todas as atividades e filtra pela instituição localmente
     */
    private fun buscarTodasEFiltrar(instituicaoId: Int) {
        viewModelScope.launch {
            try {
                atividadeService.buscarTodasAtividades().enqueue(object : Callback<AtividadesListResponse> {
                    override fun onResponse(
                        call: Call<AtividadesListResponse>,
                        response: Response<AtividadesListResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val todasAtividades = response.body()!!.atividades
                            val atividadesFiltradas = todasAtividades.filter {
                                it.instituicao_id == instituicaoId
                            }

                            Log.d("AtividadeViewModel", "✅ Fallback: ${atividadesFiltradas.size} atividades encontradas")

                            // Carregar fotos salvas
                            viewModelScope.launch {
                                carregarFotosSalvas(atividadesFiltradas)
                                _atividadesState.value = AtividadesState.Success(atividadesFiltradas)
                            }
                        } else {
                            Log.e("AtividadeViewModel", "❌ Fallback também falhou: ${response.code()}")
                            _atividadesState.value = AtividadesState.Error(
                                "Não foi possível carregar as atividades. Tente novamente mais tarde."
                            )
                        }
                    }

                    override fun onFailure(call: Call<AtividadesListResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "❌ Fallback falhou na conexão", t)
                        _atividadesState.value = AtividadesState.Error(
                            "Erro de conexão. Verifique sua internet."
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "❌ Exceção no fallback", e)
                _atividadesState.value = AtividadesState.Error("Erro inesperado: ${e.message}")
            }
        }
    }

    /**
     * Criar nova atividade
     */
    fun criarAtividade(request: com.oportunyfam_mobile_ong.model.AtividadeRequest) {
        _criarAtividadeState.value = CriarAtividadeState.Loading

        Log.d("AtividadeViewModel", "📝 Criando atividade: ${request.titulo}")

        viewModelScope.launch {
            try {
                atividadeService.criarAtividade(request).enqueue(object : Callback<com.oportunyfam_mobile_ong.model.AtividadeCriadaResponse> {
                    override fun onResponse(
                        call: Call<com.oportunyfam_mobile_ong.model.AtividadeCriadaResponse>,
                        response: Response<com.oportunyfam_mobile_ong.model.AtividadeCriadaResponse>
                    ) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                Log.d("AtividadeViewModel", "✅ Atividade criada com sucesso!")
                                _criarAtividadeState.value = CriarAtividadeState.Success(
                                    response.body()!!.atividade
                                )
                                // Recarregar lista de atividades
                                buscarAtividadesPorInstituicao(request.id_instituicao)
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("AtividadeViewModel", "❌ Erro ao criar: $errorBody")
                                _criarAtividadeState.value = CriarAtividadeState.Error(
                                    "Erro ao criar atividade (${response.code()})"
                                )
                            }
                        }
                    }

                    override fun onFailure(call: Call<com.oportunyfam_mobile_ong.model.AtividadeCriadaResponse>, t: Throwable) {
                        Log.e("AtividadeViewModel", "❌ Falha ao criar", t)
                        _criarAtividadeState.value = CriarAtividadeState.Error(
                            "Erro de conexão: ${t.message}"
                        )
                    }
                })
            } catch (e: Exception) {
                Log.e("AtividadeViewModel", "❌ Exceção ao criar", e)
                _criarAtividadeState.value = CriarAtividadeState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Limpar estado de criação
     */
    fun limparEstadoCriacao() {
        _criarAtividadeState.value = CriarAtividadeState.Idle
    }

    /**
     * Atualizar foto de uma atividade na lista sem recarregar tudo
     */
    fun atualizarFotoAtividade(atividadeId: Int, novaFotoUrl: String) {
        val currentState = _atividadesState.value
        if (currentState is AtividadesState.Success) {
            val atividadesAtualizadas = currentState.atividades.map { atividade ->
                if (atividade.atividade_id == atividadeId) {
                    atividade.apply {
                        atividade_foto = novaFotoUrl
                    }
                } else {
                    atividade
                }
            }
            _atividadesState.value = AtividadesState.Success(atividadesAtualizadas)
            Log.d("AtividadeViewModel", "✅ Foto atualizada na lista para atividade $atividadeId")
        }
    }
}

/**
 * Estados possíveis para a lista de atividades
 */
sealed class AtividadesState {
    object Loading : AtividadesState()
    data class Success(val atividades: List<AtividadeResponse>) : AtividadesState()
    data class Error(val message: String) : AtividadesState()
}

/**
 * Estados possíveis para detalhes de uma atividade
 */
sealed class AtividadeDetalheState {
    object Idle : AtividadeDetalheState()
    object Loading : AtividadeDetalheState()
    data class Success(val atividade: AtividadeResponse) : AtividadeDetalheState()
    data class Error(val message: String) : AtividadeDetalheState()
}

/**
 * Estados possíveis para criação de atividade
 */
sealed class CriarAtividadeState {
    object Idle : CriarAtividadeState()
    object Loading : CriarAtividadeState()
    data class Success(val atividade: com.oportunyfam_mobile_ong.model.AtividadeSimples) : CriarAtividadeState()
    data class Error(val message: String) : CriarAtividadeState()
}

