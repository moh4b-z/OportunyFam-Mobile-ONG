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
import com.example.oportunyfam.Telas.BarraDeTarefas
import com.example.oportunyfam_mobile_ong.R

// ------------------- Modelo de conversa -------------------
data class Conversa(
    val nome: String,
    val mensagem: String,
    val hora: String,
    val imagem: Int,
    val online: Boolean,
    val mensagensNaoLidas: Int = 0
)

// ------------------- Tela principal de conversas premium -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversasScreen() {
    val conversas = remember {
        listOf(
            Conversa("Laura de Andrade", "Olá! Que bom que entrou em contato!", "10:24", R.drawable.perfil, true, 2),
            Conversa("Instituto Aprender", "Temos vagas após o feriado.", "Ontem", R.drawable.perfil, true, 0),
            Conversa("Escola Esperança", "Inscrições para reforço escolar abertas!", "Ontem", R.drawable.perfil, false, 1)
        )
    }

    Scaffold(
        topBar = { ConversasTopBarPremium() },
        bottomBar = { BarraDeTarefas() },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        ConversaListPremium(conversas = conversas, modifier = Modifier.padding(padding))
    }
}

// ------------------- TopBar degradê -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversasTopBarPremium() {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFF57C00))
    )
    TopAppBar(
        title = { Text("Conversas", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        ),
        modifier = Modifier
            .background(gradient)
            .fillMaxWidth(),
        actions = {
            IconButton(onClick = { /* ação */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    tint = Color.White
                )
            }
        }
    )
}

// ------------------- Lista de conversas premium -------------------
@Composable
fun ConversaListPremium(conversas: List<Conversa>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(conversas) { conversa ->
            ConversaItemPremium(conversa)
        }
    }
}

// ------------------- Avatar com borda degradê animada -------------------
@Composable
fun AvatarDegrade(imagem: Int, online: Boolean) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFF57C00), Color(0xFFFFA726)),
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

// ------------------- Item de conversa premium -------------------
@Composable
fun ConversaItemPremium(conversa: Conversa) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navegação futura */ },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarDegrade(imagem = conversa.imagem, online = conversa.online)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(conversa.nome, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    conversa.mensagem,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(conversa.hora, color = Color(0xFF616161), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                if (conversa.mensagensNaoLidas > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(20.dp)
                            .background(Color(0xFFD32F2F), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${conversa.mensagensNaoLidas}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ------------------- Preview premium -------------------
@Preview(showBackground = true)
@Composable
fun PreviewConversasScreen() {
    ConversasScreen()
}
