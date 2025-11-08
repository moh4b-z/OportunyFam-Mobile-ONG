package com.oportunyfam_mobile_ong.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile_ong.model.AtividadeResponse

/**
 * ✅ REUTILIZÁVEL PARA APP DE USUÁRIOS/RESPONSÁVEIS
 *
 * Card de atividade com dados da API
 * Exibe informações completas da atividade de forma visual
 *
 * Pode ser usado na aplicação de usuários para:
 * - Listar atividades disponíveis
 * - Mostrar atividades inscritas
 * - Explorar atividades por categoria
 */
@Composable
fun AtividadeCardAPI(atividade: AtividadeResponse, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagem da atividade
            if (!atividade.instituicao_foto.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(atividade.instituicao_foto)
                        .crossfade(true)
                        .build(),
                    contentDescription = atividade.titulo,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.instituicao),
                    error = painterResource(id = R.drawable.instituicao)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.instituicao),
                    contentDescription = atividade.titulo,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    atividade.titulo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    atividade.categoria,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (atividade.gratuita == 1) "Gratuita" else "R$ ${atividade.preco}",
                        fontSize = 12.sp,
                        color = Color(0xFFFFA000),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "• ${atividade.aulas.size} aulas",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Indicador visual
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .background(
                        if (atividade.ativo == 1) Color(0xFFFFA000) else Color.Gray,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun InfoItem(
    titulo: String,
    valor: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = titulo, tint = Color(0xFFFFA000))
        Spacer(modifier = Modifier.height(4.dp))
        Text(valor, fontWeight = FontWeight.Bold)
        Text(titulo, fontSize = 12.sp, color = Color.Gray)
    }
}

/**
 * Item de informação simples (sem ícone) para cards de resumo
 */
@Composable
fun InfoItemSimple(
    label: String,
    value: String
) {
    Column {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            fontSize = 14.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Card de resumo da atividade
 */
@Composable
private fun ResumoAtividadeCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Resumo da Atividade", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem("Alunos", "24", Icons.Default.Group)
                InfoItem("Próxima Aula", "15/12", Icons.Default.CalendarToday)
                InfoItem("Status", "Ativa", Icons.Default.Settings)
            }
        }
    }
}


/**
 * Card de resumo da atividade com dados da API
 */
@Composable
fun ResumoAtividadeCardAPI(atividade: AtividadeResponse) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Cabeçalho com imagem
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!atividade.instituicao_foto.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(atividade.instituicao_foto)
                            .crossfade(true)
                            .build(),
                        contentDescription = atividade.titulo,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.instituicao),
                        error = painterResource(id = R.drawable.instituicao)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.instituicao),
                        contentDescription = atividade.titulo,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        atividade.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Text(
                        atividade.categoria,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Descrição
            if (!atividade.descricao.isNullOrEmpty()) {
                Text(
                    "Descrição",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    atividade.descricao,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Informações em grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItemSimple(label = "Faixa Etária", value = "${atividade.faixa_etaria_min}-${atividade.faixa_etaria_max} anos")
                InfoItemSimple(
                    label = "Valor",
                    value = if (atividade.gratuita == 1) "Gratuita" else "R$ ${atividade.preco}"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItemSimple(label = "Aulas", value = "${atividade.aulas.size}")
                InfoItemSimple(
                    label = "Status",
                    value = if (atividade.ativo == 1) "Ativa" else "Inativa"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItemSimple(label = "Local", value = "${atividade.cidade}, ${atividade.estado}")
            }
        }
    }
}



/**
 * Card visual de atividade (antigo - manter para compatibilidade)
 */
@Composable
fun AtividadeCardVisual(titulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.instituicao),
                contentDescription = titulo,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Clique para gerenciar", fontSize = 14.sp, color = Color.Gray)
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(Color(0xFFFFA000))
            )
        }
    }
}



/**
 * Opção de gerenciamento clicável
 */
@Composable
fun OpcaoGerenciamento(titulo: String, descricao: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(descricao, fontSize = 14.sp, color = Color.Gray)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.rotate(180f)
            )
        }
    }
}

/**
 * Card de aluno
 */
@Composable
fun CardAluno(aluno: Aluno) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.instituicao),
                contentDescription = aluno.nome,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(aluno.nome, fontWeight = FontWeight.Bold)
                Text(aluno.email, fontSize = 14.sp, color = Color.Gray)
                Text("Desde ${aluno.dataCadastro}", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                aluno.status,
                color = if (aluno.status == "Ativo") Color.Green else Color.Red
            )
        }
    }
}

/**
 * Card de aula com dados da API
 */
@Composable
fun CardAulaAPI(aula: com.oportunyfam_mobile_ong.model.AulaDetalhe) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (aula.status == "Futura") Color(0xFFFFF8E1) else Color(0xFFF5F5F5)
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
                        if (aula.status == "Futura") Color(0xFFFFA000) else Color.Gray,
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
                Text(
                    aula.data_aula,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
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

            // Badge de status
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (aula.status == "Futura") Color(0xFF4CAF50) else Color.Gray
            ) {
                Text(
                    aula.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Card de aula (antigo - manter para compatibilidade)
 */
@Composable
fun CardAula(aula: Aula) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(aula.data, fontWeight = FontWeight.Bold)
                Text(
                    aula.horario,
                    color = Color(0xFFFFA000),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(aula.descricao, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${aula.alunosConfirmados} alunos confirmados",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Card de configuração
 */
@Composable
fun CardConfiguracao(config: Configuracao) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(config.titulo, fontWeight = FontWeight.Bold)
                Text(config.descricao, fontSize = 14.sp, color = Color.Gray)
            }
            when (config) {
                is Configuracao.Toggle -> {
                    Switch(
                        checked = config.valor,
                        onCheckedChange = { /* TODO */ },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFA000)
                        )
                    )
                }
                is Configuracao.Info -> {
                    Text(config.valor, color = Color.Gray)
                }
            }
        }
    }
}

/**
 * Placeholder do calendário
 */
@Composable
fun CalendarioPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text("Calendário Visual", fontSize = 18.sp, color = Color.Gray)
    }
}


// ==================== MODELOS DE DADOS ====================

/**
 * Modelo de dados para Aluno
 */
data class Aluno(
    val nome: String,
    val email: String,
    val dataCadastro: String,
    val status: String
)

/**
 * Modelo de dados para Aula
 */
data class Aula(
    val data: String,
    val horario: String,
    val descricao: String,
    val alunosConfirmados: Int
)

/**
 * Modelo de dados para Configuração
 */
sealed class Configuracao {
    abstract val titulo: String
    abstract val descricao: String

    data class Toggle(
        override val titulo: String,
        override val descricao: String,
        val valor: Boolean
    ) : Configuracao()

    data class Info(
        override val titulo: String,
        override val descricao: String,
        val valor: String
    ) : Configuracao()
}