package com.oportunyfam_mobile_ong.model

// Request para criar/atualizar atividade
data class AtividadeRequest(
    val id_instituicao: Int,
    val id_categoria: Int,
    val titulo: String,
    val descricao: String? = null,
    val faixa_etaria_min: Int,
    val faixa_etaria_max: Int,
    val gratuita: Boolean = true,
    val preco: Double = 0.0,
    val ativo: Boolean = true
)