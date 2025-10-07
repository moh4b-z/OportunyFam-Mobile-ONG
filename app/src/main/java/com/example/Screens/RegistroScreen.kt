package com.example.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam.Service.RetrofitFactory
import com.example.oportunyfam_mobile_ong.R // Substitua pelo seu R real
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.BorderStroke // CORREÇÃO: Importação para BorderStroke
import com.example.screens.components.LoginContent
import com.example.screens.components.RegistroContent


// Definição de cores
val PrimaryColor = Color(0xFFFFA500)
val BackgroundGray = Color(0xFFE0E0E0)

@Composable
fun RegistroScreen(navController: NavHostController?) {

    // =========================================================================
    // VARIÁVEIS DE ESTADO (Centralizadas aqui para serem passadas aos componentes)
    // Passo 1
    val nome = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val tipoInstituicaoId = remember { mutableStateOf("") }
    val cnpj = remember { mutableStateOf("") }

    // Passo 2
    val cep = remember { mutableStateOf("") }
    val logradouro = remember { mutableStateOf("") }
    val numero = remember { mutableStateOf("") }
    val complemento = remember { mutableStateOf("") }
    val bairro = remember { mutableStateOf("") }
    val cidade = remember { mutableStateOf("") }
    val estado = remember { mutableStateOf("") }
    val senha = remember { mutableStateOf("") }
    val confirmarSenha = remember { mutableStateOf("") }
    val concordaTermos = remember { mutableStateOf(false) }

    // Controles de Tela
    val isRegisterSelected = remember { mutableStateOf(true) }
    val currentStep = remember { mutableStateOf(1) }
    val isLoading = remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val instituicaoService = remember { RetrofitFactory().getInstituicaoService() }
    // =========================================================================

    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem topo
        Image(
            painter = painterResource(id = R.drawable.imglogin),
            contentDescription = "Crianças felizes",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .align(Alignment.TopCenter)
        )

        // Card superior com a mensagem
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 150.dp)
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Max)
                .padding(horizontal = 40.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isRegisterSelected.value) "Crie sua conta e\njunte-se a nós!" else "Bem-vindo de volta!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isRegisterSelected.value) "Estamos felizes em ter você por aqui!" else "Faça login para continuar.",
                    fontSize = 14.sp,
                    color = Color.White
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
                        .height(48.dp)
                        .background(
                            BackgroundGray,
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
                            .background(if (!isRegisterSelected.value) Color.White else BackgroundGray)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isRegisterSelected.value = false
                                errorMessage.value = null
                                currentStep.value = 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Login",
                            fontSize = 16.sp,
                            color = if (!isRegisterSelected.value) PrimaryColor else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botão Registro
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(25.dp))
                            .background(if (isRegisterSelected.value) Color.White else BackgroundGray)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                isRegisterSelected.value = true
                                errorMessage.value = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Registre-se",
                            fontSize = 16.sp,
                            color = if (isRegisterSelected.value) PrimaryColor else Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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

                // ================== CONTEÚDO DINÂMICO ==================
                if (!isRegisterSelected.value) {
                    LoginContent(
                        navController = navController,
                        email = email,
                        senha = senha,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        instituicaoService = instituicaoService,
                        scope = scope
                    )
                } else {
                    RegistroContent(
                        navController = navController,
                        // Passo 1
                        nome = nome,
                        email = email,
                        phone = phone,
                        tipoInstituicaoId = tipoInstituicaoId,
                        cnpj = cnpj,
                        // Passo 2
                        cep = cep,
                        logradouro = logradouro,
                        numero = numero,
                        complemento = complemento,
                        bairro = bairro,
                        cidade = cidade,
                        estado = estado,
                        senha = senha,
                        confirmarSenha = confirmarSenha,
                        concordaTermos = concordaTermos,
                        // Controles
                        currentStep = currentStep,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        instituicaoService = instituicaoService,
                        scope = scope
                    )
                }
            }
        }
    }
}

// Composable de Text Field Padronizado
@Composable
fun RegistroOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit = {}, // CORREÇÃO: Tipo de retorno Unit (não Unit?) e valor padrão {} (função vazia)
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        // Se trailingIcon for nulo, precisamos usar um if para não passar null para o composable
        leadingIcon = leadingIcon.takeIf { it != {} },
        trailingIcon = trailingIcon,
        label = { Text(label, color = Color.Gray) },
        visualTransformation = visualTransformation,
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color.LightGray,
            disabledBorderColor = Color.LightGray,
            cursorColor = PrimaryColor,
            focusedLabelColor = PrimaryColor,
            unfocusedLabelColor = Color.Gray
        )
    )
}

@Preview(showSystemUi = true)
@Composable
fun RegistroScreenPreview() {
    RegistroScreen(navController = null)
}