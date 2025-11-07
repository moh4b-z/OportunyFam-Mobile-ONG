package com.oportunyfam_mobile_ong.model


data class Conversa(
    val id: Int,
    val criado_em: String,
    val atualizado_em: String?,
    val participantes: List<Participante>,
    val mensagens: List<Mensagem>? = null
)

data class Participante(
    val id: Int,
    val id_pessoa: Int,
    val id_conversa: Int
)

data class ConversaResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val conversa: Conversa?
)

data class ConversasResponse(
    val status: Boolean,
    val status_code: Int,
    val messagem: String,
    val conversas: List<Conversa>?
)

data class ConversaRequest(
    val participantes: List<Int>
)

// Formato retornado pelo endpoint de instituição
data class ConversaInstituicao(
    val id_conversa: Int,
    val ultima_mensagem: UltimaMensagem?,
    val outro_participante: OutroParticipante
)

data class UltimaMensagem(
    val id: Int,
    val descricao: String,
    val data_envio: String,
    val id_remetente: Int
)

data class OutroParticipante(
    val id: Int,
    val nome: String,
    val foto_perfil: String?
)
