package com.oportunyfam_mobile_ong.oportunyfam.Telas

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile_ong.Components.BarraTarefas
import com.oportunyfam_mobile_ong.MainActivity.NavRoutes
import com.oportunyfam_mobile_ong.data.InstituicaoAuthDataStore
import com.oportunyfam_mobile_ong.oportunyfam.model.AtividadeResponse
import com.oportunyfam_mobile_ong.viewmodel.AtividadeViewModel
import com.oportunyfam_mobile_ong.viewmodel.AtividadesState
import com.oportunyfam_mobile_ong.viewmodel.AtividadeDetalheState

// ==================== ENUMS E ESTADOS ====================

/**
 * Estados possíveis da tela de atividades
 */
enum class TelaAtividade {
    LISTA,
    DETALHES,
    ALUNOS,
    CALENDARIO,
    CONFIGURACOES
}

// ==================== SCREEN PRINCIPAL ====================

/**
 * AtividadesScreen - Tela de gerenciamento de atividades
 *
 * Carrega atividades da API e permite:
 * - Listar todas as atividades da instituição
 * - Ver detalhes de cada atividade
 * - Gerenciar alunos por atividade
 * - Visualizar calendário de aulas
 * - Configurar atividades
 *
 * @param navController Controlador de navegação
 */
@Composable
fun AtividadesScreen(navController: NavHostController?) {
    val context = LocalContext.current
    val instituicaoAuthDataStore = remember { InstituicaoAuthDataStore(context) }
    val instituicao by instituicaoAuthDataStore.instituicaoStateFlow().collectAsState(initial = null)

    val viewModel: AtividadeViewModel = viewModel()
    var telaAtual by remember { mutableStateOf(TelaAtividade.LISTA) }
    var atividadeSelecionadaId by remember { mutableStateOf<Int?>(null) }

    // Carregar atividades da instituição quando a tela for exibida
    LaunchedEffect(instituicao) {
        instituicao?.let {
            Log.d("AtividadesScreen", "Carregando atividades da instituição: ${it.instituicao_id}")
            viewModel.buscarAtividadesPorInstituicao(it.instituicao_id)
        }
    }

    when (telaAtual) {
        TelaAtividade.LISTA -> {
            ListaAtividadesScreen(
                navController = navController,
                viewModel = viewModel,
                instituicaoId = instituicao?.instituicao_id,
                onAtividadeClick = { atividadeId ->
                    atividadeSelecionadaId = atividadeId
                    viewModel.buscarAtividadePorId(atividadeId)
                    telaAtual = TelaAtividade.DETALHES
                }
            )
        }
        TelaAtividade.DETALHES -> {
            atividadeSelecionadaId?.let { id ->
                DetalhesAtividadeScreen(
                    viewModel = viewModel,
                    atividadeId = id,
                    onBack = {
                        viewModel.limparDetalhe()
                        telaAtual = TelaAtividade.LISTA
                    },
                    onVerAlunos = { telaAtual = TelaAtividade.ALUNOS },
                    onVerCalendario = { telaAtual = TelaAtividade.CALENDARIO },
                    onConfiguracoes = { telaAtual = TelaAtividade.CONFIGURACOES }
                )
            }
        }
        TelaAtividade.ALUNOS -> {
            atividadeSelecionadaId?.let { id ->
                GerenciarAlunosScreen(
                    atividadeId = id,
                    onBack = { telaAtual = TelaAtividade.DETALHES }
                )
            }
        }
        TelaAtividade.CALENDARIO -> {
            atividadeSelecionadaId?.let { id ->
                CalendarioAulasScreen(
                    viewModel = viewModel,
                    atividadeId = id,
                    onBack = { telaAtual = TelaAtividade.DETALHES }
                )
            }
        }
        TelaAtividade.CONFIGURACOES -> {
            atividadeSelecionadaId?.let { id ->
                ConfiguracoesAtividadeScreen(
                    atividadeId = id,
                    onBack = { telaAtual = TelaAtividade.DETALHES }
                )
            }
        }
    }
}

// ==================== TELAS INTERNAS ====================

/**
 * Tela de lista de atividades (consumindo da API)
 */
@Composable
fun ListaAtividadesScreen(
    navController: NavHostController?,
    viewModel: AtividadeViewModel,
    instituicaoId: Int?,
    onAtividadeClick: (Int) -> Unit
) {
    val atividadesState by viewModel.atividadesState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(
                text = "Minhas Atividades",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (atividadesState) {
                is AtividadesState.Loading -> {
                    // Estado de carregamento
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFFA000))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Carregando atividades...", color = Color.Gray)
                        }
                    }
                }
                is AtividadesState.Success -> {
                    val atividades = (atividadesState as AtividadesState.Success).atividades

                    if (atividades.isEmpty()) {
                        // Estado vazio
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Nenhuma atividade cadastrada",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Cadastre sua primeira atividade!",
                                    fontSize = 14.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    } else {
                        // Lista de atividades
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(atividades) { atividade ->
                                AtividadeCardAPI(
                                    atividade = atividade,
                                    onClick = { onAtividadeClick(atividade.atividade_id) }
                                )
                            }
                        }
                    }
                }
                is AtividadesState.Error -> {
                    // Estado de erro
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "Erro ao carregar atividades",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (atividadesState as AtividadesState.Error).message,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // Tentar carregar novamente
                                    instituicaoId?.let {
                                        viewModel.buscarAtividadesPorInstituicao(it)
                                    }
                                },
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

        // Barra de Tarefas
        BarraTarefas(
            navController = navController,
            currentRoute = NavRoutes.ATIVIDADES
        )
    }
}

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
    val atividadeDetalheState by viewModel.atividadeDetalheState.collectAsState()

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
        }
    ) { paddingValues ->
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
                        // Card de resumo com dados reais
                        ResumoAtividadeCardAPI(atividade = atividade)
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

/**
 * Tela de gerenciamento de alunos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarAlunosScreen(atividadeId: Int, onBack: () -> Unit) {
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Alunos Matriculados", fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Barra de pesquisa
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Pesquisar aluno...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            // Lista de alunos (exemplo - implementar API depois)
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(getAlunosExemplo()) { aluno ->
                    CardAluno(aluno = aluno)
                }
            }
        }
    }
}

/**
 * Tela de calendário de aulas (com dados da API)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioAulasScreen(
    viewModel: AtividadeViewModel,
    atividadeId: Int,
    onBack: () -> Unit
) {
    val atividadeDetalheState by viewModel.atividadeDetalheState.collectAsState()

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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Adicionar nova aula */ },
                containerColor = Color(0xFFFFA000)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Nova Aula", tint = Color.White)
            }
        }
    ) { paddingValues ->
        when (atividadeDetalheState) {
            is AtividadeDetalheState.Success -> {
                val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                ) {
                    // Título
                    Text(
                        text = "Aulas Cadastradas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )

                    if (atividade.aulas.isEmpty()) {
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
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(atividade.aulas) { aula ->
                                CardAulaAPI(aula = aula)
                            }
                        }
                    }
                }
            }
            else -> {
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

/**
 * Tela de configurações da atividade
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesAtividadeScreen(atividadeId: Int, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            item {
                Text(
                    text = "Configurações da Atividade",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(getConfiguracoesExemplo()) { config ->
                CardConfiguracao(config = config)
            }
        }
    }
}

// ==================== COMPONENTES REUTILIZÁVEIS ====================

/**
 * Card de atividade com dados da API
 */
@Composable
fun AtividadeCardAPI(atividade: AtividadeResponse, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagem da atividade
            if (!atividade.instituicao_foto.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(atividade.instituicao_foto)
                        .crossfade(true)
                        .build(),
                    contentDescription = atividade.titulo,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.instituicao),
                    error = painterResource(id = R.drawable.instituicao)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.instituicao),
                    contentDescription = atividade.titulo,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    atividade.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    atividade.categoria,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (atividade.gratuita == 1) "Gratuita" else "R$ ${atividade.preco}",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA000),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "• ${atividade.aulas.size} aulas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Indicador visual
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(
                        if (atividade.ativo == 1) Color(0xFFFFA000) else Color.Gray,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * Card de resumo da atividade com dados da API
 */
@Composable
private fun ResumoAtividadeCardAPI(atividade: AtividadeResponse) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Cabeçalho com imagem
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!atividade.instituicao_foto.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(atividade.instituicao_foto)
                            .crossfade(true)
                            .build(),
                        contentDescription = atividade.titulo,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.instituicao),
                        error = painterResource(id = R.drawable.instituicao)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.instituicao),
                        contentDescription = atividade.titulo,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        atividade.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Text(
                        atividade.categoria,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Descrição
            if (!atividade.descricao.isNullOrEmpty()) {
                Text(
                    "Descrição",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    atividade.descricao,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Informações em grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItemSimple(label = "Faixa Etária", value = "${atividade.faixa_etaria_min}-${atividade.faixa_etaria_max} anos")
                InfoItemSimple(
                    label = "Valor",
                    value = if (atividade.gratuita == 1) "Gratuita" else "R$ ${atividade.preco}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItemSimple(label = "Aulas", value = "${atividade.aulas.size}")
                InfoItemSimple(
                    label = "Status",
                    value = if (atividade.ativo == 1) "Ativa" else "Inativa"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItemSimple(label = "Local", value = "${atividade.cidade}, ${atividade.estado}")
            }
        }
    }
}

/**
 * Item de informação simples (sem ícone) para cards de resumo
 */
@Composable
private fun InfoItemSimple(
    label: String,
    value: String
) {
    Column {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Card visual de atividade (antigo - manter para compatibilidade)
 */
@Composable
fun AtividadeCardVisual(titulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
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
            Image(
                painter = painterResource(id = R.drawable.instituicao),
                contentDescription = titulo,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Clique para gerenciar", fontSize = 14.sp, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(Color(0xFFFFA000))
            )
        }
    }
}

/**
 * Card de resumo da atividade
 */
@Composable
private fun ResumoAtividadeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Resumo da Atividade", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Alunos", "24", Icons.Default.Group)
                InfoItem("Próxima Aula", "15/12", Icons.Default.CalendarToday)
                InfoItem("Status", "Ativa", Icons.Default.Settings)
            }
        }
    }
}

/**
 * Opção de gerenciamento clicável
 */
@Composable
fun OpcaoGerenciamento(titulo: String, descricao: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(descricao, fontSize = 14.sp, color = Color.Gray)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.rotate(180f)
            )
        }
    }
}

/**
 * Item de informação com ícone
 */
@Composable
fun InfoItem(
    titulo: String,
    valor: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = titulo, tint = Color(0xFFFFA000))
        Spacer(modifier = Modifier.height(4.dp))
        Text(valor, fontWeight = FontWeight.Bold)
        Text(titulo, fontSize = 12.sp, color = Color.Gray)
    }
}

/**
 * Card de aluno
 */
@Composable
fun CardAluno(aluno: Aluno) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.instituicao),
                contentDescription = aluno.nome,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(aluno.nome, fontWeight = FontWeight.Bold)
                Text(aluno.email, fontSize = 14.sp, color = Color.Gray)
                Text("Desde ${aluno.dataCadastro}", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                aluno.status,
                color = if (aluno.status == "Ativo") Color.Green else Color.Red
            )
        }
    }
}

/**
 * Card de aula com dados da API
 */
@Composable
fun CardAulaAPI(aula: com.oportunyfam_mobile_ong.oportunyfam.model.AulaDetalhe) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (aula.status == "Futura") Color(0xFFFFF8E1) else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone de calendário
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (aula.status == "Futura") Color(0xFFFFA000) else Color.Gray,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    aula.data_aula,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${aula.hora_inicio} - ${aula.hora_fim}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${aula.vagas_disponiveis}/${aula.vagas_total} vagas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Badge de status
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (aula.status == "Futura") Color(0xFF4CAF50) else Color.Gray
            ) {
                Text(
                    aula.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Card de aula (antigo - manter para compatibilidade)
 */
@Composable
fun CardAula(aula: Aula) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(aula.data, fontWeight = FontWeight.Bold)
                Text(
                    aula.horario,
                    color = Color(0xFFFFA000),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(aula.descricao, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${aula.alunosConfirmados} alunos confirmados",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Card de configuração
 */
@Composable
fun CardConfiguracao(config: Configuracao) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(config.titulo, fontWeight = FontWeight.Bold)
                Text(config.descricao, fontSize = 14.sp, color = Color.Gray)
            }
            when (config) {
                is Configuracao.Toggle -> {
                    Switch(
                        checked = config.valor,
                        onCheckedChange = { /* TODO */ },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFA000)
                        )
                    )
                }
                is Configuracao.Info -> {
                    Text(config.valor, color = Color.Gray)
                }
            }
        }
    }
}

/**
 * Placeholder do calendário
 */
@Composable
private fun CalendarioPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Calendário Visual", fontSize = 18.sp, color = Color.Gray)
    }
}

// ==================== MODELOS DE DADOS ====================

/**
 * Modelo de dados para Aluno
 */
data class Aluno(
    val nome: String,
    val email: String,
    val dataCadastro: String,
    val status: String
)

/**
 * Modelo de dados para Aula
 */
data class Aula(
    val data: String,
    val horario: String,
    val descricao: String,
    val alunosConfirmados: Int
)

/**
 * Modelo de dados para Configuração
 */
sealed class Configuracao {
    abstract val titulo: String
    abstract val descricao: String

    data class Toggle(
        override val titulo: String,
        override val descricao: String,
        val valor: Boolean
    ) : Configuracao()

    data class Info(
        override val titulo: String,
        override val descricao: String,
        val valor: String
    ) : Configuracao()
}

// ==================== DADOS DE EXEMPLO ====================

fun getAlunosExemplo(): List<Aluno> = listOf(
    Aluno("Maria Oliveira", "maria@email.com", "15/12/2023", "Ativo"),
    Aluno("João Silva", "joao@email.com", "10/12/2023", "Ativo"),
    Aluno("Ana Souza", "ana@email.com", "05/12/2023", "Inativo")
)

fun getProximasAulasExemplo(): List<Aula> = listOf(
    Aula("20/12/2023", "10:00 - 12:00", "Aula regular de futebol", 15),
    Aula("22/12/2023", "14:00 - 16:00", "Treino técnico", 12),
    Aula("25/12/2023", "09:00 - 11:00", "Amistoso", 18)
)

fun getConfiguracoesExemplo(): List<Configuracao> = listOf(
    Configuracao.Toggle("Atividade Ativa", "Habilitar/desabilitar atividade", true),
    Configuracao.Info("Vagas Disponíveis", "Número máximo de alunos", "25"),
    Configuracao.Toggle("Permitir Novas Matrículas", "Aceitar novos alunos", true),
    Configuracao.Info("Duração da Aula", "Tempo padrão por aula", "2 horas")
)

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
fun PreviewAtividadesScreen() {
    AtividadesScreen(navController = null)
}