package com.example.oportunyfam.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.Usuario
import java.lang.reflect.Type

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

class ResultDataDeserializer : JsonDeserializer<ResultData> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ResultData {
        val jsonObject = json.asJsonObject

        return if (jsonObject.has("instituicao")) {
            val instituicao = context.deserialize<Instituicao>(
                jsonObject.get("instituicao"),
                Instituicao::class.java
            )
            ResultData.InstituicaoResult(instituicao)
        } else if (jsonObject.has("usuario")) {
            val usuario = context.deserialize<Usuario>(
                jsonObject.get("usuario"),
                Usuario::class.java
            )
            ResultData.UsuarioResult(usuario)
        } else {
            throw IllegalArgumentException("Tipo de resultado desconhecido no JSON")
        }
    }
}

data class LoginRequest(
    val email: String,
    val senha: String
)