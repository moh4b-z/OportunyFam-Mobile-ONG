package com.oportunyfam_mobile_ong.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.oportunyfam_mobile_ong.Components.BarraTarefas
import com.oportunyfam_mobile_ong.Components.Cards.AtividadeCardAPI
import com.oportunyfam_mobile_ong.MainActivity
import com.oportunyfam_mobile_ong.viewmodel.AtividadeViewModel
import com.oportunyfam_mobile_ong.viewmodel.AtividadesState
import com.oportunyfam_mobile_ong.R

/**
 * Tela de lista de atividades (consumindo da API)
 */
@Composable
fun ListaAtividadesScreen(
    navController: NavHostController?,
    viewModel: AtividadeViewModel,
    instituicaoId: Int?,
    onAtividadeClick: (Int) -> Unit
) {
    val atividadesState by viewModel.atividadesState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(
                text = "Minhas Atividades",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (atividadesState) {
                is AtividadesState.Loading -> {
                    // Estado de carregamento
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFFFA000))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Carregando atividades...", color = Color.Gray)
                        }
                    }
                }
                is AtividadesState.Success -> {
                    val atividades = (atividadesState as AtividadesState.Success).atividades

                    if (atividades.isEmpty()) {
                        // Estado vazio
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Nenhuma atividade cadastrada",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Cadastre sua primeira atividade!",
                                    fontSize = 14.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    } else {
                        // Lista de atividades
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(atividades) { atividade ->
                                AtividadeCardAPI(
                                    atividade = atividade,
                                    onClick = { onAtividadeClick(atividade.atividade_id) }
                                )
                            }
                        }
                    }
                }
                is AtividadesState.Error -> {
                    // Estado de erro
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "Erro ao carregar atividades",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                (atividadesState as AtividadesState.Error).message,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    // Tentar carregar novamente
                                    instituicaoId?.let {
                                        viewModel.buscarAtividadesPorInstituicao(it)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFA000)
                                )
                            ) {
                                Text("Tentar Novamente", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Barra de Tarefas
        BarraTarefas(
            navController = navController,
            currentRoute = MainActivity.NavRoutes.ATIVIDADES
        )
    }
}