package com.example.Telas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
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
import com.example.MainActivity.NavRoutes
import com.example.data.InstituicaoAuthDataStore
import com.example.oportunyfam.Service.RetrofitFactory
import com.example.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile.model.InstituicaoAtualizarRequest

import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import android.util.Log
import coil.request.CachePolicy


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavHostController?
) {
    val context = LocalContext.current
    val instituicaoAuthDataStore = remember { InstituicaoAuthDataStore(context) }
    val scope = rememberCoroutineScope()

    // Instituicao reativa a partir do DataStore
    val instituicao by instituicaoAuthDataStore.instituicaoStateFlow().collectAsState(initial = null)
    val isLoadingData = remember { mutableStateOf(false) }

    // Carrega dados explicitamente se ainda não estiverem disponíveis
    LaunchedEffect(key1 = Unit) {
        if (instituicao == null) {
            Log.d("PerfilScreen", "Tentando carregar dados da instituição...")
            isLoadingData.value = true
            try {
                val loaded = instituicaoAuthDataStore.loadInstituicao()
                Log.d("PerfilScreen", "Dados carregados: ${loaded?.nome ?: "null"}")
            } catch (e: Exception) {
                Log.e("PerfilScreen", "Erro ao carregar dados: ${e.message}", e)
            } finally {
                isLoadingData.value = false
            }
        }
    }

    // Adicionar log para ver os dados da instituição
    LaunchedEffect(instituicao) {
        Log.d("PerfilScreen", "========== DEBUG IMAGEM ==========")
        Log.d("PerfilScreen", "Instituicao: ${instituicao?.nome}")
        Log.d("PerfilScreen", "Foto Perfil URL: ${instituicao?.foto_perfil}")
        Log.d("PerfilScreen", "URL está vazia? ${instituicao?.foto_perfil.isNullOrEmpty()}")
        Log.d("PerfilScreen", "==================================")
    }

    // Estado para controlar a exibição do diálogo de edição
    var showEditDialog by remember { mutableStateOf(false) }

    // Estado para a nova descrição
    var novaDescricao by remember { mutableStateOf("") }

    // Estado para controlar o carregamento durante a atualização
    var isLoadingUpdate by remember { mutableStateOf(false) }

    // Estado para mensagens de erro/sucesso
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }


    // ----------------------------------------------------
    // FUNÇÃO PARA LOGOUT E NAVEGAÇÃO
    // ----------------------------------------------------
    val onLogout: () -> Unit = {
        scope.launch {
            instituicaoAuthDataStore.logout()
            navController?.navigate(NavRoutes.REGISTRO) {
                popUpTo(navController.graph.startDestinationId) {
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

                    // Criar o request para atualização com dados reais
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
                        // Atualiza o estado local com a nova descrição
                        val updatedInstituicao = currentInstituicao.copy( descricao = novaDescricao)
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

    // Não é necessário carregar manualmente: `instituicao` vem do StateFlow reativo
    // isLoadingData permanece false

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

    // Se não houver dados logados, mostra loading e aguarda
    if (instituicao == null) {
        // Timeout: se após 3 segundos ainda não carregar, redireciona
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000)
            if (instituicao == null) {
                Log.w("PerfilScreen", "Timeout aguardando dados da instituição - redirecionando para login")
                onLogout()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFFFA000))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Carregando perfil...", color = Color.Gray)
            }
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

        HorizontalDivider(color = Color.LightGray, thickness = 1.5.dp)

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
                            // Imagem da Instituição (SEMPRE ESTÁTICA - não muda com upload)
                            Image(
                                painter = painterResource(id = R.drawable.instituicao),
                                contentDescription = "Logo da Instituição",
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
                        HorizontalDivider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // 🔽 IMAGEM DE PERFIL CENTRALIZADA - CARREGA DA API 🔽
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset(y = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(70.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.size(150.dp)) {
                        // Carrega imagem da URL da API ou mostra imagem padrão
                        val fotoPerfilUrl = instituicao?.foto_perfil

                        // DEBUG: Log sempre que renderizar
                        Log.d("PerfilScreen_Render", "Renderizando imagem. URL: $fotoPerfilUrl")

                        if (!fotoPerfilUrl.isNullOrEmpty()) {
                            Log.d("PerfilScreen_Render", "✅ Carregando AsyncImage com URL: $fotoPerfilUrl")
                            // Carrega imagem do servidor com cache desabilitado
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(fotoPerfilUrl)
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.DISABLED)
                                    .memoryCachePolicy(CachePolicy.DISABLED)
                                    .build(),
                                contentDescription = "Imagem de perfil da instituição",
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.perfil),
                                error = painterResource(id = R.drawable.perfil),
                                modifier = Modifier.fillMaxSize(),
                                onSuccess = {
                                    Log.d("PerfilScreen_Render", "✅ Imagem carregada com SUCESSO!")
                                },
                                onError = { error ->
                                    Log.e("PerfilScreen_Render", "❌ ERRO ao carregar imagem: ${error.result.throwable.message}")
                                }
                            )
                        } else {
                            Log.d("PerfilScreen_Render", "⚠️ URL vazia, mostrando imagem padrão")
                            // Imagem padrão quando não há foto de perfil
                            Image(
                                painter = painterResource(id = R.drawable.perfil),
                                contentDescription = "Sem foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
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
}

@Preview(showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    PerfilScreen(
        navController = null
    )
}