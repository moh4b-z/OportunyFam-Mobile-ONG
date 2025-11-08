package com.oportunyfam_mobile_ong.model

import java.time.LocalTime

/**
 * AulaLoteRequest - Requisição para criar múltiplas aulas de uma vez
 *
 * Usado no endpoint POST /atividades/aulas/lote
 * Permite criar várias aulas simultaneamente para uma atividade.
 */
data class AulaLoteRequest(
    val id_atividade: Int,
    val aulas: List<AulaItemRequest>
)

/**
 * AulaItemRequest - Item individual de aula para criação em lote
 */
data class AulaItemRequest(
    val dia_semana: Int,
    val hora_inicio: LocalTime,
    val hora_fim: LocalTime,
    val vagas_total: Int,
    val vagas_disponiveis: Int? = null,
    val ativo: Boolean = true
)

