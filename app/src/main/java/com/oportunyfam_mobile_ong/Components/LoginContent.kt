package com.oportunyfam_mobile_ong.Components

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
import com.oportunyfam_mobile_ong.Service.LoginUniversalService
import com.oportunyfam_mobile_ong.model.LoginRequest
import com.oportunyfam_mobile_ong.model.LoginResponse
import com.oportunyfam_mobile_ong.model.ResultData
import com.oportunyfam_mobile_ong.Screens.PrimaryColor
import com.oportunyfam_mobile_ong.Screens.RegistroOutlinedTextField
import com.oportunyfam_mobile_ong.model.Instituicao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response
import com.oportunyfam_mobile_ong.R

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

                    if (response.isSuccessful && (body?.status_code == 200 || body?.status == true)) {
                        when (val result = body?.result) {
                            is ResultData.InstituicaoResult -> {
                                android.util.Log.d("LoginContent", "Login de instituição bem-sucedido: ${result.data.nome}")
                                onAuthSuccess(result.data)
                            }
                            is ResultData.UsuarioResult -> {
                                errorMessage.value = "Esta aplicação é apenas para instituições"
                                android.util.Log.w("LoginContent", "Tentativa de login de usuário na aplicação de instituição")
                                isLoading.value = false
                            }
                            null -> {
                                errorMessage.value = "Erro ao processar dados de login"
                                android.util.Log.e("LoginContent", "Resultado do login é nulo")
                                isLoading.value = false
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        errorMessage.value = when (response.code()) {
                            401 -> "Email ou senha incorretos"
                            404 -> "Conta não encontrada"
                            500 -> "Erro no servidor. Tente novamente mais tarde"
                            else -> "Falha no login. Verifique suas credenciais"
                        }
                        android.util.Log.e("LoginContent", "Erro no login - Código: ${response.code()}, Mensagem: ${errorBody ?: response.message()}")
                        isLoading.value = false
                    }

                } catch (e: java.net.UnknownHostException) {
                    errorMessage.value = "Sem conexão com a internet"
                    android.util.Log.e("LoginContent", "Erro de conexão: Sem internet", e)
                    isLoading.value = false
                } catch (e: java.net.SocketTimeoutException) {
                    errorMessage.value = "Tempo de conexão esgotado. Tente novamente"
                    android.util.Log.e("LoginContent", "Erro de conexão: Timeout", e)
                    isLoading.value = false
                } catch (e: Exception) {
                    errorMessage.value = "Erro ao conectar com o servidor"
                    android.util.Log.e("LoginContent", "Erro inesperado no login: ${e.message}", e)
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
