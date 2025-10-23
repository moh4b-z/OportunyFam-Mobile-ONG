package com.oportunyfam_mobile.model

import com.example.model.TipoInstituicao
import com.example.oportunyfam.model.EnderecoResponse

data class InstituicaoRequest(
    val nome: String,
    val logo: String?,
    val cnpj: String,
    val telefone: String?,
    val email: String,
    val senha: String,
    val descricao: String?,
    val cep: String,
    val logradouro: String,
    val numero: String?,
    val complemento: String?,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val tipos_instituicao: List<Int?>
)

data class InstituicaoResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val instituicao: Instituicao?
)

data class Instituicao(
    val id: Int,
    val nome: String,
    val cnpj: String,
    val email: String,
    val descricao: String?,
    val criado_em: String,
    val endereco: EnderecoResponse?,
    val tipos_instituicao: List<TipoInstituicao>?,
    val telefone: String?,
    val logo: String?
)


data class InstituicaoAtualizarRequest(
    val nome: String? = null,
    val cnpj: String? = null,
    val email: String? = null,
    val descricao: String? = null,
    val telefone: String? = null,
    val logo: String? = null
)