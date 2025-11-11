package com.oportunyfam_mobile_ong.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile_ong.Components.Cards.ResumoAtividadeCardAPI
import com.oportunyfam_mobile_ong.Components.OpcaoGerenciamento
import com.oportunyfam_mobile_ong.viewmodel.AtividadeDetalheState
import com.oportunyfam_mobile_ong.viewmodel.AtividadeViewModel
import com.oportunyfam_mobile_ong.R

/**
 * Tela de detalhes da atividade (com dados da API)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesAtividadeScreen(
    viewModel: AtividadeViewModel,
    atividadeId: Int,
    onBack: () -> Unit,
    onVerAlunos: () -> Unit,
    onVerCalendario: () -> Unit,
    onConfiguracoes: () -> Unit
) {
    val atividadeDetalheState by viewModel.atividadeDetalheState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (atividadeDetalheState) {
                        is AtividadeDetalheState.Success -> {
                            Text(
                                (atividadeDetalheState as AtividadeDetalheState.Success).atividade.titulo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> Text("Detalhes", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        when (atividadeDetalheState) {
            is AtividadeDetalheState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFA000))
                }
            }
            is AtividadeDetalheState.Success -> {
                val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                ) {
                    item {
                        // Card de resumo com dados reais
                        ResumoAtividadeCardAPI(atividade = atividade)
                    }

                    item {
                        // Opções de gerenciamento
                        Column(modifier = Modifier.padding(16.dp)) {
                            OpcaoGerenciamento(
                                titulo = "👥 Gerenciar Alunos",
                                descricao = "Ver e editar alunos cadastrados",
                                onClick = onVerAlunos
                            )

                            OpcaoGerenciamento(
                                titulo = "📅 Calendário de Aulas",
                                descricao = "${atividade.aulas.size} aulas cadastradas",
                                onClick = onVerCalendario
                            )

                            OpcaoGerenciamento(
                                titulo = "⚙️ Configurações",
                                descricao = "Editar informações da atividade",
                                onClick = onConfiguracoes
                            )
                        }
                    }
                }
            }
            is AtividadeDetalheState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Erro ao carregar detalhes", color = Color.Red, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            (atividadeDetalheState as AtividadeDetalheState.Error).message,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            AtividadeDetalheState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFFFA000))
                }
            }
        }
    }
}