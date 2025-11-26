package com.oportunyfam_mobile_ong.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.oportunyfam_mobile_ong.model.Mensagem
import com.oportunyfam_mobile_ong.model.TipoMensagem
import com.oportunyfam_mobile_ong.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    conversaId: Int,
    nomeContato: String,
    pessoaIdAtual: Int,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val mensagens by viewModel.mensagens.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val isUploadingAudio by viewModel.isUploadingAudio.collectAsState()
    val currentPlayingAudioUrl by viewModel.currentPlayingAudioUrl.collectAsState()
    val audioProgress by viewModel.audioProgress.collectAsState()
    var currentMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Permission state for audio recording
    val recordAudioPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )

    // ✅ SEMPRE recarrega mensagens quando a tela aparece
    // Isso garante que o histórico seja carregado mesmo após sair e voltar
    LaunchedEffect(Unit) {
        viewModel.iniciarEscutaMensagens(conversaId)
    }
    // depois do LaunchedEffect(conversaId) que chama iniciarEscutaMensagens:
    DisposableEffect(conversaId) {
        onDispose {
            viewModel.pararEscutaMensagens()
        }
    }


    // Scroll automático para a última mensagem
    LaunchedEffect(mensagens.size) {
        if (mensagens.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(mensagens.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                nomeContato = nomeContato,
                onBackClick = { navController.popBackStack() }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mensagens de erro
            errorMessage?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.limparErro() }) {
                            Text("OK", color = Color(0xFFD32F2F))
                        }
                    }
                }
            }

            // Lista de mensagens
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading && mensagens.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFF6F00))
                        }
                    }
                    mensagens.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma mensagem ainda\nSeja o primeiro a enviar!",
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        // Agrupar mensagens por data e manter ordem cronológica
                        val mensagensAgrupadas = mensagens
                            .sortedBy { it.criado_em } // Ordena do mais antigo para o mais recente
                            .groupBy { extrairData(it.criado_em) }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mensagensAgrupadas.forEach { (data, mensagensDoDia) ->
                                // Separador de data
                                item(key = "date_$data") {
                                    DateSeparator(data = data)
                                }

                                // Mensagens do dia (já ordenadas)
                                items(mensagensDoDia, key = { it.id }) { mensagem ->
                                    ChatMessage(
                                        mensagem = mensagem,
                                        isUser = mensagem.id_pessoa == pessoaIdAtual,
                                        currentPlayingUrl = currentPlayingAudioUrl,
                                        audioProgress = audioProgress,
                                        onPlayAudio = { url -> viewModel.playAudio(url) },
                                        onSeekTo = { url, position -> viewModel.seekToPosition(url, position) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Campo de entrada
            ChatInputField(
                currentMessage = currentMessage,
                onMessageChange = { currentMessage = it },
                onSendClick = {
                    if (currentMessage.isNotBlank()) {
                        viewModel.enviarMensagem(conversaId, pessoaIdAtual, currentMessage)
                        currentMessage = ""
                    }
                },
                enabled = !isLoading,
                isRecordingAudio = isRecordingAudio,
                recordingDuration = recordingDuration,
                isUploadingAudio = isUploadingAudio,
                onStartRecording = {
                    if (recordAudioPermissionState.status.isGranted) {
                        viewModel.startAudioRecording()
                    } else {
                        recordAudioPermissionState.launchPermissionRequest()
                    }
                },
                onStopRecording = { viewModel.stopAudioRecordingAndSend(conversaId, pessoaIdAtual) },
                onCancelRecording = { viewModel.cancelAudioRecording() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(nomeContato: String, onBackClick: () -> Unit) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFF57C00))
    )

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Text(
                        text = nomeContato.firstOrNull()?.uppercase() ?: "?",
                        modifier = Modifier.align(Alignment.Center),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF6F00)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = nomeContato,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Online",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White
        ),
        modifier = Modifier.background(gradient)
    )
}

@Composable
fun ChatMessage(
    mensagem: Mensagem,
    isUser: Boolean,
    currentPlayingUrl: String? = null,
    audioProgress: Pair<Int, Int> = 0 to 0,
    onPlayAudio: (String) -> Unit = {},
    onSeekTo: (String, Int) -> Unit = { _, _ -> }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFFFFE0B2) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Se for mensagem de áudio, mostra player
                if (mensagem.tipo == TipoMensagem.AUDIO && mensagem.audio_url != null) {
                    AudioMessageContent(
                        audioUrl = mensagem.audio_url,
                        duration = mensagem.audio_duracao ?: 0,
                        isPlaying = currentPlayingUrl == mensagem.audio_url,
                        progress = if (currentPlayingUrl == mensagem.audio_url) audioProgress else (0 to 0),
                        onPlayAudio = onPlayAudio,
                        onSeekTo = onSeekTo,
                        isUser = isUser,
                        messageTime = formatarHora(mensagem.criado_em),
                        isViewed = mensagem.visto
                    )
                } else {
                    // Mensagem de texto normal
                    Text(
                        text = mensagem.descricao,
                        fontSize = 15.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formatarHora(mensagem.criado_em),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        if (isUser) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (mensagem.visto) "✓✓" else "✓",
                                fontSize = 11.sp,
                                color = if (mensagem.visto) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputField(
    currentMessage: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean,
    isRecordingAudio: Boolean = false,
    recordingDuration: Int = 0,
    isUploadingAudio: Boolean = false,
    onStartRecording: () -> Unit = {},
    onStopRecording: () -> Unit = {},
    onCancelRecording: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        // Se estiver gravando áudio, mostra UI de gravação
        if (isRecordingAudio) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botão cancelar
                IconButton(onClick = onCancelRecording) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = Color.Red
                    )
                }

                // Indicador de gravação
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Gravando",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDuration(recordingDuration),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }

                // Botão parar/enviar
                FloatingActionButton(
                    onClick = onStopRecording,
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFFFF6F00),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Enviar áudio",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        } else if (isUploadingAudio) {
            // Mostra loading durante upload
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color(0xFFFF6F00)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Enviando áudio...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            // UI normal de mensagem
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Botão de microfone (aparece quando não há texto)
                if (currentMessage.isBlank()) {
                    IconButton(
                        onClick = onStartRecording,
                        enabled = enabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Gravar áudio",
                            tint = Color(0xFFFF6F00),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                OutlinedTextField(
                    value = currentMessage,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Digite uma mensagem...", fontSize = 14.sp) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFF6F00),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    maxLines = 4,
                    enabled = enabled
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Botão enviar (aparece quando há texto)
                if (currentMessage.isNotBlank()) {
                    FloatingActionButton(
                        onClick = onSendClick,
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFFFF6F00),
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DateSeparator(data: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFFE0E0E0),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 1.dp
        ) {
            Text(
                text = formatarDataSeparador(data),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )
        }
    }
}

private fun extrairData(dataHora: String): String {
    return try {
        // Formato: "2025-11-08T21:45:33.000Z"
        dataHora.substring(0, 10) // Retorna "2025-11-08"
    } catch (e: Exception) {
        "Hoje"
    }
}

private fun formatarDataSeparador(data: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdf.parse(data) ?: return data

        val hoje = Date()
        val ontem = Date(hoje.time - 24 * 60 * 60 * 1000)

        val dataFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
        val hojeFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(hoje)
        val ontemFormatada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(ontem)

        when (dataFormatada) {
            hojeFormatada -> "Hoje"
            ontemFormatada -> "Ontem"
            else -> SimpleDateFormat("dd 'de' MMMM", Locale("pt", "BR")).format(date)
        }
    } catch (e: Exception) {
        data
    }
}

private fun formatarHora(dataHora: String): String {
    return try {
        // Formato esperado: "2025-11-26T02:47:40.000Z"
        val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdfInput.timeZone = java.util.TimeZone.getTimeZone("UTC")

        val date = sdfInput.parse(dataHora)
        if (date != null) {
            val sdfOutput = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdfOutput.timeZone = java.util.TimeZone.getDefault() // Fuso horário local
            sdfOutput.format(date)
        } else {
            "Agora"
        }
    } catch (e: Exception) {
        // Fallback: tenta extrair diretamente
        try {
            val partes = dataHora.split("T")
            if (partes.size > 1) {
                partes[1].substring(0, 5)
            } else {
                "Agora"
            }
        } catch (ex: Exception) {
            "Agora"
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, secs)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioMessageContent(
    audioUrl: String,
    duration: Int,
    isPlaying: Boolean,
    progress: Pair<Int, Int>, // (current, total) em milissegundos
    onPlayAudio: (String) -> Unit,
    onSeekTo: (String, Int) -> Unit = { _, _ -> },
    isUser: Boolean,
    messageTime: String = "",
    isViewed: Boolean = false
) {
    val (currentMs, totalMs) = progress

    // Tempo atual em segundos
    val currentSeconds = if (isPlaying && totalMs > 0) {
        (currentMs / 1000)
    } else {
        0
    }

    // Tempo total em segundos
    val totalSeconds = if (totalMs > 0) (totalMs / 1000) else duration

    // Tempo a mostrar: se está tocando mostra progresso, se pausado mostra duração total
    val displayTime = if (isPlaying) {
        formatDuration(currentSeconds)
    } else {
        formatDuration(totalSeconds)
    }

    // Estado local para o slider
    var sliderPosition by remember { mutableStateOf(currentMs.toFloat()) }
    var isUserDragging by remember { mutableStateOf(false) }

    // Atualiza posição do slider quando não está arrastando
    LaunchedEffect(currentMs) {
        if (!isUserDragging) {
            sliderPosition = currentMs.toFloat()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // Botão play/pause
        IconButton(
            onClick = { onPlayAudio(audioUrl) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                tint = if (isUser) Color(0xFFFF6F00) else Color(0xFF616161),
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Barra de progresso e tempo
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Slider com bolinha para controlar posição
            Slider(
                value = sliderPosition,
                onValueChange = { newValue ->
                    isUserDragging = true
                    sliderPosition = newValue
                },
                onValueChangeFinished = {
                    isUserDragging = false
                    // Ao soltar, busca para a nova posição
                    onSeekTo(audioUrl, sliderPosition.toInt())
                },
                valueRange = 0f..(if (totalMs > 0) totalMs.toFloat() else (duration * 1000).toFloat()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = if (isUser) Color(0xFFFF6F00) else Color(0xFF616161),
                    activeTrackColor = if (isUser) Color(0xFFFF6F00) else Color(0xFF616161),
                    inactiveTrackColor = Color.Gray.copy(alpha = 0.3f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                thumb = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .offset(y = (5).dp)
                            .background(
                                color = if (isUser) Color(0xFFFF6F00) else Color(0xFF616161),
                                shape = CircleShape
                            )
                    )
                },
                track = { sliderState ->
                    // Track personalizado com linhas centralizadas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Linha de progresso (ativa)
                            Box(
                                modifier = Modifier
                                    .weight(
                                        weight = if (sliderState.valueRange.endInclusive > 0) {
                                            ((sliderState.value - sliderState.valueRange.start) /
                                            (sliderState.valueRange.endInclusive - sliderState.valueRange.start)).coerceIn(0.001f, 1f)
                                        } else {
                                            0.001f
                                        }
                                    )
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(
                                        if (isUser) Color(0xFFFF6F00) else Color(0xFF616161)
                                    )
                            )
                            // Linha de fundo (inativa)
                            Box(
                                modifier = Modifier
                                    .weight(
                                        weight = if (sliderState.valueRange.endInclusive > 0) {
                                            1f - ((sliderState.value - sliderState.valueRange.start) /
                                            (sliderState.valueRange.endInclusive - sliderState.valueRange.start)).coerceIn(0f, 0.999f)
                                        } else {
                                            1f
                                        }
                                    )
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(Color.Gray.copy(alpha = 0.3f))
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Contador, ícone, horário e status na mesma linha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lado esquerdo: Contador e ícone
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contador de tempo
                    Text(
                        text = if (isUserDragging) {
                            // Mostra tempo do slider enquanto arrasta
                            formatDuration((sliderPosition / 1000).toInt())
                        } else {
                            displayTime
                        },
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Ícone de áudio
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Áudio",
                        tint = if (isUser) Color(0xFFFF6F00) else Color(0xFF616161),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Lado direito: Horário e status de visualização
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = messageTime,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    if (isUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isViewed) "✓✓" else "✓",
                            fontSize = 11.sp,
                            color = if (isViewed) Color(0xFF4CAF50) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen(
        navController = rememberNavController(),
        conversaId = 1,
        nomeContato = "Laura de Andrade",
        pessoaIdAtual = 1
    )
}