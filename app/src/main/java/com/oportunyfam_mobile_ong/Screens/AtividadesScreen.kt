package com.oportunyfam_mobile_ong.Screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.oportunyfam_mobile_ong.Components.BarraTarefas
import com.oportunyfam_mobile_ong.data.InstituicaoAuthDataStore
import com.oportunyfam_mobile_ong.viewmodel.AtividadeViewModel
import com.oportunyfam_mobile_ong.viewmodel.InscricaoViewModel

// ==================== ENUMS E ESTADOS ====================

/**
 * Estados possíveis da tela de atividades
 */
enum class TelaAtividade {
    LISTA,
    DETALHES,
    ALUNOS,
    CALENDARIO,
    CONFIGURACOES
}

// ==================== SCREEN PRINCIPAL ====================

/**
 * AtividadesScreen - Tela de gerenciamento de atividades
 *
 * Carrega atividades da API e permite:
 * - Listar todas as atividades da instituição
 * - Ver detalhes de cada atividade
 * - Gerenciar alunos por atividade
 * - Visualizar calendário de aulas
 * - Configurar atividades
 *
 * @param navController Controlador de navegação
 */
@Composable
fun AtividadesScreen(navController: NavHostController?) {
    val context = LocalContext.current
    val instituicaoAuthDataStore = remember { InstituicaoAuthDataStore(context) }
    var instituicaoId by remember { mutableStateOf<Int?>(null) }

    // Criar ViewModel com contexto para suportar fotos individuais por atividade
    val viewModel: AtividadeViewModel = viewModel(
        factory = com.oportunyfam_mobile_ong.viewmodel.AtividadeViewModelFactory(context)
    )
    val inscricaoViewModel: InscricaoViewModel = viewModel()
    var telaAtual by remember { mutableStateOf(TelaAtividade.LISTA) }
    var atividadeSelecionadaId by remember { mutableStateOf<Int?>(null) }

    // Carregar instituição logada
    LaunchedEffect(Unit) {
        val instituicao = instituicaoAuthDataStore.loadInstituicao()
        instituicaoId = instituicao?.instituicao_id
        Log.d("AtividadesScreen", "Instituição carregada: ID=$instituicaoId")
    }

    // Carregar atividades da instituição quando o ID estiver disponível
    LaunchedEffect(instituicaoId) {
        instituicaoId?.let { id ->
            Log.d("AtividadesScreen", "Carregando atividades da instituição: $id")
            viewModel.buscarAtividadesPorInstituicao(id)
        }
    }

    Scaffold(
        bottomBar = {
            BarraTarefas(
                navController = navController,
                currentRoute = "AtividadesScreen"
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (telaAtual) {
                TelaAtividade.LISTA -> {
                    ListaAtividadesScreen(
                        navController = navController,
                        viewModel = viewModel,
                        instituicaoId = instituicaoId,
                        onAtividadeClick = { atividadeId ->
                            atividadeSelecionadaId = atividadeId
                            viewModel.buscarAtividadePorId(atividadeId)
                            telaAtual = TelaAtividade.DETALHES
                        }
                    )
                }
                TelaAtividade.DETALHES -> {
                    atividadeSelecionadaId?.let { id ->
                        DetalhesAtividadeScreen(
                            viewModel = viewModel,
                            atividadeId = id,
                            onBack = {
                                viewModel.limparDetalhe()
                                // Recarregar lista de atividades ao voltar
                                instituicaoId?.let { instId ->
                                    viewModel.buscarAtividadesPorInstituicao(instId)
                                }
                                telaAtual = TelaAtividade.LISTA
                            },
                            onVerAlunos = { telaAtual = TelaAtividade.ALUNOS },
                            onVerCalendario = { telaAtual = TelaAtividade.CALENDARIO },
                            onConfiguracoes = { telaAtual = TelaAtividade.CONFIGURACOES }
                        )
                    }
                }
                TelaAtividade.ALUNOS -> {
                    atividadeSelecionadaId?.let { id ->
                        GerenciarAlunosScreen(
                            atividadeId = id,
                            onBack = { telaAtual = TelaAtividade.DETALHES },
                            viewModel = inscricaoViewModel
                        )
                    }
                }
                TelaAtividade.CALENDARIO -> {
                    atividadeSelecionadaId?.let { id ->
                        CalendarioAulasScreen(
                            viewModel = viewModel,
                            atividadeId = id,
                            onBack = { telaAtual = TelaAtividade.DETALHES }
                        )
                    }
                }
                TelaAtividade.CONFIGURACOES -> {
                    atividadeSelecionadaId?.let { id ->
                        ConfiguracoesAtividadeScreen(
                            atividadeId = id,
                            onBack = { telaAtual = TelaAtividade.DETALHES }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAtividadesScreen() {
    AtividadesScreen(navController = null)
}