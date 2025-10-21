package com.example.Telas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val online: Boolean
)

// ------------------- Tela principal de conversas -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversasScreen() {
    val conversas = remember {
        listOf(
            Conversa("Laura de Andrade", "Olá, fico feliz que tenha entrado em contato!", "10:24", R.drawable.perfil, true),
            Conversa("Instituto Aprender", "Temos vagas disponíveis para depois do feriado.", "Ontem", R.drawable.perfil, true),
            Conversa("Escola Esperança", "As inscrições para o reforço escolar já estão abertas!", "Ontem", R.drawable.perfil, false)
        )
    }

    Scaffold(
        topBar = { ConversasTopBar() },
        bottomBar = { BarraDeTarefas() },
        containerColor = Color(0xFFF5F5F5) // Background leve
    ) { padding ->
        ConversaList(conversas = conversas, modifier = Modifier.padding(padding))
    }
}

// ------------------- TopBar elegante -------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversasTopBar() {
    TopAppBar(
        title = { Text("Conversas", fontWeight = FontWeight.SemiBold, fontSize = 22.sp) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFFF7B733),
            titleContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = { /* ação futura */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    tint = Color.White
                )
            }
        }
    )
}

// ------------------- Lista de conversas -------------------
@Composable
fun ConversaList(conversas: List<Conversa>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(conversas) { conversa ->
            ConversaItem(conversa)
        }
    }
}

// ------------------- Item de conversa aprimorado -------------------
@Composable
fun ConversaItem(conversa: Conversa) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navegação futura */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Image(
                    painter = painterResource(id = conversa.imagem),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                )
                if (conversa.online) {
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                            .align(Alignment.BottomEnd)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(conversa.nome, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    conversa.mensagem,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                conversa.hora,
                color = Color(0xFF616161),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

// ------------------- Preview -------------------
@Preview(showBackground = true)
@Composable
fun PreviewConversasScreen() {
    ConversasScreen()
}
