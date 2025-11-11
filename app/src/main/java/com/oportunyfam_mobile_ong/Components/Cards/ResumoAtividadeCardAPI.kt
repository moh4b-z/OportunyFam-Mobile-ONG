package com.oportunyfam_mobile_ong.Components.Cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.oportunyfam_mobile_ong.Components.InfoItemSimple
import com.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile_ong.model.AtividadeResponse

/**
 * Card de resumo da atividade com dados da API
 * @param onEditarFoto Callback chamado quando o usuário clica no ícone de câmera para editar a foto
 */
@Composable
fun ResumoAtividadeCardAPI(
    atividade: AtividadeResponse,
    onEditarFoto: (() -> Unit)? = null
) {
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
            // Cabeçalho com imagem da instituição (editável se onEditarFoto fornecido)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Box com imagem e ícone de câmera
                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    // Prioridade: foto API > atividade_foto local > instituicao_foto > ícone padrão
                    val fotoUrl = atividade.foto ?: atividade.atividade_foto ?: atividade.instituicao_foto

                    // Imagem da atividade/instituição
                    if (!fotoUrl.isNullOrEmpty() && fotoUrl != "null") {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(fotoUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .build(),
                            contentDescription = atividade.titulo,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = onEditarFoto != null) {
                                    onEditarFoto?.invoke()
                                },
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.instituicao),
                            error = painterResource(id = R.drawable.instituicao)
                        )
                    } else {
                        // Ícone padrão quando não há foto
                        Image(
                            painter = painterResource(id = R.drawable.instituicao),
                            contentDescription = atividade.titulo,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = onEditarFoto != null) {
                                    onEditarFoto?.invoke()
                                },
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Ícone de câmera (só aparece se onEditarFoto fornecido)
                    if (onEditarFoto != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFA000))
                                .clickable { onEditarFoto() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Editar foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
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