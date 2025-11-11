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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oportunyfam_mobile_ong.Components.Cards.CardAulaAPI
import com.oportunyfam_mobile_ong.Components.CriarAulaDialog
import com.oportunyfam_mobile_ong.data.InstituicaoAuthDataStore
import com.oportunyfam_mobile_ong.model.AulaLoteRequest
import com.oportunyfam_mobile_ong.model.AulaRequest
import com.oportunyfam_mobile_ong.viewmodel.*
import kotlinx.coroutines.launch

/**
 * Tela de calendário de aulas com gerenciamento completo
 * - Visualização de aulas por atividade
 * - Criação de aulas individuais ou em lote via calendário nativo
 * - Integração com API
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioAulasScreen(
    viewModel: AtividadeViewModel,
    atividadeId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val aulaViewModel: AulaViewModel = viewModel()

    // Estados
    val atividadeDetalheState by viewModel.atividadeDetalheState.collectAsState()
    val aulasState by aulaViewModel.aulasState.collectAsState()
    val criarAulaState by aulaViewModel.criarAulaState.collectAsState()

    var showCriarDialog by remember { mutableStateOf(false) }
    var instituicaoId by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Carregar instituição logada
    LaunchedEffect(Unit) {
        val instituicaoAuthDataStore = InstituicaoAuthDataStore(context)
        val instituicao = instituicaoAuthDataStore.loadInstituicao()
        instituicaoId = instituicao?.instituicao_id
    }

    // Carregar aulas da atividade
    LaunchedEffect(atividadeId, instituicaoId) {
        instituicaoId?.let { instId ->
            aulaViewModel.buscarAulasPorAtividade(atividadeId, instId)
        }
    }

    // Observar estado de criação
    LaunchedEffect(criarAulaState) {
        when (criarAulaState) {
            is CriarAulaState.Success -> {
                Log.d("CalendarioAulas", "✅ Aula criada! Iniciando recarregamento...")
                scope.launch {
                    snackbarHostState.showSnackbar("✅ Aula criada com sucesso!")
                }
                showCriarDialog = false

                // Aguardar um pouco para garantir que a API processou
                kotlinx.coroutines.delay(500)

                // Recarregar aulas e atividade
                instituicaoId?.let { instId ->
                    Log.d("CalendarioAulas", "🔄 Recarregando aulas da atividade $atividadeId...")
                    aulaViewModel.recarregarAulas(atividadeId, instId)
                }
                viewModel.buscarAtividadePorId(atividadeId)

                // Limpar estado após recarregar
                kotlinx.coroutines.delay(100)
                aulaViewModel.limparEstadoCriacao()
            }
            is CriarAulaState.SuccessLote -> {
                val total = (criarAulaState as CriarAulaState.SuccessLote).total
                Log.d("CalendarioAulas", "✅ $total aulas criadas! Iniciando recarregamento...")
                scope.launch {
                    snackbarHostState.showSnackbar("✅ $total aulas criadas com sucesso!")
                }
                showCriarDialog = false

                // Aguardar um pouco para garantir que a API processou
                kotlinx.coroutines.delay(500)

                // Recarregar aulas e atividade
                instituicaoId?.let { instId ->
                    Log.d("CalendarioAulas", "🔄 Recarregando aulas da atividade $atividadeId...")
                    aulaViewModel.recarregarAulas(atividadeId, instId)
                }
                viewModel.buscarAtividadePorId(atividadeId)

                // Limpar estado após recarregar
                kotlinx.coroutines.delay(100)
                aulaViewModel.limparEstadoCriacao()
            }
            is CriarAulaState.Error -> {
                Log.e("CalendarioAulas", "❌ Erro ao criar aula: ${(criarAulaState as CriarAulaState.Error).message}")
                scope.launch {
                    snackbarHostState.showSnackbar(
                        (criarAulaState as CriarAulaState.Error).message
                    )
                }
                aulaViewModel.limparEstadoCriacao()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendário de Aulas", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { showCriarDialog = true },
                containerColor = Color(0xFFFFA000)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Aula", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Informações da atividade
            when (atividadeDetalheState) {
                is AtividadeDetalheState.Success -> {
                    val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade

                    Text(
                        text = atividade.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp)
                    )
                }
                else -> {}
            }

            // Lista de aulas
            when (aulasState) {
                is AulasState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFFA000))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Carregando aulas...", color = Color.Gray)
                        }
                    }
                }
                is AulasState.Success -> {
                    var aulas = (aulasState as AulasState.Success).aulas

                    // FALLBACK: Se não carregou via API, usar aulas da atividade se disponível
                    if (aulas.isEmpty() && atividadeDetalheState is AtividadeDetalheState.Success) {
                        val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade
                        if (atividade.aulas.isNotEmpty()) {
                            Log.d("CalendarioAulas", "⚠️ Usando aulas da atividade como fallback (${atividade.aulas.size} aulas)")
                            // EFETIVAMENTE usar as aulas da atividade
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
                        }
                    }

                    if (aulas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nenhuma aula cadastrada", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
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
                                CardAulaAPI(aula = aula.toAulaDetalhe())
                            }
                        }
                    }
                }
                is AulasState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Erro ao carregar aulas",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (aulasState as AulasState.Error).message,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
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