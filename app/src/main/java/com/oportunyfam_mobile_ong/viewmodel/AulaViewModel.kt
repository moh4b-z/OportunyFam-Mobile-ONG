package com.oportunyfam_mobile_ong.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel para gerenciar o estado das aulas
 */
class AulaViewModel : ViewModel() {

    private val atividadeService = RetrofitFactory().getAtividadeService()

    // Estado das aulas por atividade
    private val _aulasState = MutableStateFlow<AulasState>(AulasState.Idle)
    val aulasState: StateFlow<AulasState> = _aulasState.asStateFlow()

    // Estado de criação de aula
    private val _criarAulaState = MutableStateFlow<CriarAulaState>(CriarAulaState.Idle)
    val criarAulaState: StateFlow<CriarAulaState> = _criarAulaState.asStateFlow()

    /**
     * Buscar aulas de uma atividade específica
     * Busca diretamente a atividade (que já inclui as aulas)
     */
    fun buscarAulasPorAtividade(atividadeId: Int, instituicaoId: Int) {
        _aulasState.value = AulasState.Loading

        Log.d("AulaViewModel", "🔍 Buscando aulas da atividade ID: $atividadeId")

        viewModelScope.launch {
            try {
                // Buscar a atividade específica (que já inclui as aulas)
                atividadeService.buscarAtividadePorId(atividadeId).enqueue(object : Callback<AtividadeUnicaResponse> {
                    override fun onResponse(
                        call: Call<AtividadeUnicaResponse>,
                        response: Response<AtividadeUnicaResponse>
                    ) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                val atividade = response.body()!!.atividade
                                val aulas = atividade.aulas

                                Log.d("AulaViewModel", "✅ ${aulas.size} aulas carregadas para atividade $atividadeId")

                                // Converter AulaDetalhe para AulaDetalhada
                                val aulasDetalhadas = aulas.map { aulaDetalhe ->
                                    AulaDetalhada(
                                        aula_id = aulaDetalhe.aula_id,
                                        id_atividade = atividadeId,
                                        data_aula = aulaDetalhe.data_aula ?: aulaDetalhe.data ?: "",
                                        hora_inicio = aulaDetalhe.hora_inicio,
                                        hora_fim = aulaDetalhe.hora_fim,
                                        vagas_total = aulaDetalhe.vagas_total,
                                        vagas_disponiveis = aulaDetalhe.vagas_disponiveis,
                                        status_aula = aulaDetalhe.status_aula,
                                        iram_participar = aulaDetalhe.iram_participar,
                                        foram = aulaDetalhe.foram,
                                        ausentes = aulaDetalhe.ausentes,
                                        nome_atividade = atividade.titulo,
                                        instituicao_nome = atividade.instituicao_nome
                                    )
                                }

                                // Log detalhado de cada aula
                                aulasDetalhadas.forEach { aula ->
                                    Log.d("AulaViewModel", "  📅 Aula ID ${aula.aula_id}: ${aula.data_aula} ${aula.hora_inicio}-${aula.hora_fim}")
                                }

                                _aulasState.value = AulasState.Success(aulasDetalhadas)
                            }
                            response.code() == 404 -> {
                                Log.d("AulaViewModel", "ℹ️ Atividade não encontrada (404)")
                                _aulasState.value = AulasState.Success(emptyList())
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string()
                                Log.e("AulaViewModel", "❌ Erro ${response.code()}: $errorBody")
                                _aulasState.value = AulasState.Error("Erro ao carregar aulas (${response.code()})")
                            }
                        }
                    }

                    override fun onFailure(call: Call<AtividadeUnicaResponse>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Falha na conexão: ${t.message}", t)
                        _aulasState.value = AulasState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção: ${e.message}", e)
                _aulasState.value = AulasState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Criar uma aula individual
     */
    fun criarAula(aulaRequest: AulaRequest) {
        _criarAulaState.value = CriarAulaState.Loading

        Log.d("AulaViewModel", "📝 Criando aula para ${aulaRequest.data_aula}")

        viewModelScope.launch {
            try {
                atividadeService.criarAula(aulaRequest).enqueue(object : Callback<AulaCriadaResponse> {
                    override fun onResponse(
                        call: Call<AulaCriadaResponse>,
                        response: Response<AulaCriadaResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            Log.d("AulaViewModel", "✅ Aula criada com sucesso!")
                            _criarAulaState.value = CriarAulaState.Success(response.body()!!.aula)
                        } else {
                            Log.e("AulaViewModel", "❌ Erro ao criar: ${response.errorBody()?.string()}")
                            _criarAulaState.value = CriarAulaState.Error("Erro ao criar aula (${response.code()})")
                        }
                    }

                    override fun onFailure(call: Call<AulaCriadaResponse>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Falha ao criar", t)
                        _criarAulaState.value = CriarAulaState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção ao criar", e)
                _criarAulaState.value = CriarAulaState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Criar várias aulas de uma vez (lote)
     */
    fun criarAulasLote(aulaLoteRequest: AulaLoteRequest) {
        _criarAulaState.value = CriarAulaState.Loading

        // ✅ Log detalhado do que está sendo enviado
        Log.d("AulaViewModel", "📝 Criando ${aulaLoteRequest.datas.size} aulas em lote:")
        Log.d("AulaViewModel", "  📅 Datas: ${aulaLoteRequest.datas.joinToString(", ")}")
        Log.d("AulaViewModel", "  ⏰ Início: ${aulaLoteRequest.hora_inicio}")
        Log.d("AulaViewModel", "  ⏰ Fim: ${aulaLoteRequest.hora_fim}")
        Log.d("AulaViewModel", "  👥 Vagas: ${aulaLoteRequest.vagas_total}")

        viewModelScope.launch {
            try {
                atividadeService.criarAulasLote(aulaLoteRequest).enqueue(object : Callback<AulaLoteResponse> {
                    override fun onResponse(
                        call: Call<AulaLoteResponse>,
                        response: Response<AulaLoteResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val total = response.body()!!.total_inseridas ?: 0
                            Log.d("AulaViewModel", "✅ $total aulas criadas com sucesso!")
                            _criarAulaState.value = CriarAulaState.SuccessLote(
                                response.body()!!.aulas_inseridas ?: emptyList(),
                                total
                            )
                        } else {
                            Log.e("AulaViewModel", "❌ Erro ao criar: ${response.errorBody()?.string()}")
                            _criarAulaState.value = CriarAulaState.Error("Erro ao criar aulas (${response.code()})")
                        }
                    }

                    override fun onFailure(call: Call<AulaLoteResponse>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Falha ao criar", t)
                        _criarAulaState.value = CriarAulaState.Error("Erro de conexão: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção ao criar", e)
                _criarAulaState.value = CriarAulaState.Error("Erro: ${e.message}")
            }
        }
    }

    /**
     * Deletar uma aula
     */
    fun deletarAula(aulaId: Int) {
        Log.d("AulaViewModel", "🗑️ Deletando aula ID: $aulaId")

        viewModelScope.launch {
            try {
                atividadeService.deletarAula(aulaId).enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            Log.d("AulaViewModel", "✅ Aula deletada com sucesso!")
                        } else {
                            Log.e("AulaViewModel", "❌ Erro ao deletar: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Falha ao deletar", t)
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção ao deletar", e)
            }
        }
    }

    /**
     * Limpar estado de criação
     */
    fun limparEstadoCriacao() {
        _criarAulaState.value = CriarAulaState.Idle
    }

    /**
     * Recarregar aulas após criar/deletar
     */
    fun recarregarAulas(atividadeId: Int, instituicaoId: Int) {
        Log.d("AulaViewModel", "🔄 Iniciando recarregamento de aulas...")
        buscarAulasPorAtividade(atividadeId, instituicaoId)
    }

    /**
     * Buscar todas as aulas (fallback) e filtrar por atividade
     */
    fun buscarTodasAulasEFiltrar(atividadeId: Int) {
        _aulasState.value = AulasState.Loading

        Log.d("AulaViewModel", "🔍 Fallback: Buscando TODAS as aulas e filtrando...")

        viewModelScope.launch {
            try {
                atividadeService.buscarTodasAulas().enqueue(object : Callback<AulasListResponse> {
                    override fun onResponse(
                        call: Call<AulasListResponse>,
                        response: Response<AulasListResponse>
                    ) {
                        when {
                            response.isSuccessful && response.body() != null -> {
                                val todasAulas = response.body()!!.aulas ?: emptyList()
                                val aulasFiltradas = todasAulas.filter { it.id_atividade == atividadeId }

                                Log.d("AulaViewModel", "✅ Fallback: ${aulasFiltradas.size} aulas encontradas")
                                _aulasState.value = AulasState.Success(aulasFiltradas)
                            }
                            response.code() == 404 -> {
                                Log.d("AulaViewModel", "ℹ️ Fallback: Nenhuma aula (404)")
                                _aulasState.value = AulasState.Success(emptyList())
                            }
                            else -> {
                                Log.e("AulaViewModel", "❌ Fallback falhou: ${response.code()}")
                                _aulasState.value = AulasState.Error("Erro ao buscar aulas")
                            }
                        }
                    }

                    override fun onFailure(call: Call<AulasListResponse>, t: Throwable) {
                        Log.e("AulaViewModel", "❌ Fallback falhou: ${t.message}", t)
                        _aulasState.value = AulasState.Error("Erro de conexão")
                    }
                })
            } catch (e: Exception) {
                Log.e("AulaViewModel", "❌ Exceção no fallback", e)
                _aulasState.value = AulasState.Error("Erro inesperado")
            }
        }
    }
}

/**
 * Estados possíveis para a lista de aulas
 */
sealed class AulasState {
    object Idle : AulasState()
    object Loading : AulasState()
    data class Success(val aulas: List<AulaDetalhada>) : AulasState()
    data class Error(val message: String) : AulasState()
}

/**
 * Estados possíveis para criação de aula
 */
sealed class CriarAulaState {
    object Idle : CriarAulaState()
    object Loading : CriarAulaState()
    data class Success(val aula: AulaSimples?) : CriarAulaState()
    data class SuccessLote(val aulas: List<AulaSimples>, val total: Int) : CriarAulaState()
    data class Error(val message: String) : CriarAulaState()
}
