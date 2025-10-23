package com.example.Telas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.Components.BarraTarefas
import com.example.data.InstituicaoAuthDataStore
import com.example.oportunyfam.Service.RetrofitFactory
import com.example.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile.model.Instituicao
import com.oportunyfam_mobile.model.InstituicaoRequest
import kotlinx.coroutines.launch

// 🔽 NOVOS IMPORTS PARA IMAGEM 🔽
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import android.net.Uri
import androidx.compose.foundation.clickable
import com.example.Service.AzureBlobRetrofit
import com.example.model.getRealPathFromURI
import com.oportunyfam_mobile.model.InstituicaoAtualizarRequest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavHostController?
) {
    val context = LocalContext.current
    val instituicaoAuthDataStore = remember { InstituicaoAuthDataStore(context) }
    val scope = rememberCoroutineScope()

    // Estado para armazenar os dados da instituição logada
    var instituicao by remember { mutableStateOf<Instituicao?>(null) }
    val isLoadingData = remember { mutableStateOf(true) }

    // Estado para controlar a exibição do diálogo de edição
    var showEditDialog by remember { mutableStateOf(false) }

    // Estado para a nova descrição
    var novaDescricao by remember { mutableStateOf("") }

    // Estado para controlar o carregamento durante a atualização
    var isLoadingUpdate by remember { mutableStateOf(false) }

    // Estado para mensagens de erro/sucesso
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    // 🔽 NOVOS ESTADOS PARA IMAGEM 🔽
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageFile by remember { mutableStateOf<File?>(null) }

    // ----------------------------------------------------
    // FUNÇÃO PARA FAZER UPLOAD E ATUALIZAR IMAGEM (DECLARAR PRIMEIRO!)
    // ----------------------------------------------------
    val uploadAndUpdateProfileImage: () -> Unit = {
        tempImageFile?.let { imageFile ->
            isLoadingUpdate = true
            scope.launch {
                try {
                    // 🔽 CONFIGURAR COM SEUS DADOS DO AZURE 🔽
                    val storageAccount = "sua-conta-storage"
                    val sasToken = "seu-token-sas"
                    val containerName = "imagens-perfil"

                    // Fazer upload para Azure
                    val imageUrl = AzureBlobRetrofit.uploadImageToAzure(
                        imageFile,
                        storageAccount,
                        sasToken,
                        containerName
                    )

                    if (imageUrl != null && instituicao != null) {
                        // Atualizar na API
                        val instituicaoService = RetrofitFactory().getInstituicaoService()
                        val currentInstituicao = instituicao!!

                        val updateRequest = InstituicaoAtualizarRequest(
                            nome = currentInstituicao.nome,
                            logo = imageUrl, // NOVA URL DA IMAGEM\\
                            cnpj = currentInstituicao.cnpj,
                            telefone = currentInstituicao.telefone,
                            email = currentInstituicao.email,
                            descricao = currentInstituicao.descricao ?: "",
                        )

                        val response = instituicaoService.atualizar(currentInstituicao.id, updateRequest)

                        if (response.isSuccessful) {
                            // Atualizar estado local
                            val updatedInstituicao = currentInstituicao.copy(logo = imageUrl)
                            instituicao = updatedInstituicao
                            instituicaoAuthDataStore.saveInstituicao(updatedInstituicao)

                            snackbarMessage = "Imagem de perfil atualizada com sucesso!"
                            showSnackbar = true
                        } else {
                            snackbarMessage = "Erro ao atualizar perfil: ${response.code()}"
                            showSnackbar = true
                        }
                    } else {
                        snackbarMessage = "Erro ao fazer upload da imagem"
                        showSnackbar = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    snackbarMessage = "Erro: ${e.message}"
                    showSnackbar = true
                } finally {
                    isLoadingUpdate = false
                    tempImageFile = null
                    selectedImageUri = null
                }
            }
        }
    }

    // ----------------------------------------------------
    // LAUNCHER PARA SELECIONAR IMAGEM (DECLARAR DEPOIS DA FUNÇÃO)
    // ----------------------------------------------------
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Converter URI para File
            val filePath = context.getRealPathFromURI(it)
            filePath?.let { path ->
                tempImageFile = File(path)
                uploadAndUpdateProfileImage() // ✅ AGORA A FUNÇÃO JÁ ESTÁ DECLARADA
            }
        }
    }

    // ----------------------------------------------------
    // FUNÇÃO PARA ABRIR SELETOR DE IMAGEM
    // ----------------------------------------------------
    val openImagePicker: () -> Unit = {
        imagePickerLauncher.launch("image/*")
    }

    // ----------------------------------------------------
    // FUNÇÃO PARA LOGOUT E NAVEGAÇÃO
    // ----------------------------------------------------
    val onLogout: () -> Unit = {
        scope.launch {
            instituicaoAuthDataStore.logout()
            navController?.navigate("login") {
                popUpTo(navController.graph.id) {
                    inclusive = true
                }
            }
        }
    }

    // ----------------------------------------------------
    // FUNÇÃO PARA EDITAR DESCRIÇÃO
    // ----------------------------------------------------
    val onEditDescription: () -> Unit = {
        novaDescricao = instituicao?.descricao ?: ""
        showEditDialog = true
    }

    // ----------------------------------------------------
    // FUNÇÃO PARA SALVAR DESCRIÇÃO NA API
    // ----------------------------------------------------
    val onSaveDescription: () -> Unit = {
        if (novaDescricao.isNotBlank() && instituicao != null) {
            isLoadingUpdate = true
            scope.launch {
                try {
                    val instituicaoService = RetrofitFactory().getInstituicaoService()
                    val currentInstituicao = instituicao!!

                    // CORREÇÃO: Usar dados reais do endereço em vez de strings vazias
                    val endereco = currentInstituicao.endereco

                    // Criar o request para atualização com dados reais
                    val updateRequest = InstituicaoAtualizarRequest(
                        nome = currentInstituicao.nome,
                        logo = currentInstituicao.logo,
                        cnpj = currentInstituicao.cnpj,
                        telefone = currentInstituicao.telefone,
                        email = currentInstituicao.email,
                        descricao = novaDescricao
                    )

                    val response = instituicaoService.atualizar(currentInstituicao.id, updateRequest)

                    if (response.isSuccessful) {
                        // Atualiza o estado local com a nova descrição
                        val updatedInstituicao = currentInstituicao.copy(descricao = novaDescricao)
                        instituicao = updatedInstituicao

                        // Atualiza também no DataStore
                        instituicaoAuthDataStore.saveInstituicao(updatedInstituicao)

                        showEditDialog = false
                        snackbarMessage = "Descrição atualizada com sucesso!"
                        showSnackbar = true
                    } else {
                        // Tratar erro
                        snackbarMessage = "Erro ao atualizar: ${response.code()}"
                        showSnackbar = true
                        println("Erro ao atualizar: ${response.code()}")
                        println("Mensagem de erro: ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    snackbarMessage = "Erro de conexão: ${e.message}"
                    showSnackbar = true
                } finally {
                    isLoadingUpdate = false
                }
            }
        }
    }

    // Efeito para carregar os dados do DataStore quando a tela é iniciada
    LaunchedEffect(Unit) {
        scope.launch {
            instituicao = instituicaoAuthDataStore.loadInstituicao()
            isLoadingData.value = false
        }
    }

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),
            Color(0xFFFFD27A)
        )
    )

    // Se os dados estiverem carregando, exibe um indicador
    if (isLoadingData.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFFFFA000))
        }
        return
    }

    // Se não houver dados logados, redireciona
    if (instituicao == null) {
        LaunchedEffect(Unit) {
            onLogout()
        }
        return
    }

    // Dados extraídos da variável de estado
    val instituicaoNome = instituicao?.nome ?: "Instituição Não Encontrada"
    val instituicaoEmail = instituicao?.email ?: "email@exemplo.com"

    // Snackbar para mostrar mensagens
    if (showSnackbar) {
        LaunchedEffect(showSnackbar) {
            // Auto-dismiss após 3 segundos
            kotlinx.coroutines.delay(3000)
            showSnackbar = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showSnackbar = false }) {
                        Text("OK", color = Color.White)
                    }
                }
            ) {
                Text(snackbarMessage, color = Color.White)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onLogout) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Sair", tint = Color.Black)
            }

            IconButton(onClick = { /* Ação para Notificações */ }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificações", tint = Color.Black)
            }
            IconButton(onClick = { /* Ação para Menu */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
            }
        }

        Divider(color = Color.LightGray, thickness = 1.5.dp)

        // Conteúdo principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Card branco
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 90.dp)
                ) {
                    // Informações do perfil
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Nome da Instituição
                        Text(
                            instituicaoNome,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Email da Instituição
                        Text(
                            instituicaoEmail,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats (FOLLOWING)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "127",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "FOLLOWING",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Imagem da Instituição
                            Image(
                                painter = painterResource(id = R.drawable.instituicao),
                                contentDescription = "Imagem Perfil da Instituição",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Descrição da Instituição com botão de editar
                            Box(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    instituicao?.descricao ?: "Nenhuma descrição disponível. Clique para editar seu perfil.",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 20.sp
                                )

                                // Botão de editar flutuante
                                IconButton(
                                    onClick = onEditDescription,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Editar descrição",
                                        tint = Color(0xFFFFA000),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Divisor
                        Divider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // 🔽 IMAGEM DE PERFIL CENTRALIZADA (MODIFICADA) 🔽
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset(y = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(70.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.clickable {
                        if (!isLoadingUpdate) {
                            openImagePicker()
                        }
                    }
                ) {
                    // Mostrar imagem temporária se selecionada, senão mostrar a atual
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "Nova imagem de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(150.dp)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.perfil),
                            contentDescription = "Imagem Perfil Pessoal",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(150.dp)
                        )
                    }

                    // Mostrar loading durante upload
                    if (isLoadingUpdate) {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .background(Color.Black.copy(alpha = 0.5F)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                }

                // Texto indicativo
                Text(
                    "Clique para alterar a foto",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Barra de Tarefas
        BarraTarefas()
    }

    // Diálogo de edição de descrição
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoadingUpdate) {
                    showEditDialog = false
                }
            },
            title = { Text("Editar Descrição") },
            text = {
                Column {
                    Text("Digite a nova descrição da sua instituição:")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = novaDescricao,
                        onValueChange = { novaDescricao = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Descreva sua instituição...") },
                        maxLines = 5,
                        enabled = !isLoadingUpdate
                    )
                }
            },
            confirmButton = {
                if (isLoadingUpdate) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    TextButton(
                        onClick = onSaveDescription,
                        enabled = novaDescricao.isNotBlank() && !isLoadingUpdate
                    ) {
                        Text("Salvar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false },
                    enabled = !isLoadingUpdate
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    PerfilScreen(
        navController = null
    )
}