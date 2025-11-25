package com.oportunyfam_mobile_ong.Components

import android.app.TimePickerDialog
import android.util.Log
import android.widget.CalendarView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

/**
 * Diálogo para criar uma ou mais aulas com calendário nativo
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarAulaDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        datasSelecionadas: List<String>,
        horaInicio: String,
        horaFim: String,
        vagasTotal: Int
    ) -> Unit
) {
    val context = LocalContext.current

    // Estado do calendário - datas selecionadas
    var datasSelecionadas by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Horários
    var horaInicio by remember { mutableStateOf("09:00:00") }
    var horaFim by remember { mutableStateOf("10:00:00") }

    // Vagas
    var vagasTotal by remember { mutableStateOf("10") }

    // Formato de data
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Cabeçalho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Cadastrar Aulas",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.Gray)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Calendário nativo do Android
                    Text(
                        "Selecione as datas das aulas:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "Toque em cada data que deseja adicionar",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // CalendarView nativo do Android
                    AndroidView(
                        factory = { ctx ->
                            CalendarView(ctx).apply {
                                // Configurar calendário
                                minDate = System.currentTimeMillis() // Não permitir datas passadas

                                // Listener de mudança de data
                                setOnDateChangeListener { _, year, month, dayOfMonth ->
                                    val calendar = Calendar.getInstance()
                                    calendar.set(year, month, dayOfMonth)
                                    val dataSelecionada = dateFormat.format(calendar.time)

                                    // Toggle: adicionar ou remover data
                                    datasSelecionadas = if (datasSelecionadas.contains(dataSelecionada)) {
                                        datasSelecionadas - dataSelecionada
                                    } else {
                                        datasSelecionadas + dataSelecionada
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mostrar datas selecionadas
                    if (datasSelecionadas.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "Datas selecionadas (${datasSelecionadas.size}):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA000)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                datasSelecionadas.sorted().forEach { data ->
                                    val calendar = Calendar.getInstance()
                                    calendar.time = dateFormat.parse(data) ?: Date()
                                    Text(
                                        "• ${displayDateFormat.format(calendar.time)}",
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Horários
                    Text(
                        "Horários:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hora de início
                        OutlinedButton(
                            onClick = {
                                val timeParts = horaInicio.split(":")
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        horaInicio = String.format(Locale.getDefault(), "%02d:%02d:00", hour, minute)
                                    },
                                    timeParts[0].toInt(),
                                    timeParts[1].toInt(),
                                    true
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(horaInicio.substring(0, 5), fontSize = 14.sp)
                        }

                        Text("até", modifier = Modifier.align(Alignment.CenterVertically))

                        // Hora de fim
                        OutlinedButton(
                            onClick = {
                                val timeParts = horaFim.split(":")
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        horaFim = String.format(Locale.getDefault(), "%02d:%02d:00", hour, minute)
                                    },
                                    timeParts[0].toInt(),
                                    timeParts[1].toInt(),
                                    true
                                ).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(horaFim.substring(0, 5), fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vagas totais
                    Text(
                        "Vagas totais:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = vagasTotal,
                        onValueChange = { vagasTotal = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ex: 10") },
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botões de ação
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val vagas = vagasTotal.toIntOrNull() ?: 10

                            // ✅ Log para verificar dados coletados do UI
                            Log.d("CriarAulaDialog", "📝 Dados coletados:")
                            Log.d("CriarAulaDialog", "  📅 Datas selecionadas: ${datasSelecionadas.sorted().joinToString(", ")}")
                            Log.d("CriarAulaDialog", "  ⏰ Hora início: $horaInicio")
                            Log.d("CriarAulaDialog", "  ⏰ Hora fim: $horaFim")
                            Log.d("CriarAulaDialog", "  👥 Vagas: $vagas")

                            onConfirm(
                                datasSelecionadas.sorted(),
                                horaInicio,
                                horaFim,
                                vagas
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                        enabled = datasSelecionadas.isNotEmpty() && vagasTotal.isNotEmpty()
                    ) {
                        Text("Criar ${if (datasSelecionadas.size > 1) "${datasSelecionadas.size} Aulas" else "Aula"}")
                    }
                }
            }
        }
    }
}
