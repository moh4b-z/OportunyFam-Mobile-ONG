package com.example.oportunyfam_mobile_ong

import com.example.model.Publicacao
import com.example.model.PublicacaoRequest
import com.example.model.PublicacoesListResponse
import com.example.model.PublicacaoCriadaResponse
import com.example.model.ErrorResponse
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Publicacao data models
 */
class PublicacaoModelTest {

    @Test
    fun publicacao_creation_isCorrect() {
        val publicacao = Publicacao(
            id = 1,
            idInstituicao = 10,
            titulo = "Título de teste",
            descricao = "Descrição de teste com mais de 10 caracteres",
            imagem = "https://example.com/image.jpg",
            criadoEm = "2025-01-01",
            atualizadoEm = "2025-01-01"
        )

        assertEquals(1, publicacao.id)
        assertEquals(10, publicacao.idInstituicao)
        assertEquals("Título de teste", publicacao.titulo)
        assertNotNull(publicacao.descricao)
        assertNotNull(publicacao.imagem)
    }

    @Test
    fun publicacaoRequest_creation_isCorrect() {
        val request = PublicacaoRequest(
            titulo = "Título",
            descricao = "Descrição longa",
            imagem = "https://example.com/image.jpg",
            instituicaoId = 10
        )

        assertEquals("Título", request.titulo)
        assertEquals("Descrição longa", request.descricao)
        assertEquals("https://example.com/image.jpg", request.imagem)
        assertEquals(10, request.instituicaoId)
    }

    @Test
    fun publicacoesListResponse_withEmptyList_isCorrect() {
        val response = PublicacoesListResponse(
            status = true,
            publicacoes = emptyList()
        )

        assertTrue(response.status)
        assertNotNull(response.publicacoes)
        assertEquals(0, response.publicacoes?.size)
    }

    @Test
    fun publicacoesListResponse_withNullList_isCorrect() {
        val response = PublicacoesListResponse(
            status = true,
            publicacoes = null
        )

        assertTrue(response.status)
        assertNull(response.publicacoes)
    }

    @Test
    fun publicacaoCriadaResponse_creation_isCorrect() {
        val publicacao = Publicacao(
            id = 1,
            idInstituicao = 10,
            titulo = "Novo",
            descricao = "Descrição",
            imagem = "https://example.com/image.jpg",
            criadoEm = "2025-01-01",
            atualizadoEm = "2025-01-01"
        )

        val response = PublicacaoCriadaResponse(
            status = true,
            status_code = 201,
            messagem = "Publicação criada",
            publicacao = publicacao
        )

        assertTrue(response.status)
        assertEquals(201, response.status_code)
        assertEquals("Publicação criada", response.messagem)
        assertNotNull(response.publicacao)
    }

    @Test
    fun errorResponse_creation_isCorrect() {
        val errorResponse = ErrorResponse(
            status = false,
            status_code = 400,
            messagem = "Campo obrigatório não colocado, ou ultrapassagem de caracteres"
        )

        assertFalse(errorResponse.status)
        assertEquals(400, errorResponse.status_code)
        assertTrue(errorResponse.messagem.contains("obrigatório"))
    }
}
