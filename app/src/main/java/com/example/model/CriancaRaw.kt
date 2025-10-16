package com.example.oportunyfam.model

data class CriancaRaw(
    val id: Int,
    val nome: String,
    val foto_perfil: String? = null,
    val email: String? = null,
    val cpf: String? = null,
    val senha: String? = null,
    val data_nascimento: String? = null,
    val criado_em: String? = null,
    val id_sexo: Int? = null
)
