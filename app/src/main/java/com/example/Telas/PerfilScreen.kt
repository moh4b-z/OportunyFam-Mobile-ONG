package com.example.Telas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.Components.BarraTarefas
import com.example.data.AuthDataStore
import com.example.oportunyfam.model.Instituicao
import com.example.oportunyfam_mobile_ong.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    navController: NavHostController?,
    onLogout: () -> Unit // Função para lidar com o Logout
) {
    val context = LocalContext.current
    val authDataStore = remember { AuthDataStore(context) }
    val scope = rememberCoroutineScope()

    // Estado para armazenar os dados da instituição logada
    var instituicao by remember { mutableStateOf<Instituicao?>(null) }
    val isLoadingData = remember { mutableStateOf(true) }

    // Efeito para carregar os dados do DataStore quando a tela é iniciada
    LaunchedEffect(Unit) {
        scope.launch {
            // ✨ CORREÇÃO: Usando a função loadInstituicao() do AuthDataStore.
            instituicao = authDataStore.loadInstituicao()
            isLoadingData.value = false
        }
    }

    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),  // Laranja original
            Color(0xFFFFD27A)   // Laranja claro suave
        )
    )

    // Se os dados estiverem carregando, exibe um indicador
    if (isLoadingData.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    // Se não houver dados logados, redireciona (chamando onLogout)
    if (instituicao == null) {
        LaunchedEffect(Unit) {
            onLogout()
        }
        return
    }

    // Dados extraídos da variável de estado
    val instituicaoNome = instituicao?.nome ?: "Instituição Não Encontrada"
    val instituicaoEmail = instituicao?.email ?: "email@exemplo.com"


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController?.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            // Botão de Logout que chama a função de limpeza de dados e navegação
            IconButton(onClick = { onLogout() }) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Sair", tint = Color.Black)
            }
            IconButton(onClick = { /* Ação para Notificações */ }) {
                Icon(Icons.Filled.Notifications, contentDescription = "Notificações", tint = Color.Black)
            }
            IconButton(onClick = { /* Ação para Menu */ }) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
            }
        }

        Divider(color = Color.LightGray, thickness = 1.5.dp)

        // Conteúdo principal
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Card branco
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 90.dp) // Espaço para a imagem
                ) {
                    // Informações do perfil
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Nome da Instituição (DADO DINÂMICO)
                        Text(
                            instituicaoNome,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Email da Instituição (DADO DINÂMICO)
                        Text(
                            instituicaoEmail,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats (FOLLOWING)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "127",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "FOLLOWING",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Imagem da Instituição (assumindo que R.drawable.instituicao existe)
                            Image(
                                painter = painterResource(id = R.drawable.instituicao),
                                contentDescription = "Imagem Perfil da Instituição",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))

                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Descrição da Instituição (usando dado do modelo, se existir)
                            Text(
                                instituicao?.descricao ?: "Nenhuma descrição disponível. Clique para editar seu perfil.",
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f) // ocupa o espaço restante
                            )
                        }


                        Spacer(modifier = Modifier.height(32.dp))

                        // Divisor
                        Divider(
                            color = Color.LightGray,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))


                    }
                }
            }

            // Imagem de perfil centralizada
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset(y = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(70.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    // Imagem de Perfil Pessoal
                    Image(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = "Imagem Perfil Pessoal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(150.dp)
                    )
                }
            }
        }

        // Barra de Tarefas
        BarraTarefas()
    }
}

@Preview(showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    PerfilScreen(
        navController = null,
        onLogout = {}
    )
}