package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.Service.PublicacaoService
import com.example.model.ErrorResponse
import com.example.model.Publicacao
import com.example.model.PublicacaoRequest
import com.example.oportunyfam.Service.RetrofitFactory
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class PublicacaoViewModel : ViewModel() {

    private val publicacaoService: PublicacaoService = RetrofitFactory().getPublicacaoService()

    // Estado para lista de publicações
    private val _publicacoesState = MutableStateFlow<PublicacoesState>(PublicacoesState.Idle)
    val publicacoesState: StateFlow<PublicacoesState> = _publicacoesState

    // Estado para criação de publicação
    private val _criarPublicacaoState = MutableStateFlow<CriarPublicacaoState>(CriarPublicacaoState.Idle)
    val criarPublicacaoState: StateFlow<CriarPublicacaoState> = _criarPublicacaoState

    // Buscar publicações por instituição
    fun buscarPublicacoesPorInstituicao(idInstituicao: Int) {
        viewModelScope.launch {
            _publicacoesState.value = PublicacoesState.Loading
            
            try {
                val response = publicacaoService.buscarPublicacoesPorInstituicao(idInstituicao).awaitResponse()
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        val publicacoes = body.publicacoes ?: emptyList()
                        _publicacoesState.value = PublicacoesState.Success(publicacoes)
                        Log.d("PublicacaoViewModel", "✅ Publicações carregadas: ${publicacoes.size}")
                    } else {
                        _publicacoesState.value = PublicacoesState.Error("Erro ao carregar publicações")
                        Log.e("PublicacaoViewModel", "❌ Response body null ou status false")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.messagem
                    } catch (e: Exception) {
                        "Erro ao carregar publicações"
                    }
                    _publicacoesState.value = PublicacoesState.Error(errorMessage)
                    Log.e("PublicacaoViewModel", "❌ Erro ao buscar publicações: $errorMessage")
                }
            } catch (e: Exception) {
                _publicacoesState.value = PublicacoesState.Error(e.message ?: "Erro desconhecido")
                Log.e("PublicacaoViewModel", "❌ Exception ao buscar publicações: ${e.message}", e)
            }
        }
    }

    // Criar publicação - versão com objeto
    fun criarPublicacao(publicacao: PublicacaoRequest) {
        criarPublicacao(
            titulo = publicacao.titulo,
            descricao = publicacao.descricao,
            imagem = publicacao.imagem,
            instituicaoId = publicacao.instituicaoId
        )
    }

    // Criar publicação - versão com parâmetros individuais
    fun criarPublicacao(titulo: String, descricao: String?, imagem: String?, instituicaoId: Int) {
        viewModelScope.launch {
            _criarPublicacaoState.value = CriarPublicacaoState.Loading

            // Validação do título
            if (titulo.length < 5) {
                _criarPublicacaoState.value = CriarPublicacaoState.Error(
                    "O título deve ter no mínimo 5 caracteres"
                )
                Log.w("PublicacaoViewModel", "⚠️ Validação falhou: título muito curto (${titulo.length} chars)")
                return@launch
            }

            // Validação da descrição
            if (descricao.isNullOrBlank() || descricao.length < 10) {
                _criarPublicacaoState.value = CriarPublicacaoState.Error(
                    "A descrição deve ter no mínimo 10 caracteres"
                )
                Log.w("PublicacaoViewModel", "⚠️ Validação falhou: descrição muito curta (${descricao?.length ?: 0} chars)")
                return@launch
            }

            // Validação da imagem
            if (imagem.isNullOrBlank()) {
                _criarPublicacaoState.value = CriarPublicacaoState.Error(
                    "A imagem é obrigatória"
                )
                Log.w("PublicacaoViewModel", "⚠️ Validação falhou: imagem não fornecida")
                return@launch
            }

            try {
                val request = PublicacaoRequest(
                    titulo = titulo,
                    descricao = descricao,
                    imagem = imagem,
                    instituicaoId = instituicaoId
                )

                Log.d("PublicacaoViewModel", "📤 Enviando publicação: $request")

                val response = publicacaoService.criarPublicacao(request).awaitResponse()

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status) {
                        _criarPublicacaoState.value = CriarPublicacaoState.Success(body.publicacao)
                        Log.d("PublicacaoViewModel", "✅ Publicação criada com sucesso")
                    } else {
                        _criarPublicacaoState.value = CriarPublicacaoState.Error("Erro ao criar publicação")
                        Log.e("PublicacaoViewModel", "❌ Response body null ou status false")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = try {
                        val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse.messagem
                    } catch (e: Exception) {
                        "Erro ao criar publicação"
                    }
                    _criarPublicacaoState.value = CriarPublicacaoState.Error(errorMessage)
                    Log.e("PublicacaoViewModel", "❌ Erro ao criar publicação: $errorMessage")
                }
            } catch (e: Exception) {
                _criarPublicacaoState.value = CriarPublicacaoState.Error(e.message ?: "Erro desconhecido")
                Log.e("PublicacaoViewModel", "❌ Exception ao criar publicação: ${e.message}", e)
            }
        }
    }

    // Resetar estado de criação
    fun resetCriarPublicacaoState() {
        _criarPublicacaoState.value = CriarPublicacaoState.Idle
    }
}

// Estados para listagem de publicações
sealed class PublicacoesState {
    object Idle : PublicacoesState()
    object Loading : PublicacoesState()
    data class Success(val publicacoes: List<Publicacao>) : PublicacoesState()
    data class Error(val message: String) : PublicacoesState()
}

// Estados para criação de publicação
sealed class CriarPublicacaoState {
    object Idle : CriarPublicacaoState()
    object Loading : CriarPublicacaoState()
    data class Success(val publicacao: Publicacao?) : CriarPublicacaoState()
    data class Error(val message: String) : CriarPublicacaoState()
}
