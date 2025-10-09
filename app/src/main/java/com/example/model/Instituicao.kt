package com.example.oportunyfam.model

import com.google.gson.annotations.SerializedName

class Instituicao(
    val id: Int,
    val nome: String,
    val logo: String?,
    val cnpj: String,
    val telefone: String,
    val email: String,
    val senha: String,
    val descricao: String?,
    @SerializedName("criado_em")
    val criadoEm: String,
    @SerializedName("id_endereco")
    val idEndereco: Int
)

// Estrutura completa da resposta da API
data class AuthResponse(
    val status: Boolean,
    @SerializedName("status_code")
    val statusCode: Int,
    val messagem: String,
    val instituicao: Instituicao
)
