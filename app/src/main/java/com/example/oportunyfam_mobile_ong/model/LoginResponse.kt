package com.example.oportunyfam_mobile_ong.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.example.oportunyfam_mobile_ong.model.Instituicao
import com.example.oportunyfam_mobile_ong.model.Usuario
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

        // Log do JSON recebido para debug
        android.util.Log.d("ResultDataDeserializer", "JSON recebido: $jsonObject")

        return when {
            // Caso 1: JSON tem campo "instituicao"
            jsonObject.has("instituicao") -> {
                val instituicao = context.deserialize<Instituicao>(
                    jsonObject.get("instituicao"),
                    Instituicao::class.java
                )
                android.util.Log.d("ResultDataDeserializer", "Tipo identificado: InstituicaoResult (com wrapper)")
                ResultData.InstituicaoResult(instituicao)
            }
            // Caso 2: JSON tem campo "usuario"
            jsonObject.has("usuario") -> {
                val usuario = context.deserialize<Usuario>(
                    jsonObject.get("usuario"),
                    Usuario::class.java
                )
                android.util.Log.d("ResultDataDeserializer", "Tipo identificado: UsuarioResult (com wrapper)")
                ResultData.UsuarioResult(usuario)
            }
            // Caso 3: JSON contém campos específicos de Instituição (instituicao_id, cnpj)
            jsonObject.has("instituicao_id") || jsonObject.has("cnpj") -> {
                val instituicao = context.deserialize<Instituicao>(
                    jsonObject,
                    Instituicao::class.java
                )
                android.util.Log.d("ResultDataDeserializer", "Tipo identificado: InstituicaoResult (direto)")
                ResultData.InstituicaoResult(instituicao)
            }
            // Caso 4: JSON contém campos específicos de Usuário (usuario_id, cpf)
            jsonObject.has("usuario_id") || (jsonObject.has("cpf") && !jsonObject.has("cnpj")) -> {
                val usuario = context.deserialize<Usuario>(
                    jsonObject,
                    Usuario::class.java
                )
                android.util.Log.d("ResultDataDeserializer", "Tipo identificado: UsuarioResult (direto)")
                ResultData.UsuarioResult(usuario)
            }
            // Caso 5: Erro - não foi possível identificar o tipo
            else -> {
                val campos = jsonObject.keySet().joinToString(", ")
                android.util.Log.e("ResultDataDeserializer", "Campos disponíveis: $campos")
                throw IllegalArgumentException("Tipo de resultado desconhecido no JSON. Campos disponíveis: $campos")
            }
        }
    }
}

data class LoginRequest(
    val email: String,
    val senha: String
)