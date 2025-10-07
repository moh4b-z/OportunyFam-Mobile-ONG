package com.example.model

data class InstituicaoRequest(
    val id: Int? = null,
    val nome: String,
    val logo: String? = null,
    val cnpj: String,
    val email: String,
    val senha: String,
    val descricao: String? = null,
    val telefone: String? = null,
    // LISTA DE IDs DE TIPOS DE INSTITUIÇÃO
    val tipos_instituicao: List<Int> = emptyList(), // Adicionado para N:M
    val logradouro: String,
    val numero: String? = null,
    val complemento: String? = null,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val cep : String,
)