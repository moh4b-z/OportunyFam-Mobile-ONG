package com.example.oportunyfam.Service


import com.example.Service.EnderecoService
import com.example.Service.LoginUniversalService
import com.example.Service.TipoInstituicaoService
import com.example.oportunyfam.model.ResultData
import com.example.oportunyfam.model.ResultDataDeserializer
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

class RetrofitFactory {
    private val BASE_URL = "https://oportunyfam-back-end.onrender.com/v1/oportunyfam/"

    // Cria o objeto Gson com setLenient e registra o deserializador customizado
    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(ResultData::class.java, ResultDataDeserializer())
        .create()

    // 1. Cria o interceptor de logging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Define o nível de detalhe (Headers e Body serão logados no Logcat)
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. Adiciona o interceptor ao cliente OkHttp
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Adiciona o interceptor
        .build()

    private val retrofitFactory = Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        // CORREÇÃO 1: Devemos passar o nosso objeto 'gson' configurado
        .addConverterFactory(GsonConverterFactory.create(gson))
        // CORREÇÃO 2 (CRÍTICA): Devemos conectar o cliente que contém o interceptor
        .client(client)
        .build()

    fun getInstituicaoService(): InstituicaoService {
        return retrofitFactory.create(InstituicaoService::class.java)
    }

    fun getAuthService(): AuthService {
        return retrofitFactory.create(AuthService::class.java)
    }

    fun getUsuarioService(): UsuarioService {
        return retrofitFactory.create(UsuarioService::class.java)
    }
    fun getEnderecoService(): EnderecoService {
        return retrofitFactory.create(EnderecoService::class.java)
    }
    fun getAtividadeService(): AtividadeService {
        return retrofitFactory.create(AtividadeService::class.java)
    }

    fun getTipoInstituicaoService(): TipoInstituicaoService {
        return retrofitFactory.create(TipoInstituicaoService::class.java)
    }
    fun getCriancaService(): CriancaService {
        return retrofitFactory.create(CriancaService::class.java)
    }

    fun getLoginUniversalService(): LoginUniversalService {
        return retrofitFactory.create(LoginUniversalService::class.java)
    }

}