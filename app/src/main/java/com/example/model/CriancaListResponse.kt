package com.example.oportunyfam.model

data class CriancaListResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val criancas: List<CriancaRaw>
)
