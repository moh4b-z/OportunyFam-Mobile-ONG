package com.example.oportunyfam_mobile_ong.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.oportunyfam_mobile_ong.R

// -------------------- ESTADOS DA TELA --------------------
enum class TelaAtividade {
    LISTA,
    DETALHES,
    ALUNOS,
    CALENDARIO,
    CONFIGURACOES
}


// -------------------- ATIVIDADES SCREEN --------------------
@Composable
fun AtividadesScreen( navController: NavHostController?) {
    var telaAtual by remember { mutableStateOf(TelaAtividade.LISTA) }
    var atividadeSelecionada by remember { mutableStateOf("") }

    when (telaAtual) {
        TelaAtividade.LISTA -> {
            ListaAtividadesScreen(
                onAtividadeClick = { atividade ->
                    atividadeSelecionada = atividade
                    telaAtual = TelaAtividade.DETALHES
                }
            )
        }
        TelaAtividade.DETALHES -> {
            DetalhesAtividadeScreen(
                atividade = atividadeSelecionada,
                onBack = { telaAtual = TelaAtividade.LISTA },
                onVerAlunos = { telaAtual = TelaAtividade.ALUNOS },
                onVerCalendario = { telaAtual = TelaAtividade.CALENDARIO },
                onConfiguracoes = { telaAtual = TelaAtividade.CONFIGURACOES }
            )
        }
        TelaAtividade.ALUNOS -> {
            GerenciarAlunosScreen(
                atividade = atividadeSelecionada,
                onBack = { telaAtual = TelaAtividade.DETALHES }
            )
        }
        TelaAtividade.CALENDARIO -> {
            CalendarioAulasScreen(
                atividade = atividadeSelecionada,
                onBack = { telaAtual = TelaAtividade.DETALHES }
            )
        }
        TelaAtividade.CONFIGURACOES -> {
            ConfiguracoesAtividadeScreen(
                atividade = atividadeSelecionada,
                onBack = { telaAtual = TelaAtividade.DETALHES }
            )
        }
    }
}

// -------------------- LISTA DE ATIVIDADES --------------------
@Composable
fun ListaAtividadesScreen(onAtividadeClick: (String) -> Unit) {
    val atividades = listOf(
        "Futebol",
        "Vôlei",
        "Luta"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Atividades",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(atividades) { atividade ->
                AtividadeCardVisual(
                    titulo = atividade,
                    onClick = { onAtividadeClick(atividade) }
                )
            }
        }
    }
}

// -------------------- DETALHES DA ATIVIDADE --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesAtividadeScreen(
    atividade: String,
    onBack: () -> Unit,
    onVerAlunos: () -> Unit,
    onVerCalendario: () -> Unit,
    onConfiguracoes: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(atividade, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
            // Card de resumo
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

            // Opções de gerenciamento
            Column(modifier = Modifier.padding(16.dp)) {
                OpcaoGerenciamento(
                    titulo = "👥 Gerenciar Alunos",
                    descricao = "Ver e editar alunos cadastrados",
                    onClick = onVerAlunos
                )

                OpcaoGerenciamento(
                    titulo = "📅 Calendário de Aulas",
                    descricao = "Agendar próximas aulas e horários",
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

// -------------------- GERENCIAR ALUNOS --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GerenciarAlunosScreen(atividade: String, onBack: () -> Unit) {
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Alunos - $atividade", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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

            // Lista de alunos
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(getAlunosExemplo()) { aluno ->
                    CardAluno(aluno = aluno)
                }
            }
        }
    }
}

// -------------------- CALENDÁRIO DE AULAS --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioAulasScreen(atividade: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendário - $atividade", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
                Icon(Icons.Default.CalendarToday, contentDescription = "Nova Aula")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Visual do calendário (simples)
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

            // Próximas aulas
            Text(
                text = "Próximas Aulas",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(getProximasAulasExemplo()) { aula ->
                    CardAula(aula = aula)
                }
            }
        }
    }
}

// -------------------- CONFIGURAÇÕES DA ATIVIDADE --------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesAtividadeScreen(atividade: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Configurações - $atividade", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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

// -------------------- COMPONENTES REUTILIZÁVEIS --------------------
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
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.rotate(180f))
        }
    }
}

@Composable
fun InfoItem(titulo: String, valor: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = titulo, tint = Color(0xFFFFA000))
        Spacer(modifier = Modifier.height(4.dp))
        Text(valor, fontWeight = FontWeight.Bold)
        Text(titulo, fontSize = 12.sp, color = Color.Gray)
    }
}

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
            Text(aluno.status, color = if (aluno.status == "Ativo") Color.Green else Color.Red)
        }
    }
}

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
                Text(aula.horario, color = Color(0xFFFFA000), fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(aula.descricao, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${aula.alunosConfirmados} alunos confirmados", fontSize = 12.sp, color = Color.Gray)
        }
    }
}



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
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFA000))
                    )
                }
                is Configuracao.Info -> {
                    Text(config.valor, color = Color.Gray)
                }
            }
        }
    }
}

// -------------------- MODELOS DE DADOS --------------------
data class Aluno(
    val nome: String,
    val email: String,
    val dataCadastro: String,
    val status: String
)

data class Aula(
    val data: String,
    val horario: String,
    val descricao: String,
    val alunosConfirmados: Int
)

sealed class Configuracao {

    abstract val titulo: String
    abstract val descricao: String

    data class Toggle(override val titulo: String, override val descricao: String, val valor: Boolean) : Configuracao()
    data class Info(override val titulo: String, override val descricao: String, val valor: String) : Configuracao()
}

// -------------------- DADOS EXEMPLO --------------------
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

// -------------------- PREVIEW --------------------
@Preview(showBackground = true)
@Composable
fun PreviewAtividadesScreen() {
    AtividadesScreen(navController = null)
}