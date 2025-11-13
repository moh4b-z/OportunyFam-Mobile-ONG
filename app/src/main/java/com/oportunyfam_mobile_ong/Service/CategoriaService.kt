package com.oportunyfam_mobile_ong.Service

import com.oportunyfam_mobile_ong.model.CategoriasResponse
import retrofit2.Call
import retrofit2.http.GET

/**
 * Service para operações relacionadas a categorias de atividades
 */
interface CategoriaService {

    /**
     * Buscar todas as categorias disponíveis
     * GET /v1/oportunyfam/categorias
     */
    @GET("categorias")
    fun buscarTodasCategorias(): Call<CategoriasResponse>
}

