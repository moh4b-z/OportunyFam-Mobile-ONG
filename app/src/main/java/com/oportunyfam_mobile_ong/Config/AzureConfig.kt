package com.oportunyfam_mobile_ong.Config

import android.util.Log

/**
 * Configurações do Azure Storage
 * IMPORTANTE: Nunca commite chaves de acesso no Git!
 * Use variáveis de ambiente ou arquivo local.properties
 */
object AzureConfig {
    /**
     * Obtém a chave de acesso do Azure Storage
     * Prioridade:
     * 1. Variável de ambiente AZURE_STORAGE_KEY
     * 2. Propriedade do sistema azure.storage.key
     * 3. BuildConfig (se configurado)
     *
     * @return A chave de acesso ou null se não configurada
     */
    fun getStorageKey(): String? {
        // Tenta obter da variável de ambiente
        val envKey = System.getenv("AZURE_STORAGE_KEY")
        if (!envKey.isNullOrBlank()) {
            return envKey
        }

        // Tenta obter da propriedade do sistema
        val sysKey = System.getProperty("azure.storage.key")
        if (!sysKey.isNullOrBlank()) {
            return sysKey
        }

        // Log de aviso se não configurado
        Log.w("AzureConfig", """
            ⚠️ Azure Storage Key não configurada!
            
            Para habilitar upload de imagens:
            1. Configure a variável de ambiente AZURE_STORAGE_KEY ou
            2. Adicione no local.properties:
               azure.storage.key=SUA_CHAVE_AQUI
            
            IMPORTANTE: Nunca commite chaves de acesso no Git!
        """.trimIndent())

        return null
    }

    /**
     * Verifica se o Azure Storage está configurado
     */
    fun isConfigured(): Boolean {
        return getStorageKey() != null
    }

    const val STORAGE_ACCOUNT = "oportunyfamstorage"
    const val CONTAINER_PERFIL = "imagens-perfil"
    const val CONTAINER_PUBLICACOES = "imagens-publicacoes"
}
