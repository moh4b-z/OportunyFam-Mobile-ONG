package com.oportunyfam_mobile_ong.model

/**
 * Modelo para categoria de atividade
 */
data class Categoria(
    val id: Int,
    val nome: String
)

/**
 * Response da API para lista de categorias
 */
data class CategoriasResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val categorias: List<Categoria>
)

