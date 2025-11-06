package com.example.oportunyfam.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.Usuario
import java.lang.reflect.Type

/**
 * LoginResponse - Resposta da API de login
 *
 * Estrutura atualizada para corresponder à resposta real da API:
 * - result contém os dados da instituição/usuário diretamente
 * - accessToken é retornado no nível raiz
 */
data class LoginResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val tipo: String, // "instituicao" ou "usuario"
    val result: ResultData?,
    val accessToken: String? // Token de acesso JWT
)

/**
 * ResultData - Dados do resultado do login
 *
 * Pode ser dados de uma instituição ou usuário
 */
sealed class ResultData {
    data class InstituicaoResult(val data: Instituicao) : ResultData()
    data class UsuarioResult(val data: Usuario) : ResultData()
}

/**
 * ResultDataDeserializer - Deserializador customizado para ResultData
 *
 * Atualizado para lidar com a estrutura real da API onde:
 * - O campo 'tipo' indica se é "instituicao" ou "usuario"
 * - Os dados estão diretamente no campo 'result'
 */
class ResultDataDeserializer : JsonDeserializer<ResultData> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ResultData {
        val jsonObject = json.asJsonObject

        // Obtém o tipo do contexto pai (deve ser injetado)
        // Como não temos acesso ao contexto pai aqui, vamos verificar pelos campos
        return try {
            when {
                // Verifica se tem campos específicos de instituição
                jsonObject.has("instituicao_id") -> {
                    val instituicao = context.deserialize<Instituicao>(json, Instituicao::class.java)
                    ResultData.InstituicaoResult(instituicao)
                }
                // Verifica se tem campos específicos de usuário
                jsonObject.has("usuario_id") -> {
                    val usuario = context.deserialize<Usuario>(json, Usuario::class.java)
                    ResultData.UsuarioResult(usuario)
                }
                // Se não conseguir identificar, tenta instituição primeiro
                else -> {
                    val instituicao = context.deserialize<Instituicao>(json, Instituicao::class.java)
                    ResultData.InstituicaoResult(instituicao)
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Não foi possível deserializar os dados do resultado: ${e.message}", e)
        }
    }
}

/**
 * LoginRequest - Dados para requisição de login
 */
data class LoginRequest(
    val email: String,
    val senha: String
)