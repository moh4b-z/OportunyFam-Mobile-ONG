package com.example.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.model.InstituicaoRequest
import com.example.oportunyfam.Service.InstituicaoService
import com.example.oportunyfam.model.Instituicao
import com.example.screens.PrimaryColor
import com.example.screens.RegistroOutlinedTextField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.Response // Importação essencial para isSuccessful e body()

@Composable
fun RegistroContent(
    navController: NavHostController?,
    // Passo 1
    nome: MutableState<String>,
    email: MutableState<String>,
    phone: MutableState<String>,
    tipoInstituicaoId: MutableState<String>,
    cnpj: MutableState<String>,
    // Passo 2
    cep: MutableState<String>,
    logradouro: MutableState<String>,
    numero: MutableState<String>,
    complemento: MutableState<String>,
    bairro: MutableState<String>,
    cidade: MutableState<String>,
    estado: MutableState<String>,
    senha: MutableState<String>,
    confirmarSenha: MutableState<String>,
    concordaTermos: MutableState<Boolean>,
    // Controles
    currentStep: MutableState<Int>,
    isLoading: MutableState<Boolean>,
    errorMessage: MutableState<String?>,
    instituicaoService: InstituicaoService,
    scope: CoroutineScope
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Passo 1
        if (currentStep.value == 1) {
            RegistroOutlinedTextField(
                value = nome.value,
                onValueChange = { nome.value = it },
                label = "Nome da Instituição",
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Nome", tint = Color(0x9E000000)) },
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(10.dp))

            RegistroOutlinedTextField(
                value = email.value,
                onValueChange = { email.value = it },
                label = "Email",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0x9E000000)) },
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(10.dp))

            RegistroOutlinedTextField(
                value = phone.value,
                onValueChange = { phone.value = it },
                label = "Telefone (ex: 11999999999)",
                leadingIcon = { Icon(Icons.Default.Call, contentDescription = "Telefone", tint = Color(0x9E000000)) },
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Tipo de Instituição (Usando ID como String temporário, conforme orientação anterior)
            RegistroOutlinedTextField(
                value = tipoInstituicaoId.value,
                onValueChange = { tipoInstituicaoId.value = it },
                label = "ID do Tipo de Instituição (Ex: 1)",
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = "Instituição", tint = Color(0x9E000000)) },
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = PrimaryColor) },
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(10.dp))

            RegistroOutlinedTextField(
                value = cnpj.value,
                onValueChange = { cnpj.value = it },
                label = "CNPJ",
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = "CNPJ", tint = Color(0x9E000000)) },
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(15.dp))

            // Controles de Navegação do Passo 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "← Voltar",
                    fontSize = 16.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .clickable {
                            if (nome.value.isBlank() || email.value.isBlank() || phone.value.isBlank() || tipoInstituicaoId.value.isBlank() || cnpj.value.isBlank()) {
                                errorMessage.value = "Preencha todos os campos do Passo 1"
                            } else {
                                errorMessage.value = null
                                currentStep.value = 2
                            }
                        },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Avançar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = "Avançar", tint = PrimaryColor)
                }
            }
        }

        // Passo 2
        if (currentStep.value == 2) {
            RegistroOutlinedTextField(
                value = cep.value,
                onValueChange = { cep.value = it },
                label = "CEP",
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "CEP", tint = Color(0x9E000000)) },
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(10.dp))

            RegistroOutlinedTextField(
                value = logradouro.value,
                onValueChange = { logradouro.value = it },
                label = "Logradouro (Rua, Avenida)",
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Logradouro", tint = Color(0x9E000000)) },
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Linha de Número/Complemento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RegistroOutlinedTextField(
                    value = numero.value,
                    onValueChange = { numero.value = it },
                    label = "Nº (Opcional)",
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading.value
                )
                RegistroOutlinedTextField(
                    value = complemento.value,
                    onValueChange = { complemento.value = it },
                    label = "Compl. (Opcional)",
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading.value
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            RegistroOutlinedTextField(
                value = bairro.value,
                onValueChange = { bairro.value = it },
                label = "Bairro",
                leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = "Bairro", tint = Color(0x9E000000)) },
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Linha de Cidade/Estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RegistroOutlinedTextField(
                    value = cidade.value,
                    onValueChange = { cidade.value = it },
                    label = "Cidade",
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading.value
                )
                RegistroOutlinedTextField(
                    value = estado.value,
                    onValueChange = { estado.value = it },
                    label = "Estado (UF)",
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading.value
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            RegistroOutlinedTextField(
                value = senha.value,
                onValueChange = { senha.value = it },
                label = "Senha",
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha", tint = Color(0x9E000000)) },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(10.dp))

            RegistroOutlinedTextField(
                value = confirmarSenha.value,
                onValueChange = { confirmarSenha.value = it },
                label = "Confirmar senha",
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirmar senha", tint = Color(0x9E000000)) },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isLoading.value
            )
            Spacer(modifier = Modifier.height(15.dp))

            // Checkbox de Termos de Serviço
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = concordaTermos.value,
                    onCheckedChange = { concordaTermos.value = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryColor)
                )
                Text(
                    text = "Concorda com os ",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "termos de serviço",
                    color = PrimaryColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* Ação para ver termos */ }
                )
            }
            Spacer(modifier = Modifier.height(15.dp))

            // Controles de Navegação do Passo 2 (Cadastrar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.clickable {
                        errorMessage.value = null
                        currentStep.value = 1
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "← Voltar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryColor
                    )
                }

                Row(
                    modifier = Modifier
                        .clickable(enabled = !isLoading.value) {
                            errorMessage.value = null

                            // Validações
                            if (cep.value.isBlank() || logradouro.value.isBlank() || bairro.value.isBlank() || cidade.value.isBlank() || estado.value.isBlank() || senha.value.isBlank() || confirmarSenha.value.isBlank()) {
                                errorMessage.value = "Preencha todos os campos obrigatórios"
                                return@clickable
                            }
                            if (senha.value != confirmarSenha.value) {
                                errorMessage.value = "As senhas não coincidem"
                                return@clickable
                            }
                            if (!concordaTermos.value) {
                                errorMessage.value = "Você precisa concordar com os termos de serviço"
                                return@clickable
                            }

                            isLoading.value = true

                            scope.launch {
                                try {
                                    val tiposId = tipoInstituicaoId.value.toIntOrNull()
                                    if (tiposId == null) {
                                        errorMessage.value = "ID do Tipo de Instituição inválido. Insira um número."
                                        isLoading.value = false
                                        return@launch
                                    }

                                    // Construindo o Request com TODOS os campos obrigatórios
                                    val request = com.example.model.InstituicaoRequest(
                                        nome = nome.value,
                                        logo = null,
                                        cnpj = cnpj.value,
                                        email = email.value,
                                        senha = senha.value,
                                        descricao = null,
                                        telefone = phone.value.takeIf { it.isNotBlank() }, // Opcional, mas se preenchido
                                        tipos_instituicao = listOf(tiposId),
                                        logradouro = logradouro.value,
                                        numero = numero.value.takeIf { it.isNotBlank() },
                                        complemento = complemento.value.takeIf { it.isNotBlank() },
                                        bairro = bairro.value,
                                        cidade = cidade.value,
                                        estado = estado.value,
                                        cep = cep.value
                                    )

                                    // Chamada ao serviço
                                    val response: Response<Instituicao> = instituicaoService.criar(request)

                                    // Acessando propriedades da Response
                                    if (response.isSuccessful && response.body() != null) {
                                        navController?.navigate("perfil")
                                    } else {
                                        val errorBody = response.errorBody()?.string() ?: response.message()
                                        errorMessage.value = "Falha no Cadastro. Verifique os dados. Erro: $errorBody"
                                    }
                                } catch (e: Exception) {
                                    errorMessage.value = "Erro ao conectar: ${e.message}"
                                } finally {
                                    isLoading.value = false
                                }
                            }
                        },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading.value) {
                        CircularProgressIndicator(
                            color = PrimaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Cadastrar →",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                    }
                }
            }
        }
    }
}
