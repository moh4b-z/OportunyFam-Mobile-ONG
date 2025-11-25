package com.oportunyfam_mobile_ong.Screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.oportunyfam_mobile_ong.Components.AgendaHorizontal
import com.oportunyfam_mobile_ong.Components.BarraTarefas
import com.oportunyfam_mobile_ong.MainActivity.NavRoutes
import com.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.data.InstituicaoAuthDataStore
import com.oportunyfam_mobile_ong.model.Aluno
import com.oportunyfam_mobile_ong.model.AulaDetalhada
import com.oportunyfam_mobile_ong.model.ConversaRequest
import com.oportunyfam_mobile_ong.model.StatusInscricao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * HomeScreen - Tela principal do aplicativo
 *
 * Exibe:
 * - Lista de atividades disponíveis
 * - Lista de alunos cadastrados
 * - Opções de gerenciamento com filtros
 *
 * @param navController Controlador de navegação
 */
@Composable
fun HomeScreen(navController: NavHostController?) {
    val context = LocalContext.current
    val authDataStore = remember { InstituicaoAuthDataStore(context) }

    var listaAlunos by remember { mutableStateOf<List<Aluno>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var instituicaoId by remember { mutableStateOf<Int?>(null) }
    var pessoaId by remember { mutableStateOf<Int?>(null) } // ✅ Adiciona pessoa_id

    // Filtros
    var statusFiltro by remember { mutableStateOf<Int?>(null) }
    var atividadeFiltro by remember { mutableStateOf<Int?>(null) }

    // Agenda
    var listaAulas by remember { mutableStateOf<List<AulaDetalhada>>(emptyList()) }
    var dataSelecionada by remember { mutableStateOf<LocalDate?>(null) } // ✅ Null = não clicou ainda
    var diaClicado by remember { mutableStateOf(false) } // ✅ Controla se já clicou

    // Carrega instituição logada
    LaunchedEffect(Unit) {
        val instituicao = authDataStore.loadInstituicao()
        instituicaoId = instituicao?.instituicao_id
        pessoaId = instituicao?.pessoa_id // ✅ Carrega pessoa_id também
        Log.d("HomeScreen", "🏫 Instituição ID=$instituicaoId, Pessoa ID=$pessoaId")
    }

    // Carrega as aulas para a agenda
    LaunchedEffect(instituicaoId) {
        val id = instituicaoId ?: return@LaunchedEffect

        val service = RetrofitFactory().getAtividadeService()
        try {
            Log.d("HomeScreen", "🔄 Buscando atividades da instituição ID: $id")

            // 1. Buscar atividades da instituição
            val atividadesResponse = withContext(Dispatchers.IO) {
                service.buscarAtividadesPorInstituicao(id).execute()
            }

            if (atividadesResponse.isSuccessful) {
                val atividades = atividadesResponse.body()?.atividades ?: emptyList()
                Log.d("HomeScreen", "✅ ${atividades.size} atividades encontradas")

                // 2. Extrair aulas de cada atividade
                val todasAulas = mutableListOf<AulaDetalhada>()

                atividades.forEach { atividade ->
                    Log.d("HomeScreen", "📚 Buscando aulas da atividade: ${atividade.titulo}")

                    val detalhesResponse = withContext(Dispatchers.IO) {
                        service.buscarAtividadePorId(atividade.atividade_id).execute()
                    }

                    if (detalhesResponse.isSuccessful) {
                        val atividadeCompleta = detalhesResponse.body()?.atividade
                        val aulas = atividadeCompleta?.aulas ?: emptyList()

                        Log.d("HomeScreen", "  ✅ ${aulas.size} aulas encontradas na atividade ${atividade.titulo}")

                        aulas.forEach { aulaDetalhe ->
                            todasAulas.add(
                                AulaDetalhada(
                                    aula_id = aulaDetalhe.aula_id,
                                    id_atividade = atividade.atividade_id,
                                    data_aula = aulaDetalhe.data_aula ?: aulaDetalhe.data ?: "",
                                    hora_inicio = aulaDetalhe.hora_inicio,
                                    hora_fim = aulaDetalhe.hora_fim,
                                    vagas_total = aulaDetalhe.vagas_total,
                                    vagas_disponiveis = aulaDetalhe.vagas_disponiveis,
                                    status_aula = aulaDetalhe.status_aula,
                                    nome_atividade = atividade.titulo,
                                    instituicao_nome = atividade.instituicao_nome ?: "",
                                    iram_participar = aulaDetalhe.iram_participar,
                                    foram = aulaDetalhe.foram,
                                    ausentes = aulaDetalhe.ausentes
                                )
                            )
                        }
                    } else {
                        Log.w("HomeScreen", "  ⚠️ Erro ao buscar detalhes da atividade ${atividade.atividade_id}: ${detalhesResponse.code()}")
                    }
                }

                listaAulas = todasAulas
                Log.d("HomeScreen", "")
                Log.d("HomeScreen", "════════════════════════════════════════")
                Log.d("HomeScreen", "✅ TOTAL: ${todasAulas.size} aulas carregadas")
                Log.d("HomeScreen", "════════════════════════════════════════")
                Log.d("HomeScreen", "")
            } else {
                Log.e("HomeScreen", "❌ Erro ao buscar atividades: ${atividadesResponse.code()}")
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "❌ Erro ao buscar aulas: ${e.message}", e)
        }
    }

    // Carrega a lista de alunos da API
    LaunchedEffect(instituicaoId, statusFiltro, atividadeFiltro) {
        val id = instituicaoId ?: return@LaunchedEffect

        isLoading = true
        errorMessage = null

        val service = RetrofitFactory().getInstituicaoService()
        try {
            val response = withContext(Dispatchers.IO) {
                service.buscarAlunos(
                    instituicao_id = id,
                    atividade_id = atividadeFiltro,
                    status_id = statusFiltro
                ).execute()
            }

            if (response.isSuccessful) {
                val todosAlunos = response.body()?.alunos ?: emptyList()

                // ✅ Agrupar por crianca_id para evitar duplicatas
                // (mesma criança em múltiplas atividades)
                listaAlunos = todosAlunos
                    .groupBy { it.crianca_id }
                    .map { (_, alunosGrupo) ->
                        // Pega a primeira inscrição de cada criança
                        // (ou pode escolher a mais recente, aprovada, etc)
                        alunosGrupo.first()
                    }

                Log.d("HomeScreen", "✅ API retornou ${todosAlunos.size} inscrições")
                Log.d("HomeScreen", "✅ Agrupado em ${listaAlunos.size} aluno(s) único(s)")

                errorMessage = null // Limpa erro se sucesso
            } else if (response.code() == 404) {
                // 404 significa que não há alunos cadastrados
                listaAlunos = emptyList()
                errorMessage = null // Não é erro, apenas não tem alunos
                Log.d("HomeScreen", "ℹ️ Nenhum aluno encontrado")
            } else {
                errorMessage = "Erro ao buscar alunos"
                Log.e("HomeScreen", "❌ Erro ${response.code()}: ${response.errorBody()?.string()}")
            }
        } catch (e: CancellationException) {
            // ✅ Coroutine cancelada por navegação - NÃO é erro
            Log.d("HomeScreen", "⏹️ Carregamento de alunos cancelado (navegação)")
            // Não re-throw aqui porque estamos em um LaunchedEffect
        } catch (e: Exception) {
            errorMessage = "Sem conexão com a internet"
            Log.e("HomeScreen", "❌ Erro ao buscar alunos: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Cabeçalho
        HomeHeader()

        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // Conteúdo principal
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            // Card clicável para abrir agenda
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { diaClicado = !diaClicado },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Agenda",
                                tint = Color(0xFFFFA000),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Agenda de Aulas",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "${listaAulas.size} aulas cadastradas",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Icon(
                            if (diaClicado) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (diaClicado) "Fechar" else "Abrir",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Calendário e aulas (APENAS SE CLICOU)
            if (diaClicado) {
                // Calendário
                item {
                    AgendaHorizontal(
                        aulas = listaAulas,
                        onDateSelected = { data ->
                            dataSelecionada = data
                            Log.d("HomeScreen", "📅 Data selecionada: $data")
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Aulas do dia selecionado
                if (dataSelecionada != null) {
                    item {
                // Filtrar aulas pela data selecionada
                val dataFormatada = dataSelecionada?.toString() ?: "" // yyyy-MM-dd

                // Logs detalhados
                Log.d("HomeScreen", "")
                Log.d("HomeScreen", "════════════════════════════════════════")
                Log.d("HomeScreen", "🔍 FILTRANDO AULAS DO DIA")
                Log.d("HomeScreen", "════════════════════════════════════════")
                Log.d("HomeScreen", "📅 Data selecionada: $dataFormatada")
                Log.d("HomeScreen", "📚 Total de aulas disponíveis: ${listaAulas.size}")

                // Log de todas as aulas disponíveis
                listaAulas.forEach { aula ->
                    Log.d("HomeScreen", "  📖 Aula: ${aula.nome_atividade} - Data: '${aula.data_aula}'")
                }

                val aulasDoDia = listaAulas.filter { aula ->
                    try {
                        // Converter a data da aula para LocalDate
                        val aulaLocalDate = when {
                            // Formato: 2025-11-26T14:30:00.000Z
                            aula.data_aula.contains("T") -> {
                                LocalDate.parse(aula.data_aula.substring(0, 10))
                            }
                            // Formato: 26/11/2025 (dd/MM/yyyy)
                            aula.data_aula.contains("/") -> {
                                val partes = aula.data_aula.split("/")
                                if (partes.size == 3) {
                                    val dia = partes[0].toInt()
                                    val mes = partes[1].toInt()
                                    val ano = partes[2].toInt()
                                    LocalDate.of(ano, mes, dia)
                                } else {
                                    null
                                }
                            }
                            // Formato: 2025-11-26 (yyyy-MM-dd)
                            else -> LocalDate.parse(aula.data_aula)
                        }

                        val match = aulaLocalDate == dataSelecionada

                        Log.d("HomeScreen", "  🔎 Comparando: '${aula.data_aula}' → '$aulaLocalDate' == '$dataSelecionada' → ${if (match) "✅ MATCH" else "❌ NO MATCH"}")

                        if (match) {
                            Log.d("HomeScreen", "  ✅ ✅ ✅ Aula ENCONTRADA: ${aula.nome_atividade} às ${aula.hora_inicio}")
                        }

                        match
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "❌ Erro ao filtrar aula: ${e.message}", e)
                        false
                    }
                }

                Log.d("HomeScreen", "")
                Log.d("HomeScreen", "🎯 RESULTADO: ${aulasDoDia.size} aula(s) encontrada(s)")
                Log.d("HomeScreen", "════════════════════════════════════════")
                Log.d("HomeScreen", "")

                // Card com borda destacada para mostrar o resultado do clique
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Mostrar data selecionada com destaque
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(Color(0xFFFFA000), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dataSelecionada?.let { "📅 ${formatarDataExibicao(it)}" } ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFFFA000)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = Color(0xFFFFA000).copy(alpha = 0.3f), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(12.dp))

                        if (aulasDoDia.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Aulas do Dia",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFFFFA000)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFA000).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "${aulasDoDia.size} aula${if (aulasDoDia.size > 1) "s" else ""}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFA000)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            aulasDoDia.forEach { aula ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Ícone de aula
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(
                                                    Color(0xFFFFA000).copy(alpha = 0.1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                tint = Color(0xFFFFA000),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            // Nome da atividade em destaque
                                            Text(
                                                text = aula.nome_atividade ?: "Aula",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.Black,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            // Horário
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "⏰ ",
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "${formatarHora(aula.hora_inicio)} - ${formatarHora(aula.hora_fim)}",
                                                    fontSize = 13.sp,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            // Vagas
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "👥 ",
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "${aula.vagas_disponiveis}/${aula.vagas_total} vagas",
                                                    fontSize = 12.sp,
                                                    color = if (aula.vagas_disponiveis > 0) Color(0xFF4CAF50) else Color.Red,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Status badge
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (aula.status_aula) {
                                                "Hoje" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                                "Futura" -> Color(0xFF2196F3).copy(alpha = 0.1f)
                                                else -> Color.Gray.copy(alpha = 0.1f)
                                            }
                                        ) {
                                            Text(
                                                text = aula.status_aula ?: "",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (aula.status_aula) {
                                                    "Hoje" -> Color(0xFF4CAF50)
                                                    "Futura" -> Color(0xFF2196F3)
                                                    else -> Color.Gray
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                            Spacer(modifier = Modifier.height(16.dp))
                        } else {
                            // Mensagem quando não há aulas no dia selecionado
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📅", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nenhuma aula neste dia",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Selecione outro dia no calendário",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                }
                }
            }

            // Título e Filtros
            item {

                Text(
                    text = "Gerenciar Alunos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Filtros
                FiltrosAlunos(
                    statusSelecionado = statusFiltro,
                    onStatusChange = { statusFiltro = it },
                    atividadeSelecionada = atividadeFiltro,
                    onAtividadeChange = { atividadeFiltro = it }
                )
            }

            // Estados de carregamento e erro
            when {
                isLoading -> item {
                    LoadingIndicator()
                }

                errorMessage != null -> item {
                    ErrorMessage(errorMessage!!)
                }

                listaAlunos.isEmpty() -> item {
                    EmptyStateMessage()
                }

                else -> items(listaAlunos) { aluno ->
                    AlunoCard(
                        aluno = aluno,
                        navController = navController,
                        instituicaoId = instituicaoId,
                        pessoaId = pessoaId // ✅ Passa pessoaId
                    )
                }
            }
        }

        // Fundo da área da barra na mesma cor base do gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFA000))
        ) {
            BarraTarefas(
                navController = navController,
                currentRoute = NavRoutes.HOME
            )
        }
    }
}

// ==================== COMPONENTES ====================

/**
 * Cabeçalho da tela com logo e notificações
 */
@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo fixa da instituição
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(45.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "OportunyFam",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notificações",
            tint = Color(0xFFFFA000),
            modifier = Modifier.size(25.dp)
        )
    }
}

/**
 * Filtros para alunos
 */
@Composable
private fun FiltrosAlunos(
    statusSelecionado: Int?,
    onStatusChange: (Int?) -> Unit,
    atividadeSelecionada: Int?,
    onAtividadeChange: (Int?) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = "Filtrar por status:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Botão "Todos"
            FilterChip(
                selected = statusSelecionado == null,
                onClick = { onStatusChange(null) },
                label = {
                    Text(
                        text = "Todos",
                        fontSize = 10.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // Filtros de status (exceto "Sugerida Pela Criança" que é id 1)
            listOf(
                StatusInscricao.CANCELADA,
                StatusInscricao.PENDENTE,
                StatusInscricao.APROVADA,
                StatusInscricao.NEGADA
            ).forEach { status ->
                FilterChip(
                    selected = statusSelecionado == status.id,
                    onClick = {
                        onStatusChange(if (statusSelecionado == status.id) null else status.id)
                    },
                    label = {
                        Text(
                            text = status.nome,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Indicador de carregamento
 */
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFFFA000))
    }
}

/**
 * Mensagem de erro
 */
@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Mensagem quando não há alunos
 */
@Composable
private fun EmptyStateMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📚",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nenhum aluno no momento",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Os alunos inscritos aparecerão aqui",
                fontSize = 14.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Card de Aluno com informações da inscrição
 */
@Composable
fun AlunoCard(
    aluno: Aluno,
    navController: NavHostController?,
    instituicaoId: Int?,
    pessoaId: Int? // ✅ Adiciona pessoaId
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { showDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = aluno.crianca_foto,
                contentDescription = "Foto do aluno",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.perfil),
                error = painterResource(id = R.drawable.perfil),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(aluno.crianca_nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    aluno.atividade_titulo,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                // Badge de status
                Surface(
                    color = when (aluno.status_id) {
                        3 -> Color(0xFFFFA000).copy(alpha = 0.2f) // Pendente
                        4 -> Color(0xFF4CAF50).copy(alpha = 0.2f) // Aprovada
                        2 -> Color(0xFF9E9E9E).copy(alpha = 0.2f) // Cancelada
                        5 -> Color(0xFFF44336).copy(alpha = 0.2f) // Negada
                        else -> Color.LightGray
                    },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = aluno.status_inscricao,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = when (aluno.status_id) {
                            3 -> Color(0xFFFFA000) // Pendente
                            4 -> Color(0xFF4CAF50) // Aprovada
                            2 -> Color(0xFF9E9E9E) // Cancelada
                            5 -> Color(0xFFF44336) // Negada
                            else -> Color.Gray
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(
                        when (aluno.status_id) {
                            3 -> Color(0xFFFFA000) // Pendente
                            4 -> Color(0xFF4CAF50) // Aprovada
                            2 -> Color(0xFF9E9E9E) // Cancelada
                            5 -> Color(0xFFF44336) // Negada
                            else -> Color.LightGray
                        }
                    )
            )
        }
    }

    // Diálogo de detalhes
    if (showDialog) {
        DetalhesAlunoDialog(
            aluno = aluno,
            navController = navController,
            instituicaoId = instituicaoId,
            pessoaId = pessoaId,
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Diálogo com detalhes completos do aluno
 */
@Composable
fun DetalhesAlunoDialog(
    aluno: Aluno,
    navController: NavHostController?,
    instituicaoId: Int?,
    pessoaId: Int?, // ✅ Adiciona pessoaId como parâmetro
    onDismiss: () -> Unit
) {
    var isLoadingConversa by remember { mutableStateOf(false) }
    var errorMensagem by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto de perfil
                AsyncImage(
                    model = aluno.crianca_foto,
                    contentDescription = "Foto do aluno",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.perfil),
                    error = painterResource(id = R.drawable.perfil),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Nome
                Text(
                    aluno.crianca_nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Informações detalhadas
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetalheItem("Atividade", aluno.atividade_titulo)
                    DetalheItem("Status", aluno.status_inscricao)
                    DetalheItem(
                        "Data de Inscrição",
                        aluno.data_inscricao.substring(0, 10) // Extrai apenas a data
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mensagem de erro
                errorMensagem?.let { erro ->
                    Text(
                        text = erro,
                        color = Color.Red,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Botão de Mensagem (sempre disponível se houver crianca_id)
                if (pessoaId != null) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoadingConversa = true
                                errorMensagem = null

                                try {
                                    // 1️⃣ Buscar a criança para obter o pessoa_id
                                    val criancaService = RetrofitFactory().getCriancaService()
                                    val criancaResponse = withContext(Dispatchers.IO) {
                                        criancaService.buscarPorId(aluno.crianca_id).execute()
                                    }

                                    if (!criancaResponse.isSuccessful || criancaResponse.body()?.crianca == null) {
                                        errorMensagem = "Erro ao buscar dados da criança"
                                        return@launch
                                    }

                                    val criancaPessoaId = criancaResponse.body()!!.crianca!!.pessoa_id
                                    val conversaService = RetrofitFactory().getConversaService()

                                    Log.d("DetalhesAlunoDialog", "🔍 Buscando conversa existente entre pessoa $pessoaId e criança $criancaPessoaId")

                                    // 2️⃣ PRIMEIRO: Buscar conversas existentes do perfil logado
                                    val buscarResponse = withContext(Dispatchers.IO) {
                                        conversaService.buscarPorIdPessoa(pessoaId)
                                    }

                                    if (buscarResponse.isSuccessful) {
                                        val conversas = buscarResponse.body()?.getConversasList()
                                        val conversaExistente = conversas?.find { conversa ->
                                            conversa.outro_participante.id == criancaPessoaId
                                        }

                                        if (conversaExistente != null) {
                                            // ✅ Conversa JÁ EXISTE - Reutilizar
                                            Log.d("DetalhesAlunoDialog", "✅ Conversa existente encontrada: ID=${conversaExistente.id_conversa}")
                                            navController?.navigate(
                                                "${NavRoutes.CHAT}/${conversaExistente.id_conversa}/${aluno.crianca_nome}/$pessoaId"
                                            )
                                            onDismiss()
                                            return@launch
                                        }
                                    }

                                    // 3️⃣ NÃO EXISTE: Criar nova conversa
                                    Log.d("DetalhesAlunoDialog", "➕ Conversa não existe. Criando nova...")
                                    val criarRequest = ConversaRequest(
                                        participantes = listOf(pessoaId, criancaPessoaId)
                                    )

                                    val criarResponse = withContext(Dispatchers.IO) {
                                        conversaService.criar(criarRequest)
                                    }

                                    if (criarResponse.isSuccessful) {
                                        val conversaId = criarResponse.body()?.conversa?.id
                                        if (conversaId != null) {
                                            Log.d("DetalhesAlunoDialog", "✅ Nova conversa criada: ID=$conversaId")
                                            navController?.navigate(
                                                "${NavRoutes.CHAT}/$conversaId/${aluno.crianca_nome}/$pessoaId"
                                            )
                                            onDismiss()
                                        } else {
                                            errorMensagem = "Erro ao criar conversa"
                                        }
                                    } else {
                                        errorMensagem = "Erro ao criar conversa: ${criarResponse.code()}"
                                        Log.e("DetalhesAlunoDialog", "❌ Erro: ${criarResponse.errorBody()?.string()}")
                                    }
                                } catch (e: Exception) {
                                    errorMensagem = "Erro: ${e.message}"
                                    Log.e("DetalhesAlunoDialog", "❌ Exceção ao criar conversa", e)
                                } finally {
                                    isLoadingConversa = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoadingConversa,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        if (isLoadingConversa) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("💬 Enviar Mensagem")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Botão Fechar
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA000)
                    )
                ) {
                    Text("Fechar")
                }
            }
        }
    }
}

/**
 * Item de detalhe (Label + Valor)
 */
@Composable
private fun DetalheItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

/**
 * Formata hora de HH:mm:ss ou 1970-01-01THH:mm:ss.SSSZ para HH:mm
 */
private fun formatarHora(hora: String): String {
    return try {
        // Se vier com timestamp completo (1970-01-01T09:00:00.000Z)
        if (hora.contains("T")) {
            val horaParte = hora.split("T")[1]
            horaParte.substring(0, 5) // HH:mm
        } else {
            // Se vier como HH:mm:ss
            hora.substring(0, 5) // HH:mm
        }
    } catch (e: Exception) {
        hora // Retorna original se der erro
    }
}

/**
 * Formata data para exibição em português
 * Ex: 2025-11-24 → Segunda, 24 de Novembro
 */
private fun formatarDataExibicao(data: LocalDate): String {
    return try {
        val diaSemana = when (data.dayOfWeek.value) {
            1 -> "Segunda"
            2 -> "Terça"
            3 -> "Quarta"
            4 -> "Quinta"
            5 -> "Sexta"
            6 -> "Sábado"
            7 -> "Domingo"
            else -> ""
        }

        val mes = when (data.monthValue) {
            1 -> "Janeiro"
            2 -> "Fevereiro"
            3 -> "Março"
            4 -> "Abril"
            5 -> "Maio"
            6 -> "Junho"
            7 -> "Julho"
            8 -> "Agosto"
            9 -> "Setembro"
            10 -> "Outubro"
            11 -> "Novembro"
            12 -> "Dezembro"
            else -> ""
        }

        "$diaSemana, ${data.dayOfMonth} de $mes"
    } catch (e: Exception) {
        data.toString()
    }
}

