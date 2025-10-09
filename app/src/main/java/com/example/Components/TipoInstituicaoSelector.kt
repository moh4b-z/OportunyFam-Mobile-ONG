package com.example.Components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.model.TipoInstituicao
import com.example.screens.PrimaryColor
import com.example.screens.RegistroOutlinedTextField
import com.example.oportunyfam_mobile_ong.R
import com.example.oportunyfam.Service.RetrofitFactory
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.model.TipoInstituicaoResponse
import android.util.Log // Importação necessária para logging

private const val TAG = "TipoInstituicaoSelector" // Tag para identificação no Logcat

@Composable
fun TipoInstituicaoSelector(
    selectedTypeIds: MutableState<List<Int>>,
    selectedTypeNames: MutableState<String>,
    isEnabled: Boolean,
) {
    val institutionTypes = remember { mutableStateOf<List<TipoInstituicao>>(emptyList()) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val tipoInstituicaoService = remember { RetrofitFactory().getTipoInstituicaoService() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                tipoInstituicaoService.listarTodos().enqueue(object : Callback<TipoInstituicaoResponse> {
                    override fun onResponse(call: Call<TipoInstituicaoResponse>, response: Response<TipoInstituicaoResponse>) {
                        if (response.isSuccessful) {
                            institutionTypes.value = response.body()?.tiposInstituicao ?: emptyList()
                            Log.d(TAG, "Tipos de instituição carregados com sucesso.")
                        } else {
                            // Loga falhas de status HTTP (4xx, 5xx)
                            Log.e(TAG, "Falha ao carregar tipos de instituição: HTTP ${response.code()} - ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<TipoInstituicaoResponse>, t: Throwable) {
                        // CORREÇÃO: Usa Log.e para erros de rede (Timeouts, Host não encontrado, etc.)
                        Log.e(TAG, "ERRO DE CONEXÃO AO CARREGAR TIPOS: ${t.message}", t)
                        // O Log.e com Throwable 't' imprime todo o StackTrace, que é crucial para debug.
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Exceção ao iniciar requisição: ${e.message}", e)
            }
        }
    }

    Box {
        RegistroOutlinedTextField(
            value = selectedTypeNames.value,
            onValueChange = { /* Não permite escrita manual */ },
            label = stringResource(R.string.label_institution_type),
            modifier = Modifier.clickable(enabled = isEnabled) { isDropdownExpanded = true },
            readOnly = true,
            keyboardOptions = KeyboardOptions.Default,
            trailingIcon = {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.desc_icon_dropdown),
                    tint = PrimaryColor
                )
            }
        )

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (institutionTypes.value.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Carregando tipos...", color = Color.Gray) },
                    onClick = { /* Não faz nada */ }
                )
            } else {
                institutionTypes.value.forEach { type ->
                    val isSelected = selectedTypeIds.value.contains(type.id)
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryColor),
                                    enabled = isEnabled
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(type.nome, color = Color.Black)
                            }
                        },
                        onClick = {
                            if (isEnabled) {
                                val currentIds = selectedTypeIds.value.toMutableList()
                                if (currentIds.contains(type.id)) {
                                    currentIds.remove(type.id)
                                } else {
                                    type.id?.let { currentIds.add(it) }
                                }

                                selectedTypeIds.value = currentIds

                                val names = institutionTypes.value
                                    .filter { currentIds.contains(it.id) }
                                    .joinToString(" - ") { it.nome }
                                selectedTypeNames.value = names
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
