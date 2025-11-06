package com.example.oportunyfam.model

// Response para aula única criada
data class AulaCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aula: AulaResponse
)

// Response para aula
data class AulaResponse(
    val id: Int,
    val id_atividade: Int,
    val data_aula: String,
    val hora_inicio: String,
    val hora_fim: String,
    val vagas_total: Int,
    val vagas_disponiveis: Int
)

// Response para criação em lote de aulas
data class AulaLoteResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val aulas_inseridas: List<AulaResponse>,
    val total_inseridas: Int,
    val erros: Any? // null ou lista de erros
)