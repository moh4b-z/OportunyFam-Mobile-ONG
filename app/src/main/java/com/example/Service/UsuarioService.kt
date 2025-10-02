package com.example.Service

import com.example.model.LoginRequest
import com.example.model.LoginResponse
import com.example.model.RegistroRequest
import com.example.model.RegistroResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface UsuarioService {
    @POST("usuario")
    suspend fun registrar(@Body request: RegistroRequest): Response<RegistroResponse>

    @POST("usuario/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
