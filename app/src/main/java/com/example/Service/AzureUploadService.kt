package com.example.Service

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Url
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import java.io.File
import java.io.FileInputStream
import java.util.UUID
import kotlin.io.extension
import kotlin.io.readBytes
import kotlin.jvm.java
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

interface AzureBlobApiService {
    @PUT
    @Headers("x-ms-blob-type: BlockBlob")
    suspend fun uploadFile(
        @Url uploadUrl: String,
        @Header("x-ms-date") date: String,
        @Header("x-ms-version") version: String,
        @Header("Authorization") authorization: String,
        @Body fileBytes: RequestBody
    ): Response<Void>
}












object AzureBlobRetrofit {
    private const val BASE_URL = "https://placeholder.blob.core.windows.net/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
    }

    val apiService: AzureBlobApiService by lazy {
        retrofit.create(AzureBlobApiService::class.java)
    }

    /**
     * Testa a conectividade com o Azure Storage
     */
    suspend fun testAzureConnection(storageAccount: String): Boolean {
        return try {
            println("🔍 Testando conectividade com: https://${storageAccount}.blob.core.windows.net/")

            // Simples teste de resolução DNS executado em IO dispatcher
            val host = "${storageAccount}.blob.core.windows.net"

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val address = java.net.InetAddress.getByName(host)
                println("✅ DNS resolvido: ${address.hostAddress}")
            }
            true
        } catch (e: java.net.UnknownHostException) {
            println("❌ Falha na resolução DNS: ${e.message}")
            println("⚠️ Verifique se o nome da conta está correto: $storageAccount")
            false
        } catch (e: android.os.NetworkOnMainThreadException) {
            println("❌ Erro: Operação de rede na thread principal")
            println("⚠️ Este erro não deveria ocorrer - verifique o contexto da coroutine")
            false
        } catch (e: Exception) {
            println("❌ Falha no teste de conectividade: ${e.message}")
            println("🔍 Tipo de erro: ${e.javaClass.simpleName}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Upload de imagem para Azure Blob Storage usando Shared Key Authentication
     * @param imageFile Arquivo de imagem a ser enviado
     * @param storageAccount Nome da conta de storage do Azure
     * @param accountKey Chave de acesso da conta
     * @param containerName Nome do container no Azure
     * @return URL da imagem ou null em caso de erro
     */
    suspend fun uploadImageToAzure(
        imageFile: File,
        storageAccount: String,
        accountKey: String,
        containerName: String
    ): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        // Gera um nome único para o blob usando UUID
        val extension = imageFile.extension.ifEmpty { "jpg" }
        val blobName = "${UUID.randomUUID()}.$extension"
        val uploadUrl = "https://${storageAccount}.blob.core.windows.net/${containerName}/${blobName}"

        println("📤 Iniciando upload para Azure Storage...")
        println("🔗 Storage Account: $storageAccount")
        println("📦 Container: $containerName")
        println("📄 Blob Name: $blobName")
        println("🌐 URL completa: $uploadUrl")

        try {
            val fileBytes = FileInputStream(imageFile).readBytes()
            val requestBody = fileBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

            println("📊 Tamanho do arquivo: ${fileBytes.size} bytes")

            // Gera data no formato RFC1123
            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("GMT")
            val date = dateFormat.format(Date())

            val version = "2020-04-08"
            val contentLength = fileBytes.size.toString()

            println("📅 Data: $date")
            println("📏 Content-Length: $contentLength")

            // Gera assinatura Shared Key
            val authorization = generateSharedKeyAuth(
                storageAccount = storageAccount,
                accountKey = accountKey,
                method = "PUT",
                contentLength = contentLength,
                contentType = "image/jpeg",
                date = date,
                version = version,
                canonicalizedResource = "/${storageAccount}/${containerName}/${blobName}"
            )

            println("🔐 Authorization gerada")
            println("🚀 Enviando requisição...")

            val response = apiService.uploadFile(
                uploadUrl = uploadUrl,
                date = date,
                version = version,
                authorization = authorization,
                fileBytes = requestBody
            )

            if (response.isSuccessful) {
                println("✅ Upload de imagem bem-sucedido para: $uploadUrl")
                uploadUrl
            } else {
                val errorBody = response.errorBody()?.string()
                println("❌ Erro no upload da imagem: ${response.code()} - $errorBody")
                println("⚠️ Headers da resposta: ${response.headers()}")
                null
            }
        } catch (e: java.net.UnknownHostException) {
            e.printStackTrace()
            println("❌ Erro DNS: Não foi possível resolver o host $storageAccount.blob.core.windows.net")
            println("⚠️ Verifique:")
            println("   1. Nome da conta Azure está correto")
            println("   2. Conexão com internet está ativa")
            println("   3. Firewall não está bloqueando")
            null
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ Erro ao fazer upload da imagem: ${e.message}")
            println("🔍 Tipo de erro: ${e.javaClass.simpleName}")
            null
        }
    }

    /**
     * Gera a assinatura Shared Key para autenticação no Azure Storage
     */
    private fun generateSharedKeyAuth(
        storageAccount: String,
        accountKey: String,
        method: String,
        contentLength: String,
        contentType: String,
        date: String,
        version: String,
        canonicalizedResource: String
    ): String {
        val stringToSign = buildString {
            append("$method\n")           // HTTP Verb
            append("\n")                   // Content-Encoding
            append("\n")                   // Content-Language
            append("$contentLength\n")     // Content-Length
            append("\n")                   // Content-MD5
            append("$contentType\n")       // Content-Type
            append("\n")                   // Date
            append("\n")                   // If-Modified-Since
            append("\n")                   // If-Match
            append("\n")                   // If-None-Match
            append("\n")                   // If-Unmodified-Since
            append("\n")                   // Range
            append("x-ms-blob-type:BlockBlob\n") // CanonicalizedHeaders
            append("x-ms-date:$date\n")
            append("x-ms-version:$version\n")
            append(canonicalizedResource)  // CanonicalizedResource
        }

        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(Base64.decode(accountKey, Base64.DEFAULT), "HmacSHA256")
        mac.init(secretKey)

        val signature = Base64.encodeToString(mac.doFinal(stringToSign.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)

        return "SharedKey $storageAccount:$signature"
    }

    /**
     * Verifica se uma URL de blob é acessível (HEAD request).
     * Retorna true se status for 2xx.
     */
    suspend fun isUrlAccessible(url: String): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val code = connection.responseCode
            connection.disconnect()
            println("🔎 HEAD $url -> $code")
            return@withContext code in 200..299
        } catch (e: Exception) {
            e.printStackTrace()
            println("🔎 HEAD failed for $url: ${e.message}")
            return@withContext false
        }
    }
}

















/**
 * Upload genérico de arquivo para Azure Blob Storage
 * @deprecated Use uploadImageToAzure for images instead
 */
suspend fun uploadFileToAzure(
    file: File,
    storageAccount: String,
    accountKey: String,
    containerName: String
): String? {
    // Redireciona para a função principal
    return AzureBlobRetrofit.uploadImageToAzure(file, storageAccount, accountKey, containerName)
}