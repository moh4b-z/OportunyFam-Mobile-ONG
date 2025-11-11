package com.oportunyfam_mobile_ong.model

/**
 * Enum de Status de Inscrição
 *
 * Baseado na documentação da API (OportunyFam.postman_collection.json)
 * Nota: ID 1 (Sugerida Pela Criança) foi removido conforme solicitado
 */
enum class StatusInscricao(val id: Int, val nome: String) {
    CANCELADA(2, "Cancelada"),
    PENDENTE(3, "Pendente"),
    APROVADA(4, "Aprovada"),
    NEGADA(5, "Negada");

    companion object {
        fun fromId(id: Int): StatusInscricao? = entries.find { it.id == id }

        fun fromNome(nome: String): StatusInscricao? =
            entries.find { it.nome.equals(nome, ignoreCase = true) }
    }
}