package com.example.Config

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
     */
    fun getStorageKey(): String {
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

        // Fallback: Retorna erro claro
        throw IllegalStateException(
            """
            Azure Storage Key não configurada!
            
            Configure a variável de ambiente AZURE_STORAGE_KEY ou
            adicione no local.properties:
            azure.storage.key=SUA_CHAVE_AQUI
            
            Nunca commite chaves de acesso no Git!
            """.trimIndent()
        )
    }

    const val STORAGE_ACCOUNT = "oportunyfamstorage"
    const val CONTAINER_PERFIL = "imagens-perfil"
    const val CONTAINER_PUBLICACOES = "imagens-publicacoes"
}

