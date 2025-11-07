package com.oportunyfam_mobile_ong.model

/**
 * AulaCriadaResponse - Resposta da criação/atualização de uma aula
 *
 * Retornado pelos endpoints:
 * - POST /atividades/aulas (criar aula)
 * - PUT /atividades/aulas/:id (atualizar aula)
 * - GET /atividades/aulas/:id (buscar aula)
 */
data class AulaCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val message: String,
    val aula: AulaResponse?
)
