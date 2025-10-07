package com.example.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam_mobile_ong.R
import com.example.service.RetrofitFactory
import com.example.model.LoginRequest
import com.example.model.RegistroRequest
import kotlinx.coroutines.launch

@Composable
fun RegistroScreen(navController: NavHostController?) {

    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val number = remember { mutableStateOf("") }
    val tipoInstituicao = remember { mutableStateOf("") }
    val cpf = remember { mutableStateOf("") }
    val cep = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val confirmarSenha = remember { mutableStateOf("") }
    val isRegisterSelected = remember { mutableStateOf(true) }
    val currentStep = remember { mutableStateOf(1) }

    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val usuarioService = remember { RetrofitFactory().getUsuarioService() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem topo
        Image(
            painter = painterResource(id = R.drawable.imglogin),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
        )

        // Card sobre a imagem
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(120.dp)
                .offset(y = 140.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x8CFFA500))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isRegisterSelected.value) "Crie sua conta e junte-se a nós!" else "Seja bem-vindo novamente",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRegisterSelected.value) "Estamos felizes em ter você por aqui!" else "Entre em sua conta para usar os nossos serviços",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        // Card principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.68f)
                .align(Alignment.BottomCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                // Botões Login / Registro
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(25.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Botão Login
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                if (!isRegisterSelected.value) Color.White else Color(0xFFE0E0E0)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) {
                                isRegisterSelected.value = false
                                errorMessage.value = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botão Registro
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(25.dp))
                            .background(
                                if (isRegisterSelected.value) Color.White else Color(0xFFE0E0E0)
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current
                            ) {
                                isRegisterSelected.value = true
                                errorMessage.value = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Registre-se",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                errorMessage.value?.let { error ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // ================== LOGIN ==================
                if (!isRegisterSelected.value) {
                    OutlinedTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "", tint = Color(0x9E000000)) },
                        label = { Text("Email") },
                        enabled = !isLoading.value
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = senha.value,
                        onValueChange = { senha.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "", tint = Color(0x9E000000)) },
                        label = { Text("Senha") },
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
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFFA500))
                            )
                            Text("Lembrar-me", color = Color.Gray, fontSize = 14.sp)
                        }
                        TextButton(onClick = { /* esqueceu senha */ }) {
                            Text("Esqueceu sua senha?", color = Color(0xFFFFA500), fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            errorMessage.value = null

                            // Validações
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

                                    val response = usuarioService.login(request)

                                    if (response.isSuccessful && response.body() != null) {
                                        val loginResponse = response.body()!!
                                        // Login bem-sucedido - navegar para tela principal
                                        navController?.navigate("perfil")
                                    } else {
                                        errorMessage.value = "Email ou senha incorretos"
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
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
                }

                // ================== REGISTRO ==================
                if (isRegisterSelected.value) {
                    // Passo 1
                    if (currentStep.value == 1) {
                        OutlinedTextField(
                            value = nome.value,
                            onValueChange = { nome.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Nome") },
                            enabled = !isLoading.value
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = email.value,
                            onValueChange = { email.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Email") },
                            enabled = !isLoading.value
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = number.value,
                            onValueChange = { number.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Contato") },
                            enabled = !isLoading.value
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = tipoInstituicao.value,
                            onValueChange = { tipoInstituicao.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Tipo de Instituição") },
                            enabled = !isLoading.value
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (nome.value.isBlank() || email.value.isBlank() ||
                                        number.value.isBlank() || tipoInstituicao.value.isBlank()) {
                                        errorMessage.value = "Preencha todos os campos antes de prosseguir"
                                    } else {
                                        errorMessage.value = null
                                        currentStep.value = 2
                                    }
                                },
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prosseguir",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFA500)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "", tint = Color(0xFFFFA500))
                        }
                    }

                    // Passo 2
                    if (currentStep.value == 2) {
                        OutlinedTextField(
                            value = cpf.value,
                            onValueChange = { cpf.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("CNPJ") },
                            enabled = !isLoading.value
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cep.value,
                            onValueChange = { cep.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("CEP") },
                            enabled = !isLoading.value
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = senha.value,
                            onValueChange = { senha.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Digite sua senha") },
                            enabled = !isLoading.value
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = confirmarSenha.value,
                            onValueChange = { confirmarSenha.value = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "", tint = Color(0x9E000000)) },
                            label = { Text("Confirme sua senha") },
                            enabled = !isLoading.value
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Button(
                            onClick = {
                                errorMessage.value = null

                                // Validações
                                if (cpf.value.isBlank() || cep.value.isBlank() ||
                                    senha.value.isBlank() || confirmarSenha.value.isBlank()) {
                                    errorMessage.value = "Preencha todos os campos"
                                    return@Button
                                }

                                if (senha.value != confirmarSenha.value) {
                                    errorMessage.value = "As senhas não coincidem"
                                    return@Button
                                }

                                if (senha.value.length < 6) {
                                    errorMessage.value = "A senha deve ter no mínimo 6 caracteres"
                                    return@Button
                                }

                                isLoading.value = true

                                scope.launch {
                                    try {
                                        val request = RegistroRequest(
                                            nome = nome.value,
                                            email = email.value,
                                            telefone = number.value,
                                            tipoInstituicao = tipoInstituicao.value,
                                            cnpj = cpf.value,
                                            cep = cep.value,
                                            senha = senha.value
                                        )

                                        val response = usuarioService.registrar(request)

                                        if (response.isSuccessful && response.body() != null) {
                                            val registroResponse = response.body()!!
                                            // Registro bem-sucedido - navegar para tela principal
                                            navController?.navigate("perfil")
                                        } else {
                                            errorMessage.value = "Erro ao cadastrar: ${response.message()}"
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                            shape = RoundedCornerShape(25.dp),
                            enabled = !isLoading.value
                        ) {
                            if (isLoading.value) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text("Cadastrar", color = Color.White, fontSize = 16.sp)
                            }
                        }
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
                Box(
                    modifier = Modifier
                        .background(Color.White, shape = RoundedCornerShape(25.dp))
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable { /* ação Google */ },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    RegistroScreen(navController = null)
}
