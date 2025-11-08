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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.oportunyfam_mobile_ong.Components.BarraTarefas
import com.oportunyfam_mobile_ong.MainActivity.NavRoutes
import com.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile_ong.Service.RetrofitFactory
import com.oportunyfam_mobile_ong.data.InstituicaoAuthDataStore
import com.oportunyfam_mobile_ong.model.Aluno
import com.oportunyfam_mobile_ong.model.StatusInscricao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // Filtros
    var statusFiltro by remember { mutableStateOf<Int?>(null) }
    var atividadeFiltro by remember { mutableStateOf<Int?>(null) }

    // Carrega instituição logada
    LaunchedEffect(Unit) {
        val instituicao = authDataStore.loadInstituicao()
        instituicaoId = instituicao?.instituicao_id
    }

    // Carrega a lista de alunos da API
    LaunchedEffect(instituicaoId, statusFiltro, atividadeFiltro) {
        if (instituicaoId == null) return@LaunchedEffect

        isLoading = true
        errorMessage = null

        val service = RetrofitFactory().getInstituicaoService()
        try {
            val response = withContext(Dispatchers.IO) {
                service.buscarAlunos(
                    instituicao_id = instituicaoId,
                    atividade_id = atividadeFiltro,
                    status_id = statusFiltro
                ).execute()
            }

            if (response.isSuccessful) {
                listaAlunos = response.body()?.alunos ?: emptyList()
                Log.d("HomeScreen", "✅ Alunos carregados: ${listaAlunos.size}")
            } else if (response.code() == 404) {
                // 404 significa que não há alunos, não é um erro
                listaAlunos = emptyList()
                Log.d("HomeScreen", "ℹ️ Nenhum aluno encontrado")
            } else {
                errorMessage = "Erro ao buscar alunos"
                Log.e("HomeScreen", "❌ Erro ${response.code()}: ${response.errorBody()?.string()}")
            }
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
            // Título e Filtros
            item {
                Spacer(modifier = Modifier.height(16.dp))

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
                    AlunoCard(aluno = aluno)
                }
            }
        }

        // Barra de tarefas
        BarraTarefas(
            navController = navController,
            currentRoute = NavRoutes.HOME
        )
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
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Filtrar por status:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botão "Todos"
            FilterChip(
                selected = statusSelecionado == null,
                onClick = { onStatusChange(null) },
                label = { Text("Todos") }
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
                    label = { Text(status.nome, fontSize = 12.sp) }
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
fun AlunoCard(aluno: Aluno) {
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
    onDismiss: () -> Unit
) {
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

                Spacer(modifier = Modifier.height(24.dp))

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

