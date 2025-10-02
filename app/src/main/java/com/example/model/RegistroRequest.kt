package com.example.model

data class RegistroRequest(
    val nome: String,
    val email: String,
    val telefone: String,
    val tipoInstituicao: String,
    val cnpj: String,
    val cep: String,
    val senha: String
)
