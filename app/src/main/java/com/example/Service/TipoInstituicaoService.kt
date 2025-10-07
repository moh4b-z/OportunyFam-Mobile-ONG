package com.example.Service

import com.example.model.TipoInstituicao
import retrofit2.Call
import retrofit2.http.*

interface TipoInstituicaoService {

    // POST /v1/tipoInstituicao - Criar novo tipo
    @POST("tipoInstituicao")
    fun criar(@Body tipoInstituicao: TipoInstituicao): Call<TipoInstituicao>

    // DELETE /v1/tipoInstituicao/:id - Excluir tipo por ID
    @DELETE("tipoInstituicao/{id}")
    fun deletar(@Path("id") id: Int): Call<Unit>

    // PUT /v1/tipoInstituicao/:id - Atualizar tipo por ID
    @PUT("tipoInstituicao/{id}")
    fun atualizar(@Path("id") id: Int, @Body tipoInstituicao: TipoInstituicao): Call<TipoInstituicao>

    // GET /v1/tipoInstituicao - Listar todos os tipos
    @GET("tipoInstituicao")
    fun listarTodos(): Call<List<TipoInstituicao>>

    // GET /v1/tipoInstituicao/:id - Buscar tipo por ID
    @GET("tipoInstituicao/{id}")
    fun buscarPorId(@Path("id") id: Int): Call<TipoInstituicao>


}