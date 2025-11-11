package com.oportunyfam_mobile_ong.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile_ong.model.InscricaoDetalhada
import com.oportunyfam_mobile_ong.model.StatusInscricao
import com.oportunyfam_mobile_ong.viewmodel.InscricaoViewModel
import com.oportunyfam_mobile_ong.viewmodel.InscricoesState
import com.oportunyfam_mobile_ong.viewmodel.AtualizarInscricaoState
import kotlinx.coroutines.launch

/**
 * Tela de gerenciamento de alunos inscritos na atividade
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarAlunosScreen(
    atividadeId: Int,
    onBack: () -> Unit,
    viewModel: InscricaoViewModel = viewModel()
) {
    val inscricoesState by viewModel.inscricoesState.collectAsState()
    val atualizarState by viewModel.atualizarState.collectAsState()

    var searchText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Carregar inscrições ao abrir a tela
    LaunchedEffect(atividadeId) {
        viewModel.buscarInscricoesPorAtividade(atividadeId)
    }

    // Observar estado de atualização
    LaunchedEffect(atualizarState) {
        when (atualizarState) {
            is AtualizarInscricaoState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Status atualizado com sucesso!")
                }
                viewModel.limparEstadoAtualizacao()
            }
            is AtualizarInscricaoState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        (atualizarState as AtualizarInscricaoState.Error).message
                    )
                }
                viewModel.limparEstadoAtualizacao()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Alunos Inscritos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFA000),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Barra de pesquisa
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Pesquisar aluno...") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFA000),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Conteúdo baseado no estado
            when (inscricoesState) {
                is InscricoesState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFFA000))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Carregando alunos...", color = Color.Gray)
                        }
                    }
                }
                is InscricoesState.Success -> {
                    val inscricoes = (inscricoesState as InscricoesState.Success).inscricoes
                    val inscricoesFiltradas = if (searchText.isBlank()) {
                        inscricoes
                    } else {
                        inscricoes.filter {
                            it.crianca_nome.contains(searchText, ignoreCase = true)
                        }
                    }

                    if (inscricoesFiltradas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (searchText.isBlank())
                                        "Nenhum aluno inscrito ainda"
                                    else
                                        "Nenhum aluno encontrado",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(inscricoesFiltradas) { inscricao ->
                                AlunoCard(
                                    inscricao = inscricao,
                                    onStatusChange = { novoStatusId ->
                                        android.util.Log.e("GerenciarAlunos", "======================================")
                                        android.util.Log.e("GerenciarAlunos", "📥 CALLBACK RECEBIDO!")
                                        android.util.Log.e("GerenciarAlunos", "Novo Status ID: $novoStatusId")
                                        android.util.Log.e("GerenciarAlunos", "Inscrição: ${inscricao.inscricao_id}")
                                        android.util.Log.e("GerenciarAlunos", "Atividade: $atividadeId")
                                        android.util.Log.e("GerenciarAlunos", "======================================")

                                        viewModel.atualizarStatusInscricao(
                                            inscricaoId = inscricao.inscricao_id,
                                            novoStatus = novoStatusId,
                                            atividadeId = atividadeId
                                        )

                                        android.util.Log.e("GerenciarAlunos", "✅ viewModel.atualizarStatusInscricao CHAMADO")
                                    },
                                    onRemover = {
                                        viewModel.removerAluno(
                                            inscricaoId = inscricao.inscricao_id,
                                            atividadeId = atividadeId
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                is InscricoesState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "Erro ao carregar alunos",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (inscricoesState as InscricoesState.Error).message,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.buscarInscricoesPorAtividade(atividadeId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFA000)
                                )
                            ) {
                                Text("Tentar Novamente", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de aluno inscrito
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlunoCard(
    inscricao: InscricaoDetalhada,
    onStatusChange: (Int) -> Unit,  // Callback genérico que recebe o novo status ID
    onRemover: () -> Unit
) {
    val context = LocalContext.current
    var showRemoveDialog by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    // Opções de status disponíveis (do enum StatusInscricao, sem ID 1)
    val statusOptions = StatusInscricao.entries.map { it.id to it.nome }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto do aluno
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    if (!inscricao.crianca_foto.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(inscricao.crianca_foto)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto do aluno",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            error = painterResource(id = R.drawable.perfil)
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Informações do aluno
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        inscricao.crianca_nome,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    if (!inscricao.observacao.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            inscricao.observacao,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botão Remover
                IconButton(
                    onClick = { showRemoveDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remover",
                        tint = Color.Red
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dropdown de Status
            ExposedDropdownMenuBox(
                expanded = expandedStatus,
                onExpandedChange = { expandedStatus = !expandedStatus }
            ) {
                OutlinedTextField(
                    value = inscricao.status_inscricao,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status da Inscrição") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir",
                            modifier = Modifier.rotate(if (expandedStatus) 180f else 0f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFA000),
                        unfocusedBorderColor = Color.LightGray,
                        focusedLabelColor = Color(0xFFFFA000)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = {
                        Surface(
                            shape = CircleShape,
                            color = getStatusColor(inscricao.status_inscricao),
                            modifier = Modifier.size(12.dp)
                        ) {}
                    }
                )

                ExposedDropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false }
                ) {
                    statusOptions.forEach { (statusId, statusNome) ->
                        // Não mostrar opção se já for o status atual (verifica ID E nome)
                        val isStatusAtual = statusId == inscricao.status_id ||
                                           statusNome.equals(inscricao.status_inscricao, ignoreCase = true)

                        if (!isStatusAtual) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Badge visual do status
                                        Surface(
                                            shape = CircleShape,
                                            color = getStatusColor(statusNome),
                                            modifier = Modifier.size(12.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(statusNome)
                                    }
                                },
                                onClick = {
                                    android.util.Log.e("GerenciarAlunos", "======================================")
                                    android.util.Log.e("GerenciarAlunos", "🎯 DROPDOWN CLICADO!")
                                    android.util.Log.e("GerenciarAlunos", "Status selecionado: ID=$statusId, Nome=$statusNome")
                                    android.util.Log.e("GerenciarAlunos", "Inscrição ID: ${inscricao.inscricao_id}")
                                    android.util.Log.e("GerenciarAlunos", "======================================")

                                    // Toast para feedback visual
                                    android.widget.Toast.makeText(
                                        context,
                                        "Mudando para: $statusNome",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    expandedStatus = false

                                    android.util.Log.e("GerenciarAlunos", "📞 Chamando onStatusChange($statusId)")

                                    // Atualizar status passando o ID selecionado
                                    onStatusChange(statusId)

                                    android.util.Log.e("GerenciarAlunos", "✅ onStatusChange chamado com sucesso")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmação de remoção
    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remover Aluno") },
            text = { Text("Deseja realmente remover ${inscricao.crianca_nome} desta atividade?") },
            confirmButton = {
                TextButton(onClick = {
                    onRemover()
                    showRemoveDialog = false
                }) {
                    Text("Remover", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Retorna a cor do status baseado no enum StatusInscricao
 */
@Composable
fun getStatusColor(status: String): Color {
    return when (StatusInscricao.fromNome(status)) {
        StatusInscricao.APROVADA -> Color(0xFF4CAF50)      // Verde
        StatusInscricao.NEGADA -> Color(0xFFFF5722)        // Vermelho
        StatusInscricao.CANCELADA -> Color(0xFF9E9E9E)     // Cinza
        StatusInscricao.PENDENTE -> Color(0xFFFFA000)      // Laranja
        null -> Color.Gray                                  // Fallback
    }
}

