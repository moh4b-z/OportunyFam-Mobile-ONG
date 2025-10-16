package com.example.oportunyfam.Service

import com.example.Service.TipoInstituicaoService
import com.example.oportunyfam.Service.AtividadeService
import com.example.oportunyfam.Service.AuthService
import com.example.oportunyfam.Service.EnderecoService
import com.example.oportunyfam.Service.InstituicaoService
import com.example.oportunyfam.Service.UsuarioService
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory {
    private val BASE_URL = "http://10.0.2.2:8080/v1/oportunyfam/"

    // Cria o objeto Gson com setLenient para melhor flexibilidade na desserialização
    private val gson = GsonBuilder().setLenient().create()

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

}