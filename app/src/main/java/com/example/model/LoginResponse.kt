package com.example.oportunyfam.model

import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.Usuario

data class LoginResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val tipo: String,
    val result: ResultData
)
sealed class ResultData {
    data class InstituicaoResult(val data: Instituicao) : ResultData()
    data class UsuarioResult(val data: Usuario) : ResultData()
}

data class LoginRequest(
    val email: String,
    val senha: String
)