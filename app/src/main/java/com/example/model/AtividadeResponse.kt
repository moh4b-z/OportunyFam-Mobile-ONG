package com.example.oportunyfam.model

import com.google.gson.annotations.SerializedName

// Response para atividade simples (após cadastro)
data class AtividadeCriadaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val atividade: AtividadeSimples
)

data class AtividadeSimples(
    val id: Int,
    val id_instituicao: Int,
    val id_categoria: Int,
    val titulo: String,
    val descricao: String? = null,
    val faixa_etaria_min: Int,
    val faixa_etaria_max: Int,
    val gratuita: Boolean,
    val preco: String,
    val ativo: Boolean
)

// Response para lista de atividades (com detalhes completos)
data class AtividadesListResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val atividades: List<AtividadeResponse>
)

// Response para atividade única (com detalhes completos)
data class AtividadeUnicaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val atividade: AtividadeResponse
)

// Atividade com detalhes completos e aulas
data class AtividadeResponse(
    val atividade_id: Int,
    val titulo: String,
    val descricao: String? = null,
    val faixa_etaria_min: Int,
    val faixa_etaria_max: Int,
    val gratuita: Int, // 0 ou 1
    val preco: Double,
    val ativo: Int, // 0 ou 1
    val categoria: String,
    val instituicao_id: Int,
    val instituicao_nome: String,
    val instituicao_foto: String?,
    val cidade: String,
    val estado: String,
    val aulas: List<AulaDetalhe>
)

// Detalhes de uma aula dentro da atividade
data class AulaDetalhe(
    val status: String, // "Futura" ou "Encerrada"
    val aula_id: Int,
    val hora_fim: String,
    val data_aula: String,
    val hora_inicio: String,
    val vagas_total: Int,
    val vagas_disponiveis: Int
)