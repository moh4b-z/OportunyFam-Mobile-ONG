package com.oportunyfam_mobile_ong.oportunyfam.Service

import com.oportunyfam_mobile_ong.oportunyfam.model.LoginRequest
import com.oportunyfam_mobile_ong.oportunyfam.model.LoginResponse
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