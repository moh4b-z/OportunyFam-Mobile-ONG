package com.example.oportunyfam_mobile_ong.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam_mobile_ong.Service.LoginUniversalService
import com.example.oportunyfam_mobile_ong.model.LoginRequest
import com.example.oportunyfam_mobile_ong.model.LoginResponse
import com.example.oportunyfam_mobile_ong.model.ResultData
import com.example.oportunyfam_mobile_ong.R
import com.example.oportunyfam_mobile_ong.Screens.PrimaryColor
import com.example.oportunyfam_mobile_ong.Screens.RegistroOutlinedTextField
import com.example.oportunyfam_mobile_ong.model.Instituicao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response

@Composable
fun LoginContent(
    navController: NavHostController?,
    email: MutableState<String>,
    senha: MutableState<String>,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    loginUniversalService: LoginUniversalService,
    scope: CoroutineScope,
    onAuthSuccess: (Instituicao) -> Unit
) {
    // Campo e-mail
    RegistroOutlinedTextField(
        value = email.value,
        onValueChange = { email.value = it },
        label = stringResource(R.string.label_email),
        leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0x9E000000))
        },
        readOnly = isLoading.value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Campo senha
    RegistroOutlinedTextField(
        value = senha.value,
        onValueChange = { senha.value = it },
        label = stringResource(R.string.label_password),
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = "Senha", tint = Color(0x9E000000))
        },
        visualTransformation = PasswordVisualTransformation(),
        readOnly = isLoading.value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Botão Login
    Button(
        onClick = {
            errorMessage.value = null

            if (email.value.isBlank() || senha.value.isBlank()) {
                errorMessage.value = "Preencha todos os campos"
                return@Button
            }

            isLoading.value = true

            scope.launch {
                try {
                    val request = LoginRequest(email = email.value, senha = senha.value)
                    val response: Response<LoginResponse> = loginUniversalService.loginUniversal(request)
                    val body = response.body()

                    android.util.Log.d("LoginContent", "Response code: ${response.code()}")
                    android.util.Log.d("LoginContent", "Response body: $body")

                    if (response.isSuccessful && (body?.status_code == 200 || body?.status == true)) {
                        when (val result = body?.result) {
                            is ResultData.InstituicaoResult -> {
                                android.util.Log.d("LoginContent", "Login bem-sucedido como Instituição")
                                onAuthSuccess(result.data)
                            }
                            is ResultData.UsuarioResult -> {
                                android.util.Log.d("LoginContent", "Login bem-sucedido como Usuário")
                                errorMessage.value = "Login de usuário ainda não implementado nesta tela"
                                isLoading.value = false
                            }
                            null -> {
                                android.util.Log.e("LoginContent", "Result é null")
                                errorMessage.value = "Erro: Dados não retornados"
                                isLoading.value = false
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        android.util.Log.e("LoginContent", "Erro na resposta: $errorBody")
                        errorMessage.value = "Falha no Login. Verifique suas credenciais. Erro: $errorBody"
                        isLoading.value = false
                    }

                } catch (e: com.google.gson.JsonSyntaxException) {
                    android.util.Log.e("LoginContent", "Erro de parsing JSON", e)
                    errorMessage.value = "Erro ao processar resposta do servidor: ${e.message}"
                    isLoading.value = false
                } catch (e: IllegalArgumentException) {
                    android.util.Log.e("LoginContent", "Erro de argumento", e)
                    errorMessage.value = "Formato de dados inesperado: ${e.message}"
                    isLoading.value = false
                } catch (e: Exception) {
                    android.util.Log.e("LoginContent", "Erro ao conectar", e)
                    errorMessage.value = "Erro ao conectar com o servidor: ${e.message}"
                    isLoading.value = false
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
        shape = RoundedCornerShape(25.dp),
        enabled = !isLoading.value
    ) {
        if (isLoading.value) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text(stringResource(R.string.button_login), color = Color.White, fontSize = 16.sp)
        }
    }
}
