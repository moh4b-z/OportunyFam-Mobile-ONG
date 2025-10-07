package com.example.oportunyfam.Service

import com.example.Service.TipoInstituicaoService
import com.example.oportunyfam.Service.AtividadeService
import com.example.oportunyfam.Service.AuthService
import com.example.oportunyfam.Service.EnderecoService
import com.example.oportunyfam.Service.InstituicaoService
import com.example.oportunyfam.Service.UsuarioService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitFactory {
    // URL Base da sua API local.
    // OBS: Em emuladores Android, 'localhost' do PC é acessado via 10.0.2.2.
    // Usaremos http://localhost:8080/v1/ conforme solicitado.
    private val BASE_URL = "http://192.168.56.1:8080/v1/"

    private val retrofitFactory = Retrofit
        .Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
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
}