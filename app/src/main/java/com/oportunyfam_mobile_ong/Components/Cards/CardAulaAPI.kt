package com.oportunyfam_mobile_ong.Components.Cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile_ong.model.AulaDetalhe
import androidx.compose.ui.tooling.preview.Preview


/**
 * Card de aula com dados da API
 */
@Composable
fun CardAulaAPI(
    aula: AulaDetalhe,
    onDelete: ((Int) -> Unit)? = null
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (aula.status_aula == "Futura") Color(0xFFFFF8E1) else Color(0xFFF5F5F5)
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
                        if (aula.status_aula == "Futura") Color(0xFFFFA000) else Color.Gray,
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
                aula.data_aula?.let {
                    Text(
                        it,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
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

            // Badge de status e botão de excluir
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (aula.status_aula == "Futura") Color(0xFF4CAF50) else Color.Gray
                ) {
                    aula.status_aula?.let {
                        Text(
                            it,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Botão de excluir (somente se onDelete for fornecido)
                if (onDelete != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir aula",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmação de exclusão
    if (showDeleteDialog && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Excluir Aula",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Tem certeza que deseja excluir esta aula?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Data: ${aula.data_aula}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Horário: ${aula.hora_inicio} - ${aula.hora_fim}",
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(aula.aula_id)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Aula Futura")
@Composable
fun PreviewCardAulaAPI_Futura() {
    CardAulaAPI(
        aula = AulaDetalhe(
            aula_id = 1,
            id_atividade = 1,
            data_aula = "22/11/2025",
            hora_inicio = "08:00",
            hora_fim = "09:00",
            vagas_total = 20,
            vagas_disponiveis = 10,
            status_aula = "Futura",
            iram_participar = emptyList(),
            foram = emptyList(),
            ausentes = emptyList(),
            nome_atividade = "Matemática",
            instituicao_nome = "Escola Exemplo"
        )
    )
}

@Preview(showBackground = true, name = "Aula Passada")
@Composable
fun PreviewCardAulaAPI_Passada() {
    CardAulaAPI(
        aula = AulaDetalhe(
            aula_id = 2,
            id_atividade = 1,
            data_aula = "20/11/2025",
            hora_inicio = "10:00",
            hora_fim = "11:00",
            vagas_total = 20,
            vagas_disponiveis = 0,
            status_aula = "Passada",
            iram_participar = emptyList(),
            foram = emptyList(),
            ausentes = emptyList(),
            nome_atividade = "Português",
            instituicao_nome = "Escola Exemplo"
        )
    )
}

