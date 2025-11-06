package com.example.Telas

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.Components.BarraTarefas
import com.example.MainActivity.NavRoutes
import com.example.oportunyfam_mobile_ong.R

// ==================== MODELO DE DADOS ====================

/**
 * Modelo de dados para uma conversa
 *
 * @param nome Nome do contato ou instituição
 * @param mensagem Última mensagem recebida
 * @param hora Horário da última mensagem
 * @param imagem ID do recurso da imagem de perfil
 * @param online Status online do contato
 * @param mensagensNaoLidas Quantidade de mensagens não lidas
 */
data class Conversa(
    val nome: String,
    val mensagem: String,
    val hora: String,
    val imagem: Int,
    val online: Boolean,
    val mensagensNaoLidas: Int = 0
)

// ==================== SCREEN PRINCIPAL ====================

/**
 * ConversasScreen - Tela de conversas e mensagens
 *
 * Exibe uma lista de conversas com:
 * - Status online/offline
 * - Contador de mensagens não lidas
 * - Avatar com borda animada
 * - Design premium com gradientes
 *
 * @param navController Controlador de navegação
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversasScreen(navController: NavHostController?) {
    val conversas = remember {
        listOf(
            Conversa(
                nome = "Laura de Andrade",
                mensagem = "Olá! Que bom que entrou em contato!",
                hora = "10:24",
                imagem = R.drawable.perfil,
                online = true,
                mensagensNaoLidas = 2
            ),
            Conversa(
                nome = "Instituto Aprender",
                mensagem = "Temos vagas após o feriado.",
                hora = "Ontem",
                imagem = R.drawable.perfil,
                online = true,
                mensagensNaoLidas = 0
            ),
            Conversa(
                nome = "Escola Esperança",
                mensagem = "Inscrições para reforço escolar abertas!",
                hora = "Ontem",
                imagem = R.drawable.perfil,
                online = false,
                mensagensNaoLidas = 1
            )
        )
    }

    Scaffold(
        topBar = { ConversasTopBar() },
        bottomBar = {
            BarraTarefas(
                navController = navController,
                currentRoute = NavRoutes.CONVERSAS
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        ConversaList(
            conversas = conversas,
            modifier = Modifier.padding(padding)
        )
    }
}

// ==================== COMPONENTES ====================

/**
 * TopBar com gradiente laranja
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversasTopBar() {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFF57C00))
    )

    TopAppBar(
        title = {
            Text(
                "Conversas",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        ),
        modifier = Modifier
            .background(gradient)
            .fillMaxWidth(),
        actions = {
            IconButton(onClick = { /* Ação de perfil ou configurações */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    tint = Color.White
                )
            }
        }
    )
}

/**
 * Lista de conversas com espaçamento e padding
 */
@Composable
private fun ConversaList(
    conversas: List<Conversa>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(conversas) { conversa ->
            ConversaItem(conversa)
        }
    }
}

/**
 * Avatar com borda degradê animada e indicador online
 *
 * @param imagem ID do recurso da imagem
 * @param online Se true, exibe indicador verde
 */
@Composable
private fun AvatarComBordaAnimada(imagem: Int, online: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_animation")

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_animation"
    )

    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFA726),
            Color(0xFFF57C00),
            Color(0xFFFFA726)
        ),
        start = Offset(animatedOffset, 0f),
        end = Offset(100f + animatedOffset, 100f)
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .border(width = 3.dp, brush = gradient, shape = CircleShape)
    ) {
        Image(
            painter = painterResource(id = imagem),
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .align(Alignment.Center)
        )

        // Indicador de status online
        if (online) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color(0xFF4CAF50), CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

/**
 * Item individual de conversa com design premium
 *
 * @param conversa Dados da conversa a ser exibida
 */
@Composable
private fun ConversaItem(conversa: Conversa) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navegar para tela de chat */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar com borda animada
            AvatarComBordaAnimada(
                imagem = conversa.imagem,
                online = conversa.online
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Informações da conversa
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversa.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = conversa.mensagem,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Hora e badge de mensagens não lidas
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = conversa.hora,
                    color = Color(0xFF616161),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                // Badge de mensagens não lidas
                if (conversa.mensagensNaoLidas > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(20.dp)
                            .background(Color(0xFFD32F2F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${conversa.mensagensNaoLidas}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==================== PREVIEW ====================

@Preview(showBackground = true)
@Composable
fun PreviewConversasScreen() {
    ConversasScreen(navController = null)
}
