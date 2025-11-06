package com.example.oportunyfam_mobile_ong.Service

import com.example.oportunyfam_mobile_ong.model.LoginRequest
import com.example.oportunyfam_mobile_ong.model.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AuthService {

    // POST - Login Universal (Endpoint: /v1/auth/login)
    @Headers("Content-Type: application/json")
    @POST("auth/login")
    fun loginUniversal(@Body request: LoginRequest): Call<LoginResponse>
}