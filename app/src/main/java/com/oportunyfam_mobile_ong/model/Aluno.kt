package com.oportunyfam_mobile_ong.model

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

/**
 * Status de inscrição possíveis:
 * 1 - Sugerida Pela Criança (não exibir para instituição)
 * 2 - Cancelada
 * 3 - Pendente
 * 4 - Aprovada
 * 5 - Negada
 */
enum class StatusInscricao(val id: Int, val nome: String) {
    SUGERIDA_PELA_CRIANCA(1, "Sugerida Pela Criança"),
    CANCELADA(2, "Cancelada"),
    PENDENTE(3, "Pendente"),
    APROVADA(4, "Aprovada"),
    NEGADA(5, "Negada");

    companion object {
        fun fromId(id: Int): StatusInscricao? = values().find { it.id == id }
    }
}

