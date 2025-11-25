package com.oportunyfam_mobile_ong.Screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oportunyfam_mobile_ong.Components.Cards.CardAulaAPI
import com.oportunyfam_mobile_ong.Components.CriarAulaDialog
import com.oportunyfam_mobile_ong.model.AulaLoteRequest
import com.oportunyfam_mobile_ong.model.AulaRequest
import com.oportunyfam_mobile_ong.viewmodel.*
import kotlinx.coroutines.launch

/**
 * Tela SIMPLIFICADA de calendário de aulas
 * APENAS para adicionar e remover aulas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioAulasScreen(
    viewModel: AtividadeViewModel,
    modifier: Modifier,
    atividadeId: Int,
    onBack: () -> Unit
) {
    val aulaViewModel: AulaViewModel = viewModel()

    // Estados
    val atividadeDetalheState by viewModel.atividadeDetalheState.collectAsState()
    val criarAulaState by aulaViewModel.criarAulaState.collectAsState()

    var showCriarDialog by remember { mutableStateOf(false) }
    var aulas by remember { mutableStateOf(emptyList<com.oportunyfam_mobile_ong.model.AulaDetalhada>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Carregar dados da atividade (que inclui as aulas)
    LaunchedEffect(atividadeId) {
        viewModel.buscarAtividadePorId(atividadeId)
    }

    // Atualizar lista de aulas quando a atividade carregar
    LaunchedEffect(atividadeDetalheState) {
        if (atividadeDetalheState is AtividadeDetalheState.Success) {
            val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade
            aulas = atividade.aulas.map { aulaDetalhe ->
                com.oportunyfam_mobile_ong.model.AulaDetalhada(
                    aula_id = aulaDetalhe.aula_id,
                    id_atividade = atividadeId,
                    data_aula = aulaDetalhe.data_aula ?: aulaDetalhe.data ?: "",
                    hora_inicio = aulaDetalhe.hora_inicio,
                    hora_fim = aulaDetalhe.hora_fim,
                    vagas_total = aulaDetalhe.vagas_total,
                    vagas_disponiveis = aulaDetalhe.vagas_disponiveis,
                    status_aula = aulaDetalhe.status_aula,
                    iram_participar = aulaDetalhe.iram_participar,
                    foram = aulaDetalhe.foram,
                    ausentes = aulaDetalhe.ausentes,
                    nome_atividade = atividade.titulo,
                    instituicao_nome = atividade.instituicao_nome
                )
            }
            Log.d("CalendarioAulas", "✅ ${aulas.size} aulas carregadas")
        }
    }

    // Observar estado de criação
    LaunchedEffect(criarAulaState) {
        when (criarAulaState) {
            is CriarAulaState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("✅ Aula criada!")
                }
                showCriarDialog = false

                // Recarregar
                kotlinx.coroutines.delay(500)
                viewModel.buscarAtividadePorId(atividadeId)
                aulaViewModel.limparEstadoCriacao()
            }
            is CriarAulaState.SuccessLote -> {
                val total = (criarAulaState as CriarAulaState.SuccessLote).total
                scope.launch {
                    snackbarHostState.showSnackbar("✅ $total aulas criadas!")
                }
                showCriarDialog = false

                // Recarregar
                kotlinx.coroutines.delay(500)
                viewModel.buscarAtividadePorId(atividadeId)
                aulaViewModel.limparEstadoCriacao()
            }
            is CriarAulaState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar("❌ ${(criarAulaState as CriarAulaState.Error).message}")
                }
                aulaViewModel.limparEstadoCriacao()
            }
            else -> {}
        }
    }

    // Função para deletar aula
    val deletarAula: (Int) -> Unit = { aulaId ->
        scope.launch {
            try {
                Log.d("CalendarioAulas", "🗑️ Deletando aula ID: $aulaId")
                aulaViewModel.deletarAula(aulaId)

                snackbarHostState.showSnackbar("🗑️ Aula excluída!")

                // Recarregar
                kotlinx.coroutines.delay(500)
                viewModel.buscarAtividadePorId(atividadeId)
            } catch (e: Exception) {
                Log.e("CalendarioAulas", "❌ Erro: ${e.message}")
                snackbarHostState.showSnackbar("Erro ao excluir")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Gerenciar Aulas", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Box(modifier = Modifier.padding(bottom = 90.dp)) {
                FloatingActionButton(
                    onClick = { showCriarDialog = true },
                    containerColor = Color(0xFFFFA000)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova Aula", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            when (atividadeDetalheState) {
                is AtividadeDetalheState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFFA000))
                    }
                }
                is AtividadeDetalheState.Success -> {
                    val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade

                    // Título
                    Text(
                        text = atividade.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Lista de aulas
                    if (aulas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📅", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Nenhuma aula cadastrada", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Clique no + para adicionar", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        Text(
                            text = "Aulas Cadastradas (${aulas.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 8.dp),
                            color = Color(0xFFFFA000)
                        )

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(aulas) { aula ->
                                CardAulaAPI(
                                    aula = aula.toAulaDetalhe(),
                                    onDelete = deletarAula
                                )
                            }
                        }
                    }
                }
                is AtividadeDetalheState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Erro ao carregar",
                            color = Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {}
            }
        }

        // Diálogo de criação de aulas
        if (showCriarDialog) {
            CriarAulaDialog(
                onDismiss = { showCriarDialog = false },
                onConfirm = { datasSelecionadas, horaInicio, horaFim, vagasTotal ->
                    Log.d("CalendarioAulas", "📝 Criando aulas: ${datasSelecionadas.size} datas")

                    if (datasSelecionadas.size == 1) {
                        // Criar aula individual
                        val aulaRequest = AulaRequest(
                            id_atividade = atividadeId,
                            data_aula = datasSelecionadas.first(),
                            hora_inicio = horaInicio,
                            hora_fim = horaFim,
                            vagas_total = vagasTotal,
                            vagas_disponiveis = vagasTotal,
                            ativo = true
                        )
                        aulaViewModel.criarAula(aulaRequest)
                    } else {
                        // Criar aulas em lote
                        val aulaLoteRequest = AulaLoteRequest(
                            id_atividade = atividadeId,
                            hora_inicio = horaInicio,
                            hora_fim = horaFim,
                            vagas_total = vagasTotal,
                            datas = datasSelecionadas
                        )
                        aulaViewModel.criarAulasLote(aulaLoteRequest)
                    }
                }
            )
        }
    }
}

// Extensão para converter AulaDetalhada para AulaDetalhe
private fun com.oportunyfam_mobile_ong.model.AulaDetalhada.toAulaDetalhe(): com.oportunyfam_mobile_ong.model.AulaDetalhe {
    return com.oportunyfam_mobile_ong.model.AulaDetalhe(
        aula_id = this.aula_id,
        id_atividade = this.id_atividade,
        data_aula = this.data_aula,
        data = this.data_aula,
        hora_inicio = this.hora_inicio,
        hora_fim = this.hora_fim,
        vagas_total = this.vagas_total,
        vagas_disponiveis = this.vagas_disponiveis,
        status_aula = this.status_aula,
        iram_participar = this.iram_participar,
        foram = this.foram,
        ausentes = this.ausentes,
        nome_atividade = this.nome_atividade ?: "",
        instituicao_nome = this.instituicao_nome ?: ""
    )
}

