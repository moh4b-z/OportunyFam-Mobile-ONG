package com.example.Screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.Components.BarraTarefas
import com.example.MainActivity.NavRoutes
import com.example.Service.AzureBlobRetrofit
import com.example.data.InstituicaoAuthDataStore
import com.example.model.getRealPathFromURI
import com.example.oportunyfam.Service.RetrofitFactory
import com.example.oportunyfam_mobile_ong.R
import com.example.viewmodel.PublicacaoViewModel
import com.example.viewmodel.PublicacoesState
import com.example.viewmodel.CriarPublicacaoState
import com.oportunyfam_mobile.model.InstituicaoAtualizarRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

// ============================================
// SCREEN PRINCIPAL
// ============================================


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(navController: NavHostController?) {
    // ============================================
    // INICIALIZAÇÃO E ESTADOS
    // ============================================
    val context = LocalContext.current
    val instituicaoAuthDataStore = remember { InstituicaoAuthDataStore(context) }
    val scope = rememberCoroutineScope()

    val instituicao by instituicaoAuthDataStore.instituicaoStateFlow().collectAsState(initial = null)

    // ViewModel de Publicações
    val publicacaoViewModel: PublicacaoViewModel = viewModel()
    val publicacoesState by publicacaoViewModel.publicacoesState.collectAsState()
    val criarPublicacaoState by publicacaoViewModel.criarPublicacaoState.collectAsState()

    var isLoadingData by remember { mutableStateOf(false) }
    var isLoadingUpdate by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var novaDescricao by remember { mutableStateOf("") }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var tempImageFile by remember { mutableStateOf<File?>(null) }

    // Estados para publicação
    var showPublicacaoDialog by remember { mutableStateOf(false) }
    var publicacaoDescricao by remember { mutableStateOf("") }
    var publicacaoImageFile by remember { mutableStateOf<File?>(null) }
    var isUploadingPublicacao by remember { mutableStateOf(false) }


    // ============================================
    // CARREGAMENTO INICIAL
    // ============================================
    LaunchedEffect(Unit) {
        if (instituicao == null) {
            Log.d("PerfilScreen", "Carregando dados da instituição...")
            isLoadingData = true
            try {
                instituicaoAuthDataStore.loadInstituicao()
            } catch (e: Exception) {
                Log.e("PerfilScreen", "Erro ao carregar dados: ${e.message}", e)
            } finally {
                isLoadingData = false
            }
        }
    }

    // Carregar publicações quando instituição estiver disponível
    LaunchedEffect(instituicao) {
        instituicao?.let {
            Log.d("PerfilScreen", "🔍 Carregando publicações da instituição: ${it.instituicao_id}")
            publicacaoViewModel.buscarPublicacoesPorInstituicao(it.instituicao_id)
        }
    }

    // Observar estado de criação de publicação
    LaunchedEffect(criarPublicacaoState) {
        when (criarPublicacaoState) {
            is CriarPublicacaoState.Success -> {
                snackbarMessage = "Publicação criada com sucesso!"
                showSnackbar = true
                publicacaoViewModel.limparEstadoCriacao()
            }
            is CriarPublicacaoState.Error -> {
                snackbarMessage = (criarPublicacaoState as CriarPublicacaoState.Error).message
                showSnackbar = true
                publicacaoViewModel.limparEstadoCriacao()
            }
            else -> {}
        }
    }

    // ============================================
    // FUNÇÕES DE NEGÓCIO
    // ============================================
    val uploadAndUpdateProfileImage: () -> Unit = {
        tempImageFile?.let { imageFile ->
            isLoadingUpdate = true
            scope.launch {
                try {
                    val accountKey = com.example.Config.AzureConfig.getStorageKey()

                    Log.d("PerfilScreen", "Iniciando upload da imagem...")

                    val imageUrl = AzureBlobRetrofit.uploadImageToAzure(
                        imageFile,
                        com.example.Config.AzureConfig.STORAGE_ACCOUNT,
                        accountKey,
                        com.example.Config.AzureConfig.CONTAINER_PERFIL
                    )

                    Log.d("PerfilScreen", "Upload retornou URL: $imageUrl")

                    if (imageUrl != null && instituicao != null) {
                        val instituicaoService = RetrofitFactory().getInstituicaoService()
                        val currentInstituicao = instituicao!!
                        val versionedUrl = "$imageUrl?v=${System.currentTimeMillis()}"

                        val updateRequest = InstituicaoAtualizarRequest(
                            nome = currentInstituicao.nome,
                            foto_perfil = versionedUrl,
                            cnpj = currentInstituicao.cnpj,
                            telefone = currentInstituicao.telefone,
                            email = currentInstituicao.email,
                            descricao = currentInstituicao.descricao ?: ""
                        )

                        val response = instituicaoService.atualizar(currentInstituicao.instituicao_id, updateRequest)

                        when {
                            response.isSuccessful -> {
                                Log.d("PerfilScreen", "Foto de perfil atualizada com sucesso!")
                                val updatedInstituicao = currentInstituicao.copy(foto_perfil = versionedUrl)
                                instituicaoAuthDataStore.saveInstituicao(updatedInstituicao)
                                snackbarMessage = "Foto de perfil atualizada com sucesso!"
                                showSnackbar = true
                            }
                            response.code() == 429 -> {
                                Log.w("PerfilScreen", "Rate limit - salvando localmente")
                                val updatedInstituicao = currentInstituicao.copy(foto_perfil = versionedUrl)
                                instituicaoAuthDataStore.saveInstituicao(updatedInstituicao)
                                snackbarMessage = "Foto salva! Servidor ocupado, sincronizará depois."
                                showSnackbar = true
                            }
                            else -> {
                                snackbarMessage = "Erro ao atualizar (${response.code()})"
                                showSnackbar = true
                            }
                        }
                    } else {
                        snackbarMessage = "Erro ao fazer upload da imagem"
                        showSnackbar = true
                    }
                } catch (e: Exception) {
                    Log.e("PerfilScreen", "Erro no upload: ${e.message}", e)
                    snackbarMessage = "Erro: ${e.message}"
                    showSnackbar = true
                } finally {
                    isLoadingUpdate = false
                    tempImageFile = null
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("PerfilScreen", "Imagem selecionada: $it")

            val filePath = context.getRealPathFromURI(it)
            filePath?.let { path ->
                tempImageFile = File(path)
                Log.d("PerfilScreen", "Arquivo preparado: ${tempImageFile?.name}")
                uploadAndUpdateProfileImage()
            } ?: run {
                snackbarMessage = "Erro ao processar a imagem"
                showSnackbar = true
            }
        }
    }

    // Launcher para selecionar imagem de publicação
    val publicacaoImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("PerfilScreen", "Imagem de publicação selecionada: $it")
            val filePath = context.getRealPathFromURI(it)
            filePath?.let { path ->
                publicacaoImageFile = File(path)
                Log.d("PerfilScreen", "Arquivo de publicação preparado: ${publicacaoImageFile?.name}")
            } ?: run {
                snackbarMessage = "Erro ao processar a imagem"
                showSnackbar = true
            }
        }
    }

    val onLogout: () -> Unit = {
        scope.launch {
            instituicaoAuthDataStore.logout()
            navController?.navigate(NavRoutes.REGISTRO) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    val onEditDescription: () -> Unit = {
        novaDescricao = instituicao?.descricao ?: ""
        showEditDialog = true
    }

    val onSaveDescription: () -> Unit = {
        if (novaDescricao.isNotBlank() && instituicao != null) {
            isLoadingUpdate = true
            scope.launch {
                try {
                    val instituicaoService = RetrofitFactory().getInstituicaoService()
                    val currentInstituicao = instituicao!!

                    val updateRequest = InstituicaoAtualizarRequest(
                        nome = currentInstituicao.nome,
                        foto_perfil = currentInstituicao.foto_perfil,
                        cnpj = currentInstituicao.cnpj,
                        telefone = currentInstituicao.telefone,
                        email = currentInstituicao.email,
                        descricao = novaDescricao
                    )

                    val response = instituicaoService.atualizar(currentInstituicao.instituicao_id, updateRequest)

                    if (response.isSuccessful) {
                        val updatedInstituicao = currentInstituicao.copy(descricao = novaDescricao)
                        instituicaoAuthDataStore.saveInstituicao(updatedInstituicao)
                        showEditDialog = false
                        snackbarMessage = "Descrição atualizada com sucesso!"
                        showSnackbar = true
                    } else {
                        snackbarMessage = "Erro ao atualizar: ${response.code()}"
                        showSnackbar = true
                    }
                } catch (e: Exception) {
                    snackbarMessage = "Erro de conexão: ${e.message}"
                    showSnackbar = true
                } finally {
                    isLoadingUpdate = false
                }
            }
        }
    }

    // ============================================
    // ESTADOS DE CARREGAMENTO
    // ============================================
    if (isLoadingData) {
        LoadingScreen()
        return
    }

    if (instituicao == null) {
        LaunchedEffect(Unit) {
            delay(3000)
            if (instituicao == null) {
                Log.w("PerfilScreen", "Timeout - redirecionando para login")
                onLogout()
            }
        }
        LoadingScreen(message = "Carregando perfil...")
        return
    }

    // ============================================
    // UI PRINCIPAL
    // ============================================
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),
            Color(0xFFFFD27A)
        )
    )

    // Dados da instituição
    val instituicaoNome = instituicao?.nome ?: "Instituição Não Encontrada"
    val instituicaoEmail = instituicao?.email ?: "email@exemplo.com"

    // Snackbar para mostrar mensagens
    if (showSnackbar) {
        LaunchedEffect(showSnackbar) {
            delay(3000)
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onLogout) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sair", tint = Color.Black)
            }

            IconButton(onClick = { /* Ação para Notificações */ }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificações", tint = Color.Black)
            }
            IconButton(onClick = { /* Ação para Menu */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
            }
        }



        Spacer(modifier = Modifier.height(60.dp))

        // Conteúdo principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Informações do perfil
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Foto de Perfil da Instituição (VEM DA API)
                    Box(
                        modifier = Modifier.size(120.dp)
                    ) {
                        Card(
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            val fotoPerfilUrl = instituicao?.foto_perfil

                            if (!fotoPerfilUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(fotoPerfilUrl)
                                        .crossfade(true)
                                        .diskCachePolicy(CachePolicy.DISABLED)
                                        .memoryCachePolicy(CachePolicy.DISABLED)
                                        .build(),
                                    contentDescription = "Foto de perfil da instituição",
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.perfil),
                                    error = painterResource(id = R.drawable.perfil),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.perfil),
                                    contentDescription = "Foto de perfil padrão",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Botão de câmera para alterar foto de perfil
                        FloatingActionButton(
                            onClick = { if (!isLoadingUpdate) imagePickerLauncher.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(36.dp)
                                .offset(x = 4.dp, y = 4.dp),
                            containerColor = Color(0xFFFFA000),
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            if (isLoadingUpdate) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Alterar foto de perfil",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nome da Instituição
                    Text(
                        instituicaoNome,
                        fontSize = 24.sp,
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // Descrição da Instituição com botão de editar
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            instituicao?.descricao ?: "Nenhuma descrição disponível. Clique no ícone para editar.",
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(end = 32.dp)
                        )

                        // Botão de editar
                        IconButton(
                            onClick = onEditDescription,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Editar descrição",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Divisor
                    HorizontalDivider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Seção de Publicações (Fotos da Instituição)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Publicações",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        // Botão Adicionar Publicação
                        FloatingActionButton(
                            onClick = { showPublicacaoDialog = true },
                            modifier = Modifier.size(48.dp),
                            containerColor = Color(0xFFFFA000),
                            contentColor = Color.White
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Adicionar Publicação",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid de Publicações da API
                    PublicacoesGrid(
                        publicacoesState = publicacoesState,
                        onDeletePublicacao = { publicacaoId ->
                            instituicao?.let {
                                publicacaoViewModel.deletarPublicacao(publicacaoId, it.instituicao_id)
                            }
                        }
                    )
                }
            }
        }

        // Barra de Tarefas
        BarraTarefas(
            navController = navController,
            currentRoute = NavRoutes.PERFIL
        )
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

    // Diálogo de criar publicação
    if (showPublicacaoDialog) {
        CriarPublicacaoDialog(
            descricao = publicacaoDescricao,
            imagemSelecionada = publicacaoImageFile != null,
            isLoading = isUploadingPublicacao,
            onDescricaoChange = { publicacaoDescricao = it },
            onSelecionarImagem = { publicacaoImagePickerLauncher.launch("image/*") },
            onSalvar = {
                if (publicacaoDescricao.trim().length >= 30 && publicacaoImageFile != null && instituicao != null) {
                    isUploadingPublicacao = true
                    scope.launch {
                        try {
                            val accountKey = com.example.Config.AzureConfig.getStorageKey()

                            Log.d("PerfilScreen", "📤 Fazendo upload da imagem da publicação...")

                            val imageUrl = AzureBlobRetrofit.uploadImageToAzure(
                                publicacaoImageFile!!,
                                com.example.Config.AzureConfig.STORAGE_ACCOUNT,
                                accountKey,
                                com.example.Config.AzureConfig.CONTAINER_PERFIL
                            )

                            if (imageUrl != null) {
                                Log.d("PerfilScreen", "✅ Upload concluído: $imageUrl")
                                Log.d("PerfilScreen", "📝 Criando publicação na API...")

                                publicacaoViewModel.criarPublicacao(
                                    descricao = publicacaoDescricao,
                                    imagem = imageUrl,
                                    instituicaoId = instituicao!!.instituicao_id
                                )

                                // Limpar e fechar
                                publicacaoDescricao = ""
                                publicacaoImageFile = null
                                showPublicacaoDialog = false
                            } else {
                                snackbarMessage = "Erro ao fazer upload da imagem"
                                showSnackbar = true
                            }
                        } catch (e: Exception) {
                            Log.e("PerfilScreen", "❌ Erro ao criar publicação", e)
                            snackbarMessage = "Erro: ${e.message}"
                            showSnackbar = true
                        } finally {
                            isUploadingPublicacao = false
                        }
                    }
                }
            },
            onDismiss = {
                if (!isUploadingPublicacao) {
                    showPublicacaoDialog = false
                    publicacaoDescricao = ""
                    publicacaoImageFile = null
                }
            }
        )
    }
}

// ============================================
// COMPONENTES DE PUBLICAÇÕES
// ============================================

@Composable
private fun PublicacoesGrid(
    publicacoesState: PublicacoesState,
    onDeletePublicacao: (Int) -> Unit
) {
    when (publicacoesState) {
        is PublicacoesState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFFA000))
            }
        }
        is PublicacoesState.Success -> {
            val publicacoes = publicacoesState.publicacoes

            if (publicacoes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nenhuma publicação ainda",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Clique no + para adicionar fotos",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(publicacoes) { publicacao ->
                        PublicacaoCard(
                            publicacao = publicacao,
                            onDelete = { onDeletePublicacao(publicacao.id) }
                        )
                    }
                }
            }
        }
        is PublicacoesState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    publicacoesState.message,
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun PublicacaoCard(
    publicacao: com.example.oportunyfam.model.Publicacao,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(220.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column {
                // Imagem
                if (!publicacao.imagem.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(publicacao.imagem)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Publicação",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.instituicao),
                        error = painterResource(id = R.drawable.instituicao)
                    )
                }

                // Descrição
                if (!publicacao.descricao.isNullOrEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            publicacao.descricao,
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            maxLines = 3,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Botão deletar
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Deletar",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Deletar Publicação") },
            text = { Text("Deseja realmente deletar esta publicação?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Deletar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun CriarPublicacaoDialog(
    descricao: String,
    imagemSelecionada: Boolean,
    isLoading: Boolean,
    onDescricaoChange: (String) -> Unit,
    onSelecionarImagem: () -> Unit,
    onSalvar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Nova Publicação", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = onDescricaoChange,
                    label = { Text("Descrição * (mín. 30 caracteres)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    maxLines = 6,
                    minLines = 4,
                    placeholder = { Text("Descreva sua publicação em detalhes...") },
                    isError = descricao.isNotEmpty() && descricao.trim().length < 30,
                    supportingText = {
                        Text(
                            text = "${descricao.trim().length}/30",
                            color = if (descricao.trim().length >= 30) Color.Gray else Color.Red,
                            fontSize = 12.sp
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSelecionarImagem,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (imagemSelecionada) Color(0xFF4CAF50) else Color(0xFFFFA000)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (imagemSelecionada) "Imagem Selecionada ✓" else "Selecionar Imagem *")
                }
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFFFA000)
                )
            } else {
                TextButton(
                    onClick = onSalvar,
                    enabled = descricao.trim().length >= 30 && imagemSelecionada
                ) {
                    Text("Publicar", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

// ============================================
// COMPONENTE DE LOADING
// ============================================

@Composable
private fun LoadingScreen(message: String = "Carregando...") {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFFA000))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}


// ============================================
// PREVIEW
// ============================================

@Preview(showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    PerfilScreen(navController = null)
}