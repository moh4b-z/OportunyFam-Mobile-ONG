package com.example.oportunyfam.Telas

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.Components.BarraTarefas
import com.example.Components.CardAviso
import com.example.oportunyfam.Service.RetrofitFactory
import com.example.oportunyfam.model.CriancaRaw
import com.example.oportunyfam_mobile_ong.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// -------------------- HOME SCREEN --------------------
@Composable
fun HomeScreen(navController: NavHostController?) {

    var listaCriancas by remember { mutableStateOf<List<CriancaRaw>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Carrega a lista de crianças da API
    LaunchedEffect(Unit) {
        val service = RetrofitFactory().getCriancaService()
        try {
            val response = withContext(Dispatchers.IO) { service.listarTodas().execute() }
            if (response.isSuccessful) {
                listaCriancas = response.body()?.criancas ?: emptyList()
            } else {
                errorMessage = "Erro ${response.code()} ao buscar crianças"
            }
        } catch (e: Exception) {
            errorMessage = "Falha de conexão: ${e.localizedMessage}"
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(45.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Associação Esperança",
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

        Divider(color = Color.LightGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            item {
                // Atividades fixas
                AtividadeCard("Futebol", "Das 10:00 às 12:00", 18)
                AtividadeCard("Vôlei", "Das 13:00 às 15:00", 12)
                AtividadeCard("Luta", "Das 16:00 às 18:00", 27)

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Gerenciar Alunos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            when {
                isLoading -> item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFFA000))
                    }
                }

                errorMessage != null -> item {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> items(listaCriancas) { crianca ->
                    AlunoCard(
                        crianca = crianca,
                        onExcluir = { c ->
                            val service = RetrofitFactory().getCriancaService()
                            service.deletar(c.id).enqueue(object : Callback<Unit> {
                                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                                    if (response.isSuccessful) {
                                        listaCriancas = listaCriancas.filter { it.id != c.id }
                                    } else {
                                        errorMessage = "Erro ao cancelar inscrição"
                                    }
                                }

                                override fun onFailure(call: Call<Unit>, t: Throwable) {
                                    errorMessage = "Falha de conexão: ${t.localizedMessage}"
                                }
                            })
                        }
                    )
                }
            }
        }

        BarraDeTarefas()
    }
}

// -------------------- CARD DE ATIVIDADES --------------------
@Composable
fun AtividadeCard(titulo: String, horario: String, pessoas: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("🕒 $horario", fontSize = 14.sp, color = Color.Gray)
                Text("👥 $pessoas Pessoas cadastradas", fontSize = 14.sp, color = Color.Gray)
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

// -------------------- ALUNO CARD --------------------
@Composable
fun AlunoCard(crianca: CriancaRaw, onExcluir: (CriancaRaw) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteWarning by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
                model = crianca.foto_perfil,
                contentDescription = "Foto do aluno",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.instituicao),
                error = painterResource(id = R.drawable.instituicao),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showDialog = true }
            ) {
                Text(crianca.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(crianca.data_nascimento ?: "Sem data", fontSize = 14.sp, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(Color(0xFFFFA000))
            )
        }
    }

    // Diálogo de detalhes
    if (showDialog) {
        DetalhesCriancaDialog(
            crianca = crianca,
            onDismiss = { showDialog = false },
            onDeleteClick = { showDeleteWarning = true }
        )
    }

    // CardAviso de confirmação de exclusão
    if (showDeleteWarning) {
        CardAviso(
            pergunta = "Deseja realmente cancelar a inscrição de ${crianca.nome}?",
            onConfirm = {
                onExcluir(crianca)
                showDeleteWarning = false
                showDialog = false // fecha o diálogo após exclusão
            },
            onDismiss = { showDeleteWarning = false },
            onCancel = { showDeleteWarning = false }
        )
    }
}

// -------------------- DETALHES DA CRIANÇA --------------------
@Composable
fun DetalhesCriancaDialog(
    crianca: CriancaRaw,
    onDismiss: () -> Unit,
    onDeleteClick: () -> Unit
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
                AsyncImage(
                    model = crianca.foto_perfil,
                    contentDescription = "Foto do aluno",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.instituicao),
                    error = painterResource(id = R.drawable.instituicao),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(crianca.nome, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Data de Nascimento: ${crianca.data_nascimento ?: "-"}", fontSize = 16.sp)
                    Text("Email: ${crianca.email ?: "-"}", fontSize = 16.sp)
                    Text("CPF: ${crianca.cpf ?: "-"}", fontSize = 16.sp)
                    Text("Sexo: ${crianca.id_sexo ?: "-"}", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botão “Deseja cancelar inscrição?” dentro do diálogo
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Deseja cancelar inscrição?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }


                Spacer(modifier = Modifier.height(12.dp))

                // Botão fechar
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar", color = Color.White)
                }
            }
        }
    }
}

// -------------------- BARRA DE TAREFAS --------------------
@Composable
fun BarraDeTarefas() {
    BarraTarefas()
}

// -------------------- PREVIEWS --------------------
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen(navController = null)
}

@Preview(showBackground = true)
@Composable
fun PreviewAlunoCard() {
    val dummy = CriancaRaw(
        id = 1,
        nome = "Maria Oliveira",
        foto_perfil = null,
        data_nascimento = "2015-09-22",
        email = "maria.oliveira@example.com",
        cpf = "98765432100",
        id_sexo = 2
    )
    AlunoCard(
        crianca = dummy,
        onExcluir = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewAtividadeCard() {
    AtividadeCard(
        titulo = "Futebol",
        horario = "Das 10:00 às 12:00",
        pessoas = 18
    )
}
