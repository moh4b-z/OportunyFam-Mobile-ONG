package com.example.screens.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke // Importação adicionada/confirmada para BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam.Service.InstituicaoService
import com.example.oportunyfam.model.Instituicao
import com.example.oportunyfam_mobile_ong.R
import com.example.oportunyfam.model.LoginRequest
import com.example.screens.PrimaryColor
import com.example.screens.RegistroOutlinedTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response // Importação essencial para isSuccessful e body()

@Composable
fun LoginContent(
    navController: NavHostController?,
    email: MutableState<String>,
    senha: MutableState<String>,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    instituicaoService: InstituicaoService,
    scope: CoroutineScope
) {
    // Email
    RegistroOutlinedTextField(
        value = email.value,
        onValueChange = { email.value = it },
        label = "Email",
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "", tint = Color(0x9E000000)) },
        enabled = !isLoading.value
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Senha
    RegistroOutlinedTextField(
        value = senha.value,
        onValueChange = { senha.value = it },
        label = "Senha",
        visualTransformation = PasswordVisualTransformation(),
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "", tint = Color(0x9E000000)) },
        enabled = !isLoading.value
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
                colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
            )
            Text("Lembrar-me", color = Color.Gray, fontSize = 14.sp)
        }
        TextButton(onClick = { /* esqueceu senha */ }) {
            Text("Esqueceu sua senha?", color = PrimaryColor, fontSize = 14.sp)
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
                    val request = com.example.oportunyfam.model.LoginRequest(
                        email = email.value,
                        senha = senha.value
                    )

                    // Chamada ao serviço
                    val response: Response<Instituicao> = instituicaoService.loginInstituicao(request)

                    // Acessando propriedades da Response
                    if (response.isSuccessful && response.body() != null) {
                        navController?.navigate("perfil")
                    } else {
                        val errorBody = response.errorBody()?.string() ?: response.message()
                        errorMessage.value = "Falha no Login. Verifique suas credenciais. Erro: $errorBody"
                    }
                } catch (e: Exception) {
                    errorMessage.value = "Erro ao conectar com o servidor: ${e.message}"
                } finally {
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
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text("Login", color = Color.White, fontSize = 16.sp)
        }
    }

    Spacer(modifier = Modifier.height(15.dp))

    // Divider "Ou entre com"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.weight(1f))
        Text("Ou entre com", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
        Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Botão Google
    OutlinedButton(
        onClick = { /* ação Google */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, Color.LightGray), // BorderStroke agora funciona
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black, containerColor = Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.google),
            contentDescription = "Logo Google",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Google", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
