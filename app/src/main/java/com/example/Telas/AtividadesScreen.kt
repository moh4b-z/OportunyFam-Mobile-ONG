package com.example.oportunyfam.Telas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import com.example.oportunyfam_mobile_ong.R

// -------------------- ATIVIDADES SCREEN --------------------
@Composable
fun AtividadesScreen() {

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
                    onClick = { /* abrir detalhes da atividade */ }
                )
            }
        }
    }
}

// -------------------- CARD DE ATIVIDADE --------------------
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
                Text("Clique para ver detalhes", fontSize = 14.sp, color = Color.Gray)
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

// -------------------- DIÁLOGO DE DETALHES DA ATIVIDADE --------------------
@Composable
fun DetalhesAtividadeDialog(
    atividade: String,
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
                Text(atividade, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // Calendário visual (simples)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Calendário visual", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Horários visuais
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Horários das aulas:", fontWeight = FontWeight.Bold)
                    listOf("10:00 - 12:00", "14:00 - 16:00").forEach { horario ->
                        Text(horario, color = Color.Gray, modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dias da semana (visuais)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Dias da semana:", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Seg", "Ter", "Qua", "Qui", "Sex").forEach { dia ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dia, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Alunos cadastrados (simples)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Alunos cadastrados:", fontWeight = FontWeight.Bold)
                    listOf("Maria Oliveira", "João Silva", "Ana Souza").forEach { aluno ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            AsyncImage(
                                model = null,
                                contentDescription = aluno,
                                placeholder = painterResource(id = R.drawable.instituicao),
                                modifier = Modifier.size(40.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(aluno, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
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

// -------------------- PREVIEW --------------------
@Preview(showBackground = true)
@Composable
fun PreviewAtividadesScreen() {
    AtividadesScreen()
}
