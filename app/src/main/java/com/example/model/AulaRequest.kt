package com.example.oportunyfam.model

// Request para criar uma única aula
data class AulaRequest(
    val id_atividade: Int,
    val data_aula: String, // Formato: "YYYY-MM-DD"
    val hora_inicio: String, // Formato: "HH:MM:SS"
    val hora_fim: String, // Formato: "HH:MM:SS"
    val vagas_total: Int
)

// Request para criar várias aulas em lote
data class AulaLoteRequest(
    val id_atividade: Int,
    val hora_inicio: String, // Formato: "HH:MM:SS"
    val hora_fim: String, // Formato: "HH:MM:SS"
    val vagas_total: Int,
    val datas: List<String> // Lista de datas no formato: "YYYY-MM-DD"
)