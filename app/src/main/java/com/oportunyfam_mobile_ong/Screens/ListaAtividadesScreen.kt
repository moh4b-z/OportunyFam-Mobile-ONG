package com.oportunyfam_mobile_ong.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.oportunyfam_mobile_ong.Components.BarraTarefas
import com.oportunyfam_mobile_ong.Components.Cards.AtividadeCardAPI
import com.oportunyfam_mobile_ong.Components.CriarAtividadeDialog
import com.oportunyfam_mobile_ong.MainActivity
import com.oportunyfam_mobile_ong.viewmodel.AtividadeViewModel
import com.oportunyfam_mobile_ong.viewmodel.AtividadesState
import com.oportunyfam_mobile_ong.viewmodel.CategoriaViewModel
import com.oportunyfam_mobile_ong.viewmodel.CategoriasState
import com.oportunyfam_mobile_ong.viewmodel.CriarAtividadeState
import kotlinx.coroutines.launch

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
    val criarAtividadeState by viewModel.criarAtividadeState.collectAsState()

    // ✅ ViewModel de categorias
    val categoriaViewModel: CategoriaViewModel = viewModel()
    val categoriasState by categoriaViewModel.categoriasState.collectAsState()

    var showCriarDialog by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observar estado de criação
    LaunchedEffect(criarAtividadeState) {
        when (criarAtividadeState) {
            is CriarAtividadeState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("Atividade criada com sucesso!")
                }
                showCriarDialog = false
                isCreating = false
                viewModel.limparEstadoCriacao()
            }
            is CriarAtividadeState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        (criarAtividadeState as CriarAtividadeState.Error).message
                    )
                }
                isCreating = false
                viewModel.limparEstadoCriacao()
            }
            is CriarAtividadeState.Loading -> {
                isCreating = true
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Botão FAB para criar atividade
            if (instituicaoId != null) {
                FloatingActionButton(
                    onClick = { showCriarDialog = true },
                    containerColor = Color(0xFFFFA000),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Criar Atividade",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues) // Use paddingValues
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

        // Diálogo de criar atividade
        if (showCriarDialog && instituicaoId != null) {
            // Extrair lista de categorias do estado
            when (categoriasState) {
                is CategoriasState.Success -> {
                    val categoriasList = (categoriasState as CategoriasState.Success).categorias

                    CriarAtividadeDialog(
                        onDismiss = {
                            if (!isCreating) {
                                showCriarDialog = false
                            }
                        },
                        onConfirm = { titulo, descricao, categoriaId, faixaMin, faixaMax, gratuita, preco ->
                            val request = com.oportunyfam_mobile_ong.model.AtividadeRequest(
                                id_instituicao = instituicaoId,
                                id_categoria = categoriaId,
                                titulo = titulo,
                                descricao = descricao.ifEmpty { "" },
                                faixa_etaria_min = faixaMin,
                                faixa_etaria_max = faixaMax,
                                gratuita = gratuita,
                                preco = preco,
                                ativo = true
                            )
                            viewModel.criarAtividade(request)
                        },
                        categorias = categoriasList, // ✅ Categorias da API
                        isLoading = isCreating
                    )
                }
                is CategoriasState.Loading -> {
                    // Mostrar diálogo de loading enquanto busca categorias
                    AlertDialog(
                        onDismissRequest = { showCriarDialog = false },
                        title = { Text("Carregando...") },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = Color(0xFFFFA000)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Buscando categorias da API...")
                            }
                        },
                        confirmButton = {}
                    )
                }
                is CategoriasState.Error -> {
                    // Mostrar erro e fechar diálogo
                    LaunchedEffect(Unit) {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Erro ao carregar categorias: ${(categoriasState as CategoriasState.Error).message}"
                            )
                        }
                        showCriarDialog = false
                    }
                }
            }
        }
    }
}

