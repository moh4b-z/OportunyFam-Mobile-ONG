package com.example.oportunyfam_mobile_ong.Service

import com.example.oportunyfam_mobile_ong.model.*
import retrofit2.Call
import retrofit2.http.*

interface AtividadeService {

    // --- ROTAS DE ATIVIDADE ---

    // POST /atividades/ - Criar atividade
    @Headers("Content-Type: application/json")
    @POST("atividades")
    fun criarAtividade(@Body atividade: AtividadeRequest): Call<AtividadeCriadaResponse>

    // GET /atividades/ - Buscar todas as atividades
    @GET("atividades")
    fun buscarTodasAtividades(): Call<AtividadesListResponse>


    // GET /atividades/instituicao/:idInstituicao - Buscar atividades por instituição
    @GET("atividades/instituicao/{idInstituicao}")
    fun buscarAtividadesPorInstituicao(@Path("idInstituicao") idInstituicao: Int): Call<AtividadesListResponse>

    // PUT /atividades/:id - Atualizar atividade
    @Headers("Content-Type: application/json")
    @PUT("atividades/{id}")
    fun atualizarAtividade(@Path("id") id: Int, @Body atividade: AtividadeRequest): Call<AtividadeCriadaResponse>

    // DELETE /atividades/:id - Deletar atividade
    @DELETE("atividades/{id}")
    fun deletarAtividade(@Path("id") id: Int): Call<Unit>

    // --- ROTAS DE AULAS ---

    // GET /v1/atividades/:id (Busca por ID)
    @GET("atividades/{id}")
    fun buscarAtividadePorId(@Path("id") id: Int): Call<AtividadeResponse>

    // POST /aulas/lote - Criar várias aulas de uma vez
    @Headers("Content-Type: application/json")
    @POST("atividades/aulas/lote")
    fun criarAulasLote(@Body aulaLote: AulaLoteRequest): Call<AulaLoteResponse>

    // GET /aulas/:id - Buscar aula por ID
    @GET("atividades/aulas/{id}")
    fun buscarAulaPorId(@Path("id") id: Int): Call<AulaCriadaResponse>

    // PUT /aulas/:id - Atualizar aula
    @Headers("Content-Type: application/json")
    @PUT("atividades/aulas/{id}")
    fun atualizarAula(@Path("id") id: Int, @Body aula: AulaRequest): Call<AulaCriadaResponse>

    // DELETE /aulas/:id - Deletar aula
    @DELETE("atividades/aulas/{id}")
    fun deletarAula(@Path("id") id: Int): Call<Unit>
}