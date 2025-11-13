package com.oportunyfam_mobile_ong.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile_ong.model.Categoria

/**
 * Diálogo para criar nova atividade
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarAtividadeDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        titulo: String,
        descricao: String,
        categoriaId: Int,
        faixaEtariaMin: Int,
        faixaEtariaMax: Int,
        gratuita: Boolean,
        preco: Double
    ) -> Unit,
    categorias: List<Categoria> = emptyList(), // ✅ Categorias da API
    isLoading: Boolean = false
) {
    var titulo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var categoriaId by remember { mutableStateOf(if (categorias.isNotEmpty()) categorias[0].id else 1) }
    var faixaEtariaMin by remember { mutableStateOf("") }
    var faixaEtariaMax by remember { mutableStateOf("") }
    var gratuita by remember { mutableStateOf(true) }
    var preco by remember { mutableStateOf("") }
    var expandedCategorias by remember { mutableStateOf(false) }


    val isFormValid = titulo.isNotBlank() &&
            titulo.length <= 100 &&
            descricao.isNotBlank() &&
            descricao.length >= 10 &&
            descricao.length <= 500 &&
            faixaEtariaMin.isNotBlank() &&
            faixaEtariaMax.isNotBlank() &&
            faixaEtariaMin.toIntOrNull() != null &&
            faixaEtariaMax.toIntOrNull() != null &&
            faixaEtariaMin.toInt() <= faixaEtariaMax.toInt() &&
            faixaEtariaMin.toInt() >= 0 &&
            faixaEtariaMax.toInt() <= 99 &&
            (gratuita || (preco.isNotBlank() && preco.toDoubleOrNull() != null && preco.toDouble() > 0))

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                "Nova Atividade",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { if (it.length <= 100) titulo = it },
                    label = { Text("Título da Atividade *") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    placeholder = { Text("Ex: Aula de Futebol") },
                    supportingText = {
                        Text(
                            "${titulo.length}/100 caracteres",
                            fontSize = 12.sp,
                            color = if (titulo.length > 100) Color.Red else Color.Gray
                        )
                    }
                )

                // Descrição
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { if (it.length <= 500) descricao = it },
                    label = { Text("Descrição *") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 3,
                    maxLines = 5,
                    placeholder = { Text("Descreva a atividade... (mínimo 10 caracteres)") },
                    supportingText = {
                        Text(
                            "${descricao.length}/500 caracteres${if (descricao.isNotEmpty() && descricao.length < 10) " (mínimo 10)" else ""}",
                            fontSize = 12.sp,
                            color = if (descricao.length > 500 || (descricao.isNotEmpty() && descricao.length < 10)) Color.Red else Color.Gray
                        )
                    },
                    isError = descricao.isNotEmpty() && descricao.length < 10
                )

                // Categoria (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = expandedCategorias,
                    onExpandedChange = { if (!isLoading) expandedCategorias = !expandedCategorias }
                ) {
                    OutlinedTextField(
                        value = categorias.find { it.id == categoriaId }?.nome ?:
                                if (categorias.isEmpty()) "Carregando..." else "Selecione",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria *") },
                        trailingIcon = {
                            Icon(
                                Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading && categorias.isNotEmpty()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategorias,
                        onDismissRequest = { expandedCategorias = false }
                    ) {
                        categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria.nome) },
                                onClick = {
                                    categoriaId = categoria.id
                                    expandedCategorias = false
                                }
                            )
                        }
                    }
                }

                // Faixa Etária
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = faixaEtariaMin,
                        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) faixaEtariaMin = it },
                        label = { Text("Idade Min *") },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("6") }
                    )

                    OutlinedTextField(
                        value = faixaEtariaMax,
                        onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) faixaEtariaMax = it },
                        label = { Text("Idade Max *") },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("12") }
                    )
                }

                // Gratuita?
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Atividade Gratuita?",
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Switch(
                        checked = gratuita,
                        onCheckedChange = { gratuita = it },
                        enabled = !isLoading,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFFA000)
                        )
                    )
                }

                // Preço (se não for gratuita)
                if (!gratuita) {
                    OutlinedTextField(
                        value = preco,
                        onValueChange = {
                            // Permite apenas números e ponto decimal
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                preco = it
                            }
                        },
                        label = { Text("Preço (R$) *") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        placeholder = { Text("50.00") },
                        prefix = { Text("R$ ") }
                    )
                }

                // Nota informativa
                Text(
                    "* Campos obrigatórios\nDescrição deve ter no mínimo 10 caracteres",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        },
        confirmButton = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFFFA000)
                )
            } else {
                TextButton(
                    onClick = {
                        onConfirm(
                            titulo.trim(),
                            descricao.trim(),
                            categoriaId,
                            faixaEtariaMin.toInt(),
                            faixaEtariaMax.toInt(),
                            gratuita,
                            if (gratuita) 0.0 else preco.toDouble()
                        )
                    },
                    enabled = isFormValid
                ) {
                    Text(
                        "Criar Atividade",
                        color = if (isFormValid) Color(0xFFFFA000) else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

