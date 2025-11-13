package com.oportunyfam_mobile_ong.model

// ============ MODELOS PARA AULAS ============

// Request para criar uma aula individual
data class AulaRequest(
    val id_atividade: Int,
    val data_aula: String, // Formato: "YYYY-MM-DD"
    val hora_inicio: String, // Formato: "HH:MM:SS"
    val hora_fim: String, // Formato: "HH:MM:SS"
    val vagas_total: Int,
    val vagas_disponiveis: Int? = null,
    val ativo: Boolean = true
)

// Response para aula criada
data class AulaCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aula: AulaSimples?
)

// Aula simples (após criação)
data class AulaSimples(
    val id: Int,
    val id_atividade: Int,
    val data_aula: String,
    val hora_inicio: String,
    val hora_fim: String,
    val vagas_total: Int,
    val vagas_disponiveis: Int
)

// Request para criar várias aulas de uma vez (lote)
data class AulaLoteRequest(
    val id_atividade: Int,
    val hora_inicio: String, // Formato: "HH:MM:SS"
    val hora_fim: String, // Formato: "HH:MM:SS"
    val vagas_total: Int,
    val datas: List<String> // Lista de datas no formato "YYYY-MM-DD"
)

// Response para criação de aulas em lote
data class AulaLoteResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aulas_inseridas: List<AulaSimples>?,
    val total_inseridas: Int?,
    val erros: Any?
)

// Response para todas as aulas
data class AulasListResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aulas: List<AulaDetalhada>?
)

// Response para aula única
data class AulaUnicaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aula: AulaDetalhada?
)

// Aula com todos os detalhes (incluindo participantes)
data class AulaDetalhada(
    val aula_id: Int,
    val id_atividade: Int,
    val data_aula: String,
    val hora_inicio: String,
    val hora_fim: String,
    val vagas_total: Int,
    val vagas_disponiveis: Int,
    val status_aula: String?, // "Futura", "Hoje", "Encerrada"
    val iram_participar: List<ParticipanteAula>?,
    val foram: List<ParticipanteAula>?,
    val ausentes: List<ParticipanteAula>?,
    val nome_atividade: String? = null, // Presente ao buscar aulas por instituição
    val instituicao_nome: String? = null // Presente ao buscar aulas por instituição
) {
    /**
     * Converte AulaDetalhada para AulaDetalhe (modelo usado nos cards)
     */
    fun toAulaDetalhe(): AulaDetalhe {
        return AulaDetalhe(
            status_aula = status_aula,
            aula_id = aula_id,
            hora_fim = hora_fim,
            data = data_aula,
            data_aula = data_aula,
            hora_inicio = hora_inicio,
            vagas_total = vagas_total,
            vagas_disponiveis = vagas_disponiveis,
            iram_participar = iram_participar,
            foram = foram,
            ausentes = ausentes,
            nome_atividade = nome_atividade,
            instituicao_nome = instituicao_nome
        )
    }
}

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
    val data_inscricao: String,
    val inscricao_id: Int? = null,
    val id_responsavel: Int? = null,
    val responsavel_id_pessoa: Int? = null,
    val crianca_id_pessoa: Int? = null
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