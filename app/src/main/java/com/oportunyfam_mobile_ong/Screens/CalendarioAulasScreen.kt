package com.oportunyfam_mobile_ong.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import com.oportunyfam_mobile_ong.Components.Cards.CardAulaAPI
import com.oportunyfam_mobile_ong.viewmodel.AtividadeDetalheState
import com.oportunyfam_mobile_ong.viewmodel.AtividadeViewModel
import com.oportunyfam_mobile_ong.R

/**
 * Tela de calendário de aulas (com dados da API)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioAulasScreen(
    viewModel: AtividadeViewModel,
    atividadeId: Int,
    onBack: () -> Unit
) {
    val atividadeDetalheState by viewModel.atividadeDetalheState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Calendário de Aulas", fontWeight = FontWeight.Bold) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Adicionar nova aula */ },
                containerColor = Color(0xFFFFA000)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Nova Aula", tint = Color.White)
            }
        }
    ) { paddingValues ->
        when (atividadeDetalheState) {
            is AtividadeDetalheState.Success -> {
                val atividade = (atividadeDetalheState as AtividadeDetalheState.Success).atividade

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color.White)
                ) {
                    // Título
                    Text(
                        text = "Aulas Cadastradas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )

                    if (atividade.aulas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Nenhuma aula cadastrada", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Clique no + para adicionar", fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(atividade.aulas) { aula ->
                                CardAulaAPI(aula = aula)
                            }
                        }
                    }
                }
            }
            else -> {
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