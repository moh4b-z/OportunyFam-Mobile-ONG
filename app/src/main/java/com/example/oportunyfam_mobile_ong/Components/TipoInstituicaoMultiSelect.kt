package com.example.oportunyfam_mobile_ong.Components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.oportunyfam_mobile_ong.model.TipoInstituicao

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipoInstituicaoMultiSelect(
    tipos: List<TipoInstituicao>,
    onSelectionChanged: (selectedIds: List<Int>) -> Unit
) {
    // Conjunto mutável para rastrear os IDs selecionados
    val selectedIds = remember { mutableStateOf(setOf<Int>()) }

    // Efeito colateral para notificar o componente pai sobre as mudanças
    LaunchedEffect(selectedIds.value) {
        onSelectionChanged(selectedIds.value.toList())
    }

    // Usamos o FlowRow para que os Chips quebrem a linha se necessário
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tipos.forEach { tipo ->
            val isSelected = selectedIds.value.contains(tipo.id)

            FilterChip(
                selected = isSelected,
                onClick = {
                    selectedIds.value = if (isSelected) {
                        selectedIds.value - tipo.id!! // Remove
                    } else {
                        selectedIds.value + tipo.id!! // Adiciona
                    }
                },
                label = { Text(tipo.nome) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Done, contentDescription = "Selecionado", Modifier.size(FilterChipDefaults.IconSize)) }
                } else null,
                // Estilos para Feedback visual
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}