package com.example.Service

import com.example.model.PublicacaoRequest
import com.example.model.PublicacaoCriadaResponse
import com.example.model.PublicacoesListResponse
import retrofit2.Call
import retrofit2.http.*

interface PublicacaoService {
    
    // POST /publicacaoInstituicoes - Criar publicação
    @Headers("Content-Type: application/json")
    @POST("publicacaoInstituicoes")
    fun criarPublicacao(@Body publicacao: PublicacaoRequest): Call<PublicacaoCriadaResponse>
    
    // GET /publicacaoInstituicoes/instituicao/:idInstituicao - Buscar publicações por instituição
    @GET("publicacaoInstituicoes/instituicao/{idInstituicao}")
    fun buscarPublicacoesPorInstituicao(@Path("idInstituicao") idInstituicao: Int): Call<PublicacoesListResponse>
}
