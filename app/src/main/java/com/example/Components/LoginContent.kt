package com.example.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam.Service.InstituicaoService
import com.example.oportunyfam.model.Instituicao
import com.example.oportunyfam.model.LoginRequest
import com.example.oportunyfam_mobile_ong.R
import com.example.screens.PrimaryColor
import com.example.screens.RegistroOutlinedTextField
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
    instituicaoService: InstituicaoService,
    scope: CoroutineScope,
    // REMOVIDO: authDataStore: AuthDataStore
    // Adicionado o callback que a tela principal irá usar para salvar e navegar
    onAuthSuccess: (Instituicao) -> Unit
) {
    // Email
    RegistroOutlinedTextField(
        value = email.value,
        onValueChange = { email.value = it },
        label = stringResource(R.string.label_email),
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = stringResource(R.string.desc_icon_email), tint = Color(0x9E000000)) },
        readOnly = isLoading.value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Senha
    RegistroOutlinedTextField(
        value = senha.value,
        onValueChange = { senha.value = it },
        label = stringResource(R.string.label_password),
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.desc_icon_lock), tint = Color(0x9E000000)) },
        visualTransformation = PasswordVisualTransformation(),
        readOnly = isLoading.value,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Lembrar-me + Esqueceu senha
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = false,
                onCheckedChange = { /* lembrar-me */ },
                colors = CheckboxDefaults.colors(checkedColor = PrimaryColor),
                enabled = !isLoading.value
            )
            Text(stringResource(R.string.label_remember_me), color = Color.Gray, fontSize = 14.sp)
        }
        TextButton(
            onClick = { /* esqueceu senha */ },
            enabled = !isLoading.value
        ) {
            Text(stringResource(R.string.button_forgot_password), color = PrimaryColor, fontSize = 14.sp)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

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
                    val request = LoginRequest(
                        email = email.value,
                        senha = senha.value
                    )

                    // Chamada ao serviço
                    val response: Response<Instituicao> = instituicaoService.loginInstituicao(request)

                    // Acessando propriedades da Response
                    if (response.isSuccessful && response.body() != null) {
                        val instituicaoLogada = response.body()!!

                        // ✨ CHAMADA AO CALLBACK CENTRALIZADO
                        // A responsabilidade de salvar e navegar está agora em RegistroScreen.kt
                        onAuthSuccess(instituicaoLogada)

                    } else {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        errorMessage.value = "Falha no Login. Verifique suas credenciais. Erro: $errorBody"
                        isLoading.value = false // Para o loading em caso de erro
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Erro ao conectar com o servidor: ${e.message}"
                    isLoading.value = false // Para o loading em caso de exceção
                }
                // O bloco finally foi removido ou modificado, pois o onAuthSuccess
                // irá resetar o isLoading em caso de sucesso no RegistroScreen.kt.
                // Mantemos o reset do isLoading nos blocos 'else' e 'catch'.
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
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(stringResource(R.string.button_login), color = Color.White, fontSize = 16.sp)
        }
    }

    Spacer(modifier = Modifier.height(15.dp))

    // Divider "Ou entre com"
    HorizontalDivider(thickness = 1.dp, color = Color.LightGray) // Usando HorizontalDivider diretamente
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.LightGray)
        Text(stringResource(R.string.text_or_login_with), color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = Color.LightGray)
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Botão Google
    OutlinedButton(
        onClick = { /* ação Google */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = Color.White),
        enabled = !isLoading.value
    ) {
        Image(
            painter = painterResource(id = R.drawable.google),
            contentDescription = stringResource(R.string.desc_icon_google),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.label_google), fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}