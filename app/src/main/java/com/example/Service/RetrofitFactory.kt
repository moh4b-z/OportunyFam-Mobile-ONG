package com.example.service

import com.example.Service.UsuarioService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory {

    // Se estiver testando em dispositivo físico, use o IP da sua máquina na rede local
    private val BASE_URL = "http://localhost:8080/v1/oportunyfam/"

    private val retrofitFactory =
        Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    fun getUsuarioService(): UsuarioService {
        return retrofitFactory.create(UsuarioService::class.java)
    }
}
