package com.example.oportunyfam.Service



import com.example.model.CriancaListResponse
import com.example.model.CriancaRequest
import com.example.model.CriancaResponse
import com.example.oportunyfam.model.LoginRequest
import com.example.oportunyfam.model.LoginResponse
import retrofit2.Call
import retrofit2.http.*

interface CriancaService {

    // POST /v1/oportunyfam/criancas
    @POST("criancas")
    fun criar(@Body crianca: CriancaRequest): Call<CriancaResponse>

    // PUT /v1/oportunyfam/criancas/:id
    @PUT("criancas/{id}")
    fun atualizar(@Path("id") id: Int, @Body crianca: CriancaRequest): Call<CriancaResponse>

    // GET /v1/oportunyfam/criancas
    @GET("criancas")
    fun listarTodas(): Call<CriancaListResponse>

    // GET /v1/oportunyfam/criancas/:id
    @GET("criancas/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<CriancaResponse>

    // POST /v1/oportunyfam/criancas/login
    @POST("criancas/login")
    fun loginCrianca(@Body request: LoginRequest): Call<LoginResponse>

    // DELETE /v1/oportunyfam/criancas/:id
    @DELETE("criancas/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>
}
