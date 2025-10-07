package com.example.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.oportunyfam_mobile_ong.R // Ajuste seu import R
import com.example.model.InstituicaoRequest
import com.example.model.TipoInstituicao
import com.example.Components.* // Importa os componentes ViaCEP, CNPJ, MultiSelect
import com.example.oportunyfam.Service.RetrofitFactory
import kotlinx.coroutines.launch

// ==============================================================================
// ⚠️ ATENÇÃO: Verifique e ajuste a localização da sua classe InstituicaoViewModel
// e crie o arquivo se não tiver feito na resposta anterior.
// ==============================================================================
class InstituicaoViewModel : androidx.lifecycle.ViewModel() {
    var nome by mutableStateOf("")
    var email by mutableStateOf("")
    var senha by mutableStateOf("")
    var cnpj by mutableStateOf("") // CNPJ é tratado no componente CnpjTextField
    var telefone by mutableStateOf("")
    var descricao by mutableStateOf("")
    var confirmarSenha by mutableStateOf("")

    // Dados de endereço (preenchidos pelo ViaCEP)
    var cep by mutableStateOf("")
    var logradouro by mutableStateOf("")
    var numero by mutableStateOf("")
    var complemento by mutableStateOf("")
    var bairro by mutableStateOf("")
    var cidade by mutableStateOf("")
    var estado by mutableStateOf("")

    // Lista de IDs de Tipos de Instituição selecionados
    var selectedTipoIds by mutableStateOf(emptyList<Int>())

    // Estado para a lista de tipos disponíveis
    var tiposInstituicao by mutableStateOf(emptyList<TipoInstituicao>())
    var isLoadingTipos by mutableStateOf(false)
}
// ==============================================================================


// ==============================================================================
// CONSTANTES DE LIMITAÇÃO DE ENTRADA
// ==============================================================================
private const val MAX_LENGTH_NOME = 100
private const val MAX_LENGTH_SENHA = 50
private const val MAX_LENGTH_TELEFONE = 15 // Suficiente para máscaras (DD) 9XXXX-XXXX
private const val MAX_LENGTH_ENDERECO = 100
private const val MAX_LENGTH_NUMERO = 10
private const val MAX_LENGTH_UF = 2
private const val MAX_LENGTH_DESCRICAO = 500


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstituicaoRegistroScreen(
    navController: NavHostController?,
    viewModel: InstituicaoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var isSaving by remember { mutableStateOf(false) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // -----------------------------------------------------
    // EFEITO: CARREGAR TIPOS DE INSTITUIÇÃO
    // -----------------------------------------------------
    LaunchedEffect(Unit) {
        viewModel.isLoadingTipos = true
        try {
            val service = RetrofitFactory().getTipoInstituicaoService()
            val response = service.listarTodos().execute()
            if (response.isSuccessful) {
                viewModel.tiposInstituicao = response.body() ?: emptyList()
            } else {
                Toast.makeText(context, "Erro ao carregar tipos de instituição.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Falha de rede ao carregar tipos.", Toast.LENGTH_SHORT).show()
        }
        viewModel.isLoadingTipos = false
    }

    // -----------------------------------------------------
    // FUNÇÃO: SUBMISSÃO DO REGISTRO
    // -----------------------------------------------------
    val handleRegister: () -> Unit = Button@{
        errorMessage.value = null

        // --- Validações de Pré-Envio (melhoradas) ---
        if (viewModel.nome.isBlank() || viewModel.email.isBlank() || viewModel.senha.isBlank() ||
            viewModel.cnpj.isBlank() || viewModel.cep.isBlank() || viewModel.logradouro.isBlank() ||
            viewModel.bairro.isBlank() || viewModel.cidade.isBlank() || viewModel.estado.isBlank()) {
            errorMessage.value = "Preencha todos os campos obrigatórios (incluindo o endereço via CEP)."
            return@Button
        }
        if (viewModel.senha != viewModel.confirmarSenha) {
            errorMessage.value = "As senhas não coincidem."
            return@Button
        }
        if (viewModel.senha.length < 8) {
            errorMessage.value = "A senha deve ter no mínimo 8 caracteres."
            return@Button
        }
        if (viewModel.selectedTipoIds.isEmpty()) {
            errorMessage.value = "Selecione pelo menos um Tipo de Instituição."
            return@Button
        }
        if (viewModel.estado.length != MAX_LENGTH_UF) {
            errorMessage.value = "O campo UF deve ter 2 letras."
            return@Button
        }


        isSaving = true

        scope.launch {
            try {
                // Remove espaços em branco antes de enviar
                val requestBody = InstituicaoRequest(
                    nome = viewModel.nome.trim(),
                    cnpj = viewModel.cnpj.filter { it.isDigit() },
                    email = viewModel.email.trim(),
                    senha = viewModel.senha,
                    descricao = viewModel.descricao.trim().ifBlank { null },
                    telefone = viewModel.telefone.filter { it.isDigit() }.ifBlank { null },
                    tipos_instituicao = viewModel.selectedTipoIds,

                    // Endereço
                    cep = viewModel.cep.filter { it.isDigit() }, // Garante o formato limpo
                    logradouro = viewModel.logradouro.trim(),
                    numero = viewModel.numero.trim().ifBlank { null },
                    complemento = viewModel.complemento.trim().ifBlank { null },
                    bairro = viewModel.bairro.trim(),
                    cidade = viewModel.cidade.trim(),
                    estado = viewModel.estado.trim(),
                    // Lat/Lng NÃO estão na request, pois são calculadas no backend via logradouro ou Viacep
                )

                val service = RetrofitFactory().getInstituicaoService() // Certifique-se que você tem o getInstituicaoService() no seu Factory
                val response = service.criar(requestBody).execute()

                if (response.isSuccessful) {
                    Toast.makeText(context, "Cadastro de Instituição realizado com sucesso!", Toast.LENGTH_LONG).show()
                    navController?.navigate("loginInstituicao") {
                        popUpTo("registroInstituicao") { inclusive = true }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    errorMessage.value = "Falha no cadastro: $errorBody"
                }
            } catch (e: Exception) {
                errorMessage.value = "Erro ao conectar com o servidor: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    // -----------------------------------------------------
    // FUNÇÃO: TRATAMENTO DE SUCESSO DO ViaCEP
    // -----------------------------------------------------
    val onCepSuccess: (data: ViaCepData) -> Unit = { data ->
        viewModel.cep = data.cep.replace("-", "")
        viewModel.logradouro = data.logradouro
        viewModel.bairro = data.bairro
        viewModel.cidade = data.localidade
        viewModel.estado = data.uf
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Instituição") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0x8CFFA500)),
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Detalhes da Instituição", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))

            // Exibição de Erros
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

            // 1. Nome da Instituição
            OutlinedTextField(
                value = viewModel.nome,
                onValueChange = {
                    // Permite APENAS letras e espaços, e limita o tamanho
                    val filteredText = it.filter { char -> char.isLetter() || char.isWhitespace() } // FIX APLICADO AQUI
                    if (filteredText.length <= MAX_LENGTH_NOME) {
                        viewModel.nome = filteredText
                    }
                },
                label = { Text("Nome da Instituição *") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = "Nome") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Máx. $MAX_LENGTH_NOME caracteres. (Apenas letras e espaços)") },
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 2. CNPJ Componente (Validação e máscara embutidas)
            CnpjTextField(
                modifier = Modifier.fillMaxWidth(),
                onValidationSuccess = { viewModel.cnpj = it } // Salva o CNPJ limpo
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 3. Telefone
            OutlinedTextField(
                value = viewModel.telefone,
                onValueChange = {
                    // Permite apenas dígitos e limita o tamanho
                    val filteredText = it.filter { char -> char.isDigit() }
                    if (filteredText.length <= MAX_LENGTH_TELEFONE) {
                        viewModel.telefone = filteredText
                    }
                },
                label = { Text("Telefone (Opcional)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Telefone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Máx. $MAX_LENGTH_TELEFONE dígitos.") },
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 4. Email
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it.take(MAX_LENGTH_NOME) }, // Limita o tamanho
                label = { Text("Email (Será seu Login) *") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next), // Teclado otimizado para email
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 5. Senha e Confirmação
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = viewModel.senha,
                    onValueChange = {
                        if (it.length <= MAX_LENGTH_SENHA) viewModel.senha = it
                    },
                    label = { Text("Senha *") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    visualTransformation = PasswordVisualTransformation(),
                    supportingText = { Text("Mín. 8, Máx. $MAX_LENGTH_SENHA caracteres.") },
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
                OutlinedTextField(
                    value = viewModel.confirmarSenha,
                    onValueChange = {
                        if (it.length <= MAX_LENGTH_SENHA) viewModel.confirmarSenha = it
                    },
                    label = { Text("Confirmar *") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirmar Senha") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))
            Text("Endereço (ViaCEP)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(16.dp))

            // 6. ViaCEP Componente
            CepTextField(
                modifier = Modifier.fillMaxWidth(),
                onValidationSuccess = onCepSuccess // Preenche os campos do ViewModel
            )

            // Campos de endereço detalhados (aparecem após o sucesso do CEP)
            if (viewModel.logradouro.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))

                // Logradouro (Preenchido pelo CEP, mas editável)
                OutlinedTextField(
                    value = viewModel.logradouro,
                    onValueChange = {
                        if (it.length <= MAX_LENGTH_ENDERECO) viewModel.logradouro = it
                    },
                    label = { Text("Logradouro (Rua) *") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Rua") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Máx. $MAX_LENGTH_ENDERECO caracteres.") },
                    enabled = !isSaving
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Número (Input manual)
                    OutlinedTextField(
                        value = viewModel.numero,
                        onValueChange = {
                            // Permite apenas dígitos e limita o tamanho
                            val filteredText = it.filter { char -> char.isDigit() }
                            if (filteredText.length <= MAX_LENGTH_NUMERO) {
                                viewModel.numero = filteredText
                            }
                        },
                        label = { Text("Nº *") },
                        leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = "Número") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                    // Complemento (Input manual)
                    OutlinedTextField(
                        value = viewModel.complemento,
                        onValueChange = {
                            if (it.length <= MAX_LENGTH_ENDERECO) viewModel.complemento = it
                        },
                        label = { Text("Comp. (Opcional)") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.weight(2f),
                        enabled = !isSaving
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                // Bairro, Cidade, Estado (Preenchidos e editáveis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = viewModel.bairro,
                        onValueChange = {
                            if (it.length <= MAX_LENGTH_ENDERECO) viewModel.bairro = it
                        },
                        label = { Text("Bairro *") },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                    OutlinedTextField(
                        value = viewModel.cidade,
                        onValueChange = {
                            if (it.length <= MAX_LENGTH_ENDERECO) viewModel.cidade = it
                        },
                        label = { Text("Cidade *") },
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    )
                    OutlinedTextField(
                        value = viewModel.estado,
                        onValueChange = {
                            // Permite apenas letras e limita o tamanho a 2
                            val filteredText = it.filter { char -> char.isLetter() }.uppercase()
                            if (filteredText.length <= MAX_LENGTH_UF) {
                                viewModel.estado = filteredText
                            }
                        },
                        label = { Text("UF *") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.width(60.dp),
                        supportingText = { Text("2 letras") },
                        enabled = !isSaving
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(24.dp))
            Text("Tipos de Serviço e Descrição", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("(*) Campos obrigatórios", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // 7. Seleção Múltipla de Tipos de Instituição
            Text("Selecione os tipos de atuação da sua Instituição: *", style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.isLoadingTipos) {
                CircularProgressIndicator()
            } else if (viewModel.tiposInstituicao.isNotEmpty()) {
                TipoInstituicaoMultiSelect(
                    tipos = viewModel.tiposInstituicao,
                    onSelectionChanged = { viewModel.selectedTipoIds = it }
                )
            } else {
                Text("Não foi possível carregar os tipos. Verifique a conexão.", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. Descrição
            OutlinedTextField(
                value = viewModel.descricao,
                onValueChange = {
                    if (it.length <= MAX_LENGTH_DESCRICAO) viewModel.descricao = it
                },
                label = { Text("Descrição e Missão (Opcional)") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                supportingText = { Text("Máx. $MAX_LENGTH_DESCRICAO caracteres.") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 9. Botão de Registro
            Button(
                onClick = handleRegister,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                shape = RoundedCornerShape(25.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("REGISTRAR INSTITUIÇÃO", color = Color.White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link para Login
            TextButton(onClick = { navController?.navigate("loginInstituicao") }) {
                Text(
                    text = "Já tem conta? Faça login aqui.",
                    color = Color(0xFFFFA500)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
