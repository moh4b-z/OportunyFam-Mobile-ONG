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
    var dataSelecionada by remember { mutableStateOf<LocalDate>(LocalDate.now()) }

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
            val response = withContext(Dispatchers.IO) {
                service.buscarAulasPorInstituicao(id).execute()
            }

            if (response.isSuccessful) {
                listaAulas = response.body()?.aulas ?: emptyList()
                Log.d("HomeScreen", "✅ Carregadas ${listaAulas.size} aulas")
            } else {
                Log.e("HomeScreen", "❌ Erro ao buscar aulas: ${response.code()}")
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
            // Agenda Horizontal
            item {
                Text(
                    text = "Agenda de Aulas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                AgendaHorizontal(
                    aulas = listaAulas,
                    onDateSelected = { data ->
                        dataSelecionada = data
                        Log.d("HomeScreen", "Data selecionada: $data")
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
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
