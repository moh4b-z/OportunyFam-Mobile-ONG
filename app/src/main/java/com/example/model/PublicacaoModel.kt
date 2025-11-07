package com.example.model

import com.google.gson.annotations.SerializedName

// Response para criação de publicação
data class PublicacaoCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val publicacao: Publicacao?
)

// Response para lista de publicações
data class PublicacoesListResponse(
    val status: Boolean,
    @SerializedName("publicacoes_instituicao")
    val publicacoes: List<Publicacao>?
)

// Modelo de publicação
data class Publicacao(
    val id: Int,
    @SerializedName("id_instituicao")
    val idInstituicao: Int,
    val titulo: String,
    val descricao: String?,
    val imagem: String?,
    @SerializedName("criado_em")
    val criadoEm: String?,
    @SerializedName("atualizado_em")
    val atualizadoEm: String?
)

// Request para criar publicação
data class PublicacaoRequest(
    @SerializedName("titulo")
    val titulo: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("imagem")
    val imagem: String,
    
    @SerializedName("instituicao_id")
    val instituicaoId: Int
)

// Response para erros da API
data class ErrorResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String
)
