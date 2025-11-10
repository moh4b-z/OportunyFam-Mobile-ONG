package com.oportunyfam_mobile_ong.model


import java.time.LocalTime

data class HorarioDetalhe(
    val id: Int,
    val dia_semana: Int,
    val inicio: LocalTime, // Mapeia para 'inicio' no JSON_OBJECT da View
    val fim: LocalTime,    // Mapeia para 'fim' no JSON_OBJECT da View
    val vagas_total: Int,
    val vagas_disponiveis: Int
)
// Esta classe é a estrutura do array JSON retornado pelo backend
data class AulaResponse(
    val id: Int,
    val id_atividade: Int,
    val dia_semana: Int,
    val hora_inicio: LocalTime,
    val hora_fim: LocalTime,
    val vagas_total: Int,
    val vagas_disponiveis: Int,
    val ativo: Boolean
)

data class AulaRequest(
    val id: Int? = null, // Para PUT
    val id_atividade: Int, // Chave estrangeira obrigatória
    val dia_semana: Int, // 1=Dom, 2=Seg, ...
    val hora_inicio: LocalTime,
    val hora_fim: LocalTime,
    val vagas_total: Int,
    val vagas_disponiveis: Int, // Opcional, pode ser calculado no backend
    val ativo: Boolean = true
)

data class AulaLoteResponse(
    val status: Boolean,
    val status_code: Int,
    val message: String,
    val aulas_criadas: List<AulaResponse>?
)

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

data class AulaCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val message: String,
    val aula: AulaResponse?
)

/**
 * Modelo de Aluno - Representa uma criança inscrita em uma atividade da instituição
 */
data class Aluno(
    val instituicao_id: Int,
    val instituicao_nome: String,
    val atividade_id: Int,
    val atividade_titulo: String,
    val crianca_id: Int,
    val crianca_nome: String,
    val crianca_foto: String?,
    val status_id: Int,
    val status_inscricao: String,
    val data_inscricao: String
)

/**
 * Resposta da API para buscar alunos
 */
data class AlunosResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val alunos: List<Aluno>?
)