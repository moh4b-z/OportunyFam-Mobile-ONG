package com.oportunyfam_mobile_ong.Screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile_ong.Components.Cards.ResumoAtividadeCardAPI
import com.oportunyfam_mobile_ong.Components.OpcaoGerenciamento
import com.oportunyfam_mobile_ong.Service.AzureBlobRetrofit
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.data.AtividadeFotoDataStore
import com.oportunyfam_mobile_ong.model.getRealPathFromURI
import com.oportunyfam_mobile_ong.viewmodel.AtividadeDetalheState
import com.oportunyfam_mobile_ong.viewmodel.AtividadeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Tela de detalhes da atividade (com dados da API)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesAtividadeScreen(
    viewModel: AtividadeViewModel,
    atividadeId: Int,
    onBack: () -> Unit,
    onVerAlunos: () -> Unit,
    onVerCalendario: () -> Unit,
    onConfiguracoes: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val atividadeDetalheState by viewModel.atividadeDetalheState.collectAsState()

    // Estados para gerenciar foto
    val atividadeFotoDataStore = remember { AtividadeFotoDataStore(context)  }
    var isUploadingFoto by remember { mutableStateOf(false) }
    var tempImageFile by remember { mutableStateOf<File?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Carregar foto salva da atividade ao abrir a tela
    LaunchedEffect(atividadeId) {
        val fotoSalva = atividadeFotoDataStore.buscarFotoAtividade(atividadeId)
        if (fotoSalva != null && atividadeDetalheState is AtividadeDetalheState.Success) {
            val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade
            atividade.atividade_foto = fotoSalva
        }
    }

    // Função para upload e atualização da foto DESTA ATIVIDADE ESPECÍFICA
    val uploadAndUpdatePhoto: () -> Unit = {
        tempImageFile?.let { imageFile ->
            isUploadingFoto = true
            scope.launch {
                try {
                    // Verificar se Azure está configurado
                    if (!com.oportunyfam_mobile_ong.Config.AzureConfig.isConfigured()) {
                        Log.w("DetalhesAtividade", "⚠️ Azure não configurado")
                        scope.launch {
                            snackbarHostState.showSnackbar("Upload de imagens não configurado")
                        }
                        isUploadingFoto = false
                        return@launch
                    }

                    val accountKey = com.oportunyfam_mobile_ong.Config.AzureConfig.getStorageKey()!!
                    Log.d("DetalhesAtividade", "📤 Fazendo upload da foto da atividade ID: $atividadeId")

                    val imageUrl = AzureBlobRetrofit.uploadImageToAzure(
                        imageFile,
                        com.oportunyfam_mobile_ong.Config.AzureConfig.STORAGE_ACCOUNT,
                        accountKey,
                        com.oportunyfam_mobile_ong.Config.AzureConfig.CONTAINER_PERFIL
                    )

                    if (imageUrl != null) {
                        val versionedUrl = "$imageUrl?v=${System.currentTimeMillis()}"

                        // 1. Salvar foto localmente no DataStore (fallback)
                        atividadeFotoDataStore.salvarFotoAtividade(atividadeId, versionedUrl)
                        Log.d("DetalhesAtividade", "✅ Foto salva localmente para atividade $atividadeId (fallback)")

                        // 2. Atualizar na API (agora com suporte ao campo 'foto')
                        if (atividadeDetalheState is AtividadeDetalheState.Success) {
                            val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade
                            val atividadeService = RetrofitFactory().getAtividadeService()

                            val updateRequest = com.oportunyfam_mobile_ong.model.AtividadeRequest(
                                id_instituicao = atividade.instituicao_id,
                                id_categoria = 1, // TODO: pegar categoria real
                                titulo = atividade.titulo,
                                descricao = atividade.descricao,
                                faixa_etaria_min = atividade.faixa_etaria_min,
                                faixa_etaria_max = atividade.faixa_etaria_max,
                                gratuita = atividade.gratuita == 1,
                                preco = atividade.preco,
                                ativo = atividade.ativo == 1,
                                foto = versionedUrl  // ✅ Enviar foto para API
                            )

                            // Usar enqueue (assíncrono) ao invés de execute (síncrono)
                            atividadeService.atualizarAtividade(atividadeId, updateRequest)
                                .enqueue(object : retrofit2.Callback<com.oportunyfam_mobile_ong.model.AtividadeCriadaResponse> {
                                    override fun onResponse(
                                        call: retrofit2.Call<com.oportunyfam_mobile_ong.model.AtividadeCriadaResponse>,
                                        response: retrofit2.Response<com.oportunyfam_mobile_ong.model.AtividadeCriadaResponse>
                                    ) {
                                        if (response.isSuccessful) {
                                            Log.d("DetalhesAtividade", "✅ Foto atualizada na API com sucesso!")
                                            scope.launch {
                                                // Recarregar dados
                                                delay(200)
                                                viewModel.buscarAtividadePorId(atividadeId)
                                                delay(100)
                                                snackbarHostState.showSnackbar("✅ Foto atualizada na API!")
                                            }
                                        } else {
                                            Log.w("DetalhesAtividade", "⚠️ API retornou erro (${response.code()})")
                                            scope.launch {
                                                snackbarHostState.showSnackbar("✅ Foto salva localmente!")
                                            }
                                        }
                                    }

                                    override fun onFailure(
                                        call: retrofit2.Call<com.oportunyfam_mobile_ong.model.AtividadeCriadaResponse>,
                                        t: Throwable
                                    ) {
                                        Log.w("DetalhesAtividade", "⚠️ Erro ao atualizar API: ${t.message}")
                                        scope.launch {
                                            snackbarHostState.showSnackbar("✅ Foto salva localmente!")
                                        }
                                    }
                                })
                        }

                        // 3. Atualizar a foto no estado atual (exibição imediata)
                        delay(100)
                        if (atividadeDetalheState is AtividadeDetalheState.Success) {
                            val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade
                            atividade.atividade_foto = versionedUrl
                        }

                        // 4. Atualizar a foto na lista de atividades (para refletir mudança ao voltar)
                        viewModel.atualizarFotoAtividade(atividadeId, versionedUrl)

                        // 5. Se não conseguiu fazer chamada da API, mostrar mensagem
                        if (atividadeDetalheState !is AtividadeDetalheState.Success) {
                            scope.launch {
                                snackbarHostState.showSnackbar("✅ Foto salva!")
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Erro ao fazer upload da foto")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DetalhesAtividade", "❌ Erro no upload", e)
                    scope.launch {
                        snackbarHostState.showSnackbar("Erro: ${e.message}")
                    }
                } finally {
                    isUploadingFoto = false
                    tempImageFile = null
                }
            }
        }
    }

    // Launcher para selecionar imagem
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("DetalhesAtividade", "📷 Imagem selecionada: $it")
            val filePath = context.getRealPathFromURI(it)
            filePath?.let { path ->
                tempImageFile = File(path)
                uploadAndUpdatePhoto()
            } ?: run {
                scope.launch {
                    snackbarHostState.showSnackbar("Erro ao processar imagem")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (atividadeDetalheState) {
                        is AtividadeDetalheState.Success -> {
                            Text(
                                (atividadeDetalheState as AtividadeDetalheState.Success).atividade.titulo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> Text("Detalhes", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Mostrar loading durante upload
        if (isUploadingFoto) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFFFFA000))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Atualizando foto...", color = Color.Gray)
                }
            }
            return@Scaffold
        }

        when (atividadeDetalheState) {
            is AtividadeDetalheState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFA000))
                }
            }
            is AtividadeDetalheState.Success -> {
                val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                ) {
                    item {
                        // Card de resumo com dados reais e capacidade de editar foto
                        ResumoAtividadeCardAPI(
                            atividade = atividade,
                            onEditarFoto = {
                                Log.d("DetalhesAtividade", "📸 Botão de editar foto clicado")
                                imagePickerLauncher.launch("image/*")
                            }
                        )
                    }

                    item {
                        // Opções de gerenciamento
                        Column(modifier = Modifier.padding(16.dp)) {
                            OpcaoGerenciamento(
                                titulo = "👥 Gerenciar Alunos",
                                descricao = "Ver e editar alunos cadastrados",
                                onClick = onVerAlunos
                            )

                            OpcaoGerenciamento(
                                titulo = "📅 Calendário de Aulas",
                                descricao = "${atividade.aulas.size} aulas cadastradas",
                                onClick = onVerCalendario
                            )

                            OpcaoGerenciamento(
                                titulo = "⚙️ Configurações",
                                descricao = "Editar informações da atividade",
                                onClick = onConfiguracoes
                            )
                        }
                    }
                }
            }
            is AtividadeDetalheState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Erro ao carregar detalhes", color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            (atividadeDetalheState as AtividadeDetalheState.Error).message,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            AtividadeDetalheState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFA000))
                }
            }
        }
    }
}