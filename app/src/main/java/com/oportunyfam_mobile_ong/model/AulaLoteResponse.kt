package com.oportunyfam_mobile_ong.model

/**
 * AulaLoteResponse - Resposta da criação de múltiplas aulas
 *
 * Retornado pelo endpoint POST /atividades/aulas/lote
 */
data class AulaLoteResponse(
    val status: Boolean,
    val status_code: Int,
    val message: String,
    val aulas_criadas: List<AulaResponse>?
)

