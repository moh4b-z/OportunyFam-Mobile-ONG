# 🎤 Implementação de Áudio no Chat - Checklist Completo

**Data de Criação:** 25/01/2025  
**Projeto:** OportunyFam Mobile ONG  
**Objetivo:** Adicionar funcionalidade de mensagens de áudio no chat

---

## 📋 Índice
1. [Visão Geral](#visão-geral)
2. [Pré-requisitos](#pré-requisitos)
3. [Backend - Mudanças na API](#backend---mudanças-na-api)
4. [Frontend - Mudanças no App](#frontend---mudanças-no-app)
5. [Testes](#testes)
6. [Checklist de Implementação](#checklist-de-implementação)

---

## 🎯 Visão Geral

### O que será implementado:
- ✅ Gravação de mensagens de áudio
- ✅ Upload de áudio para Firebase Storage
- ✅ Reprodução de mensagens de áudio
- ✅ Visualização de duração e progresso
- ✅ Interface intuitiva para gravar/enviar/reproduzir

### Fluxo da Funcionalidade:
```
1. Usuário pressiona botão de microfone
2. App solicita permissão de RECORD_AUDIO (se necessário)
3. Inicia gravação do áudio
4. Usuário pode cancelar ou enviar
5. Ao enviar:
   a. Upload do áudio para Firebase Storage
   b. Criação da mensagem na API com URL do áudio
   c. Sincronização com Firebase Realtime Database
6. Destinatário recebe mensagem e pode reproduzir
```

---

## 🔧 Pré-requisitos

### ✅ Já Configurado no Projeto:
- Firebase Storage (`firebase-storage-ktx`)
- Firebase Realtime Database (`firebase-database-ktx`)
- Coroutines (`kotlinx-coroutines`)
- Accompanist Permissions (`accompanist-permissions`)
- MediaRecorder/MediaPlayer (nativo Android)

### ⚠️ Precisa Verificar:
- [ ] Firebase Storage Rules configuradas
- [ ] API backend atualizada para aceitar novos campos

---

## 🌐 Backend - Mudanças na API

### 1. **Atualizar Modelo de Dados (Backend)**

**Arquivo:** `models/Mensagem.js` ou equivalente

**Campos a adicionar:**
```javascript
{
  id: Int,
  descricao: String,
  visto: Boolean,
  criado_em: DateTime,
  atualizado_em: DateTime,
  id_conversa: Int,
  id_pessoa: Int,
  // 🆕 NOVOS CAMPOS:
  tipo: String, // "TEXTO", "AUDIO", "IMAGEM"
  audio_url: String, // URL do áudio no Firebase Storage
  audio_duracao: Int // Duração em segundos
}
```

### 2. **Atualizar Endpoints da API**

#### Endpoint: `POST /mensagens`
**Request Body Atualizado:**
```json
{
  "id_conversa": 1,
  "id_pessoa": 5,
  "descricao": "Áudio",
  "tipo": "AUDIO",
  "audio_url": "https://firebasestorage.googleapis.com/...",
  "audio_duracao": 15
}
```

#### Endpoint: `GET /mensagens/:conversaId`
**Response Atualizado:**
```json
{
  "status": true,
  "status_code": 200,
  "messagem": "Mensagens recuperadas com sucesso",
  "mensagens": [
    {
      "id": 1,
      "descricao": "Olá!",
      "tipo": "TEXTO",
      "audio_url": null,
      "audio_duracao": null,
      ...
    },
    {
      "id": 2,
      "descricao": "Áudio",
      "tipo": "AUDIO",
      "audio_url": "https://firebasestorage.googleapis.com/.../audio_123.m4a",
      "audio_duracao": 15,
      ...
    }
  ]
}
```

### 3. **Validações no Backend**

```javascript
// Validar tipo de mensagem
if (tipo === "AUDIO") {
  // Verificar se audio_url está presente
  if (!audio_url || audio_url.trim() === "") {
    return res.status(400).json({
      status: false,
      messagem: "URL do áudio é obrigatória para mensagens de áudio"
    });
  }
  
  // Verificar se audio_duracao é válida
  if (!audio_duracao || audio_duracao <= 0) {
    return res.status(400).json({
      status: false,
      messagem: "Duração do áudio inválida"
    });
  }
  
  // Limitar duração máxima (ex: 5 minutos = 300 segundos)
  if (audio_duracao > 300) {
    return res.status(400).json({
      status: false,
      messagem: "Áudio muito longo. Máximo permitido: 5 minutos"
    });
  }
}
```

---

## 📱 Frontend - Mudanças no App

### 📁 Estrutura de Arquivos a Criar/Modificar

```
app/src/main/java/com/oportunyfam_mobile_ong/
├── model/
│   └── Mensagem.kt ............................ ✏️ MODIFICAR
├── Service/
│   ├── FirebaseMensagemService.kt ............. ✏️ MODIFICAR
│   ├── FirebaseAudioService.kt ................ 🆕 CRIAR
│   └── AudioRecorder.kt ....................... 🆕 CRIAR
├── Components/
│   ├── AudioMessageBubble.kt .................. 🆕 CRIAR
│   ├── AudioRecordButton.kt ................... 🆕 CRIAR
│   └── AudioPlayer.kt ......................... 🆕 CRIAR
├── viewmodel/
│   └── ChatViewModel.kt ....................... ✏️ MODIFICAR
├── Screens/
│   └── ChatScreen.kt .......................... ✏️ MODIFICAR
└── AndroidManifest.xml ........................ ✏️ MODIFICAR
```

---

## 🔨 Implementação Detalhada

### 1️⃣ **Atualizar Modelo de Dados**

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/model/Mensagem.kt`

#### 🆕 Adicionar enum TipoMensagem:
```kotlin
enum class TipoMensagem {
    TEXTO,
    AUDIO,
    IMAGEM // Para futuras implementações
}
```

#### ✏️ Modificar data class Mensagem:
```kotlin
data class Mensagem(
    val id: Int,
    val descricao: String,
    val visto: Boolean,
    val criado_em: String,
    val atualizado_em: String?,
    val id_conversa: Int,
    val id_pessoa: Int,
    // 🆕 ADICIONAR:
    val tipo: TipoMensagem = TipoMensagem.TEXTO,
    val audio_url: String? = null,
    val audio_duracao: Int? = null
)
```

#### ✏️ Modificar data class MensagemRequest:
```kotlin
data class MensagemRequest(
    val id_conversa: Int,
    val id_pessoa: Int,
    val descricao: String,
    // 🆕 ADICIONAR:
    val tipo: String = "TEXTO",
    val audio_url: String? = null,
    val audio_duracao: Int? = null
)
```

#### ✏️ Modificar MensagemResponse (se necessário):
```kotlin
// Geralmente não precisa modificar, pois já retorna Mensagem
// Mas verificar se a API retorna os novos campos
```

---

### 2️⃣ **Atualizar Firebase Service**

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Service/FirebaseMensagemService.kt`

#### ✏️ Modificar data class MensagemFirebase:
```kotlin
data class MensagemFirebase(
    val id: Int = 0,
    val descricao: String = "",
    val visto: Boolean = false,
    val criado_em: String = "",
    val atualizado_em: String? = null,
    val id_conversa: Int = 0,
    val id_pessoa: Int = 0,
    // 🆕 ADICIONAR:
    val tipo: String = "TEXTO",
    val audio_url: String? = null,
    val audio_duracao: Int? = null
)
```

#### ✏️ Atualizar função de conversão:
```kotlin
// No método toMensagem():
private fun MensagemFirebase.toMensagem() = Mensagem(
    id = id,
    descricao = descricao,
    visto = visto,
    criado_em = criado_em,
    atualizado_em = atualizado_em,
    id_conversa = id_conversa,
    id_pessoa = id_pessoa,
    // 🆕 ADICIONAR:
    tipo = try { TipoMensagem.valueOf(tipo) } catch (e: Exception) { TipoMensagem.TEXTO },
    audio_url = audio_url,
    audio_duracao = audio_duracao
)
```

#### ✏️ Atualizar função enviarMensagem:
```kotlin
suspend fun enviarMensagem(mensagem: Mensagem): Result<Unit> {
    return try {
        val mensagemRef = database.child("conversas")
            .child(mensagem.id_conversa.toString())
            .child("mensagens")
            .child(mensagem.id.toString())

        val mensagemFirebase = MensagemFirebase(
            id = mensagem.id,
            descricao = mensagem.descricao,
            visto = mensagem.visto,
            criado_em = mensagem.criado_em,
            atualizado_em = mensagem.atualizado_em,
            id_conversa = mensagem.id_conversa,
            id_pessoa = mensagem.id_pessoa,
            // 🆕 ADICIONAR:
            tipo = mensagem.tipo.name,
            audio_url = mensagem.audio_url,
            audio_duracao = mensagem.audio_duracao
        )

        mensagemRef.setValue(mensagemFirebase).await()
        Log.d(TAG, "✅ Mensagem ${mensagem.id} enviada para Firebase")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro ao enviar mensagem para Firebase", e)
        Result.failure(e)
    }
}
```

---

### 3️⃣ **Criar FirebaseAudioService**

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Service/FirebaseAudioService.kt`

```kotlin
package com.oportunyfam_mobile_ong.Service

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class FirebaseAudioService {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val TAG = "FirebaseAudioService"

    /**
     * Faz upload de um arquivo de áudio para o Firebase Storage
     * @param audioFile Arquivo de áudio local
     * @param conversaId ID da conversa
     * @return URL do áudio ou null se falhar
     */
    suspend fun uploadAudio(audioFile: File, conversaId: Int): String? {
        return try {
            if (!audioFile.exists()) {
                Log.e(TAG, "❌ Arquivo de áudio não existe: ${audioFile.absolutePath}")
                return null
            }

            Log.d(TAG, "📤 Iniciando upload de áudio: ${audioFile.name}")
            
            val fileName = "audio_${System.currentTimeMillis()}.m4a"
            val audioRef = storageRef.child("audios/conversa_$conversaId/$fileName")
            
            // Upload do arquivo
            val uploadTask = audioRef.putFile(Uri.fromFile(audioFile)).await()
            
            // Obter URL de download
            val downloadUrl = audioRef.downloadUrl.await()
            
            Log.d(TAG, "✅ Upload concluído: $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao fazer upload de áudio", e)
            null
        }
    }

    /**
     * Baixa um arquivo de áudio do Firebase Storage (para cache local)
     * @param audioUrl URL do áudio
     * @return Arquivo local temporário
     */
    suspend fun downloadAudio(audioUrl: String): File? {
        return try {
            val audioRef = storage.getReferenceFromUrl(audioUrl)
            val localFile = File.createTempFile("audio_", ".m4a")
            
            audioRef.getFile(localFile).await()
            
            Log.d(TAG, "✅ Áudio baixado: ${localFile.absolutePath}")
            localFile
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao baixar áudio", e)
            null
        }
    }

    /**
     * Deleta um áudio do Firebase Storage
     * @param audioUrl URL do áudio
     */
    suspend fun deleteAudio(audioUrl: String): Result<Unit> {
        return try {
            val audioRef = storage.getReferenceFromUrl(audioUrl)
            audioRef.delete().await()
            
            Log.d(TAG, "✅ Áudio deletado: $audioUrl")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao deletar áudio", e)
            Result.failure(e)
        }
    }
}
```

---

### 4️⃣ **Criar AudioRecorder**

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Service/AudioRecorder.kt`

```kotlin
package com.oportunyfam_mobile_ong.Service

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class AudioRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0
    private val TAG = "AudioRecorder"

    /**
     * Inicia a gravação de áudio
     * @return Arquivo onde o áudio está sendo gravado, ou null se falhar
     */
    fun startRecording(): File? {
        try {
            // Criar arquivo temporário
            audioFile = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
            
            Log.d(TAG, "🎤 Iniciando gravação: ${audioFile?.absolutePath}")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            Log.d(TAG, "✅ Gravação iniciada")
            return audioFile
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao iniciar gravação", e)
            return null
        }
    }

    /**
     * Para a gravação e retorna o arquivo e duração
     * @return Par (Arquivo, Duração em segundos)
     */
    fun stopRecording(): Pair<File?, Int> {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            
            Log.d(TAG, "⏹️ Gravação finalizada. Duração: ${duration}s")
            
            return Pair(audioFile, duration)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao parar gravação", e)
            return Pair(null, 0)
        }
    }

    /**
     * Cancela a gravação e deleta o arquivo
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            audioFile?.delete()
            audioFile = null
            
            Log.d(TAG, "🗑️ Gravação cancelada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao cancelar gravação", e)
        }
    }

    /**
     * Verifica se está gravando
     */
    fun isRecording(): Boolean {
        return mediaRecorder != null
    }
}
```

---

### 5️⃣ **Criar AudioPlayer**

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Components/AudioPlayer.kt`

```kotlin
package com.oportunyfam_mobile_ong.Components

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var currentAudioUrl: String? = null
    private val TAG = "AudioPlayer"
    private var updateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _playingAudioUrl = MutableStateFlow<String?>(null)
    val playingAudioUrl: StateFlow<String?> = _playingAudioUrl.asStateFlow()

    /**
     * Reproduz ou pausa um áudio
     * @param audioUrl URL do áudio
     */
    suspend fun play(audioUrl: String) {
        try {
            if (currentAudioUrl == audioUrl && mediaPlayer != null) {
                // Mesmo áudio - alternar play/pause
                if (_isPlaying.value) {
                    pause()
                } else {
                    resume()
                }
                return
            }

            // Novo áudio - parar o anterior e iniciar novo
            stop()
            currentAudioUrl = audioUrl
            _playingAudioUrl.value = audioUrl

            Log.d(TAG, "▶️ Reproduzindo áudio: $audioUrl")

            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnCompletionListener {
                    Log.d(TAG, "✅ Reprodução concluída")
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    updateJob?.cancel()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "❌ Erro na reprodução: what=$what, extra=$extra")
                    stop()
                    true
                }
                prepare()
                start()
            }

            _duration.value = mediaPlayer?.duration ?: 0
            _isPlaying.value = true

            // Iniciar atualização de posição
            startPositionUpdater()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao reproduzir áudio", e)
            stop()
        }
    }

    /**
     * Pausa a reprodução
     */
    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
            updateJob?.cancel()
            Log.d(TAG, "⏸️ Reprodução pausada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao pausar", e)
        }
    }

    /**
     * Resume a reprodução
     */
    fun resume() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
            startPositionUpdater()
            Log.d(TAG, "▶️ Reprodução retomada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao retomar", e)
        }
    }

    /**
     * Para completamente a reprodução
     */
    fun stop() {
        try {
            updateJob?.cancel()
            mediaPlayer?.apply {
                stop()
                release()
            }
            mediaPlayer = null
            _isPlaying.value = false
            _currentPosition.value = 0
            _playingAudioUrl.value = null
            currentAudioUrl = null
            Log.d(TAG, "⏹️ Reprodução parada")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao parar", e)
        }
    }

    /**
     * Atualiza a posição do áudio continuamente
     */
    private fun startPositionUpdater() {
        updateJob?.cancel()
        updateJob = scope.launch {
            while (_isPlaying.value) {
                _currentPosition.value = mediaPlayer?.currentPosition ?: 0
                delay(100)
            }
        }
    }

    /**
     * Libera recursos
     */
    fun release() {
        stop()
        scope.cancel()
    }
}
```

---

### 6️⃣ **Criar Componentes de UI**

#### **Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Components/AudioRecordButton.kt`

```kotlin
package com.oportunyfam_mobile_ong.Components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AudioRecordButton(
    isRecording: Boolean,
    recordingDuration: Int,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    enabled: Boolean = true
) {
    if (isRecording) {
        // Interface de gravação ativa
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFEBEE),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Botão cancelar
                IconButton(
                    onClick = onCancelRecording,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Cancelar gravação",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Indicador de gravação animado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Círculo pulsante
                    val infiniteTransition = rememberInfiniteTransition(label = "recording")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(800),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Red.copy(alpha = alpha), CircleShape)
                    )

                    Spacer(Modifier.width(12.dp))

                    // Duração
                    Text(
                        formatarDuracaoAudio(recordingDuration),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }

                // Botão enviar
                FloatingActionButton(
                    onClick = onStopRecording,
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar áudio",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    } else {
        // Botão de microfone normal
        IconButton(
            onClick = onStartRecording,
            enabled = enabled,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Default.Mic,
                contentDescription = "Gravar áudio",
                tint = if (enabled) Color(0xFFFF6F00) else Color.Gray,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Formata a duração do áudio em formato mm:ss
 */
fun formatarDuracaoAudio(segundos: Int): String {
    val minutos = segundos / 60
    val segs = segundos % 60
    return String.format("%02d:%02d", minutos, segs)
}
```

#### **Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Components/AudioMessageBubble.kt`

```kotlin
package com.oportunyfam_mobile_ong.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile_ong.model.Mensagem

@Composable
fun AudioMessageBubble(
    mensagem: Mensagem,
    isUser: Boolean,
    isPlaying: Boolean,
    currentPosition: Int,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier.widthIn(min = 220.dp, max = 280.dp),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) Color(0xFFDCF8C6) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botão play/pause
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(44.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFFF6F00).copy(alpha = 0.1f)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                        tint = Color(0xFFFF6F00),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Barra de progresso
                val duracao = mensagem.audio_duracao ?: 1
                val progress = if (isPlaying && duracao > 0) {
                    (currentPosition.toFloat() / (duracao * 1000)).coerceIn(0f, 1f)
                } else {
                    0f
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Color(0xFFFF6F00),
                    trackColor = Color(0xFFFF6F00).copy(alpha = 0.2f),
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Duração/Posição atual
                    Text(
                        text = formatarDuracaoAudio(
                            if (isPlaying) currentPosition / 1000 else duracao
                        ),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    // Hora de envio
                    Text(
                        text = formatarHora(mensagem.criado_em),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

private fun formatarHora(dataHora: String): String {
    return try {
        val partes = dataHora.split("T")
        if (partes.size > 1) {
            partes[1].substring(0, 5)
        } else {
            "Agora"
        }
    } catch (e: Exception) {
        "Agora"
    }
}
```

---

### 7️⃣ **Atualizar ChatViewModel**

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/viewmodel/ChatViewModel.kt`

#### ✏️ Adicionar imports:
```kotlin
import com.oportunyfam_mobile_ong.Service.AudioRecorder
import com.oportunyfam_mobile_ong.Service.FirebaseAudioService
import com.oportunyfam_mobile_ong.Components.AudioPlayer
import com.oportunyfam_mobile_ong.model.TipoMensagem
```

#### ✏️ Adicionar propriedades na classe:
```kotlin
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    // ... código existente ...

    // 🆕 ADICIONAR:
    
    // Serviços de áudio
    private val audioRecorder = AudioRecorder(application)
    private val audioPlayer = AudioPlayer()
    private val firebaseAudioService = FirebaseAudioService()

    // Estados de gravação
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()

    // Estados de reprodução
    private val _playingAudioId = MutableStateFlow<Int?>(null)
    val playingAudioId: StateFlow<Int?> = _playingAudioId.asStateFlow()

    val audioPlayerIsPlaying = audioPlayer.isPlaying
    val audioPlayerCurrentPosition = audioPlayer.currentPosition
    val audioPlayerPlayingUrl = audioPlayer.playingAudioUrl

    // ... resto do código existente ...
}
```

#### ✏️ Adicionar funções:
```kotlin
// 🆕 ADICIONAR estas funções na classe ChatViewModel:

/**
 * Inicia a gravação de áudio
 */
fun iniciarGravacao() {
    viewModelScope.launch {
        val file = audioRecorder.startRecording()
        if (file != null) {
            _isRecording.value = true
            startDurationUpdater()
            Log.d(TAG, "🎤 Gravação iniciada")
        } else {
            _errorMessage.value = "Erro ao iniciar gravação de áudio"
        }
    }
}

/**
 * Para a gravação e envia o áudio
 */
fun pararGravacao(conversaId: Int, pessoaId: Int) {
    viewModelScope.launch {
        _isRecording.value = false
        val (audioFile, duration) = audioRecorder.stopRecording()

        if (audioFile != null && audioFile.exists()) {
            if (duration < 1) {
                _errorMessage.value = "Áudio muito curto"
                audioFile.delete()
                return@launch
            }

            if (duration > 300) { // 5 minutos
                _errorMessage.value = "Áudio muito longo. Máximo: 5 minutos"
                audioFile.delete()
                return@launch
            }

            // Upload para Firebase Storage
            _isLoading.value = true
            Log.d(TAG, "📤 Fazendo upload do áudio...")
            
            val audioUrl = firebaseAudioService.uploadAudio(audioFile, conversaId)

            if (audioUrl != null) {
                Log.d(TAG, "✅ Upload concluído: $audioUrl")
                // Criar mensagem de áudio na API
                enviarMensagemAudio(conversaId, pessoaId, audioUrl, duration)
            } else {
                _errorMessage.value = "Erro ao fazer upload do áudio"
            }

            // Limpar arquivo temporário
            audioFile.delete()
            _isLoading.value = false
        } else {
            _errorMessage.value = "Erro ao processar áudio"
        }
    }
}

/**
 * Cancela a gravação
 */
fun cancelarGravacao() {
    audioRecorder.cancelRecording()
    _isRecording.value = false
    _recordingDuration.value = 0
    Log.d(TAG, "🗑️ Gravação cancelada")
}

/**
 * Envia mensagem de áudio
 */
private suspend fun enviarMensagemAudio(
    conversaId: Int,
    pessoaId: Int,
    audioUrl: String,
    duracao: Int
) {
    try {
        Log.d(TAG, "📝 Criando mensagem de áudio na API...")
        
        // Criar mensagem na API backend
        val request = MensagemRequest(
            id_conversa = conversaId,
            id_pessoa = pessoaId,
            descricao = "Áudio", // Placeholder
            tipo = "AUDIO",
            audio_url = audioUrl,
            audio_duracao = duracao
        )

        val response = mensagemService.criar(request)

        if (response.isSuccessful && response.body()?.mensagem != null) {
            val novaMensagem = response.body()!!.mensagem!!

            Log.d(TAG, "✅ Mensagem criada na API: ID=${novaMensagem.id}")

            // Sincronizar com Firebase Realtime Database
            firebaseMensagemService.enviarMensagem(novaMensagem)
            
            Log.d(TAG, "✅ Áudio enviado com sucesso!")
        } else {
            Log.e(TAG, "❌ Erro na API: ${response.code()} - ${response.message()}")
            _errorMessage.value = "Erro ao enviar áudio: ${response.code()}"
        }
    } catch (e: Exception) {
        Log.e(TAG, "❌ Erro ao enviar mensagem de áudio", e)
        _errorMessage.value = "Erro ao enviar áudio: ${e.message}"
    }
}

/**
 * Reproduz ou pausa um áudio
 */
fun reproduzirAudio(mensagem: Mensagem) {
    viewModelScope.launch {
        if (_playingAudioId.value == mensagem.id) {
            // Mesmo áudio - parar
            audioPlayer.stop()
            _playingAudioId.value = null
        } else {
            // Novo áudio
            mensagem.audio_url?.let { url ->
                audioPlayer.play(url)
                _playingAudioId.value = mensagem.id
            }
        }
    }
}

/**
 * Para a reprodução de áudio
 */
fun pararAudio() {
    audioPlayer.stop()
    _playingAudioId.value = null
}

/**
 * Atualiza a duração da gravação a cada segundo
 */
private fun startDurationUpdater() {
    viewModelScope.launch {
        while (_isRecording.value) {
            _recordingDuration.value++
            delay(1000)
        }
        _recordingDuration.value = 0
    }
}

/**
 * Libera recursos ao destruir o ViewModel
 */
override fun onCleared() {
    super.onCleared()
    audioRecorder.cancelRecording()
    audioPlayer.release()
}
```

---

### 8️⃣ **Atualizar ChatScreen**

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Screens/ChatScreen.kt`

#### ✏️ Adicionar imports:
```kotlin
import com.oportunyfam_mobile_ong.Components.AudioMessageBubble
import com.oportunyfam_mobile_ong.Components.AudioRecordButton
import com.oportunyfam_mobile_ong.model.TipoMensagem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
```

#### ✏️ Modificar ChatScreen para solicitar permissão:
```kotlin
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
    var currentMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 🆕 ADICIONAR estados de áudio:
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val playingAudioId by viewModel.playingAudioId.collectAsState()
    val audioPlayerIsPlaying by viewModel.audioPlayerIsPlaying.collectAsState()
    val audioPlayerCurrentPosition by viewModel.audioPlayerCurrentPosition.collectAsState()
    val audioPlayerPlayingUrl by viewModel.audioPlayerPlayingUrl.collectAsState()

    // 🆕 ADICIONAR permissão de áudio:
    val audioPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )

    // ... código existente (LaunchedEffect, DisposableEffect, etc.) ...

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
            // ... código existente (mensagens de erro) ...

            // Lista de mensagens
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading && mensagens.isEmpty() -> {
                        // ... código existente ...
                    }
                    mensagens.isEmpty() -> {
                        // ... código existente ...
                    }
                    else -> {
                        // Agrupar mensagens por data e manter ordem cronológica
                        val mensagensAgrupadas = mensagens
                            .sortedBy { it.criado_em }
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

                                // Mensagens do dia
                                items(mensagensDoDia, key = { it.id }) { mensagem ->
                                    // 🆕 MODIFICAR para suportar áudio:
                                    val isUser = mensagem.id_pessoa == pessoaIdAtual
                                    
                                    when (mensagem.tipo) {
                                        TipoMensagem.AUDIO -> {
                                            // Mensagem de áudio
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 4.dp),
                                                horizontalArrangement = if (isUser) 
                                                    Arrangement.End else Arrangement.Start
                                            ) {
                                                AudioMessageBubble(
                                                    mensagem = mensagem,
                                                    isUser = isUser,
                                                    isPlaying = playingAudioId == mensagem.id && audioPlayerIsPlaying,
                                                    currentPosition = if (playingAudioId == mensagem.id) 
                                                        audioPlayerCurrentPosition else 0,
                                                    onPlayClick = {
                                                        viewModel.reproduzirAudio(mensagem)
                                                    }
                                                )
                                            }
                                        }
                                        else -> {
                                            // Mensagem de texto (código existente)
                                            ChatMessage(
                                                mensagem = mensagem,
                                                isUser = isUser
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 🆕 MODIFICAR Campo de entrada para incluir áudio:
            if (isRecording) {
                AudioRecordButton(
                    isRecording = true,
                    recordingDuration = recordingDuration,
                    onStartRecording = {},
                    onStopRecording = {
                        viewModel.pararGravacao(conversaId, pessoaIdAtual)
                    },
                    onCancelRecording = {
                        viewModel.cancelarGravacao()
                    }
                )
            } else {
                ChatInputFieldWithAudio(
                    currentMessage = currentMessage,
                    onMessageChange = { currentMessage = it },
                    onSendClick = {
                        if (currentMessage.isNotBlank()) {
                            viewModel.enviarMensagem(conversaId, pessoaIdAtual, currentMessage)
                            currentMessage = ""
                        }
                    },
                    onRecordClick = {
                        if (audioPermissionState.status.isGranted) {
                            viewModel.iniciarGravacao()
                        } else {
                            audioPermissionState.launchPermissionRequest()
                        }
                    },
                    enabled = !isLoading
                )
            }
        }
    }
}
```

#### ✏️ Criar novo componente ChatInputFieldWithAudio:
```kotlin
// 🆕 ADICIONAR esta função no ChatScreen.kt:

@Composable
fun ChatInputFieldWithAudio(
    currentMessage: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onRecordClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Campo de texto
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
            
            // Botão de enviar ou gravar
            if (currentMessage.isNotBlank()) {
                // Mostrar botão de enviar
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
            } else {
                // Mostrar botão de gravar áudio
                AudioRecordButton(
                    isRecording = false,
                    recordingDuration = 0,
                    onStartRecording = onRecordClick,
                    onStopRecording = {},
                    onCancelRecording = {},
                    enabled = enabled
                )
            }
        }
    }
}
```

---

### 9️⃣ **Atualizar AndroidManifest.xml**

**Arquivo:** `app/src/main/AndroidManifest.xml`

#### ✏️ Adicionar permissão:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Permissões existentes -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <!-- ... outras permissões ... -->
    
    <!-- 🆕 ADICIONAR: -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
        android:maxSdkVersion="28" />
    
    <application
        ...
    </application>
</manifest>
```

---

## 🔐 Firebase Storage Rules

**IMPORTANTE:** Atualizar as regras de segurança do Firebase Storage

### Arquivo: Firebase Console > Storage > Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Regras para áudios do chat
    match /audios/conversa_{conversaId}/{audioFile} {
      // Permitir upload para usuários autenticados
      allow write: if request.auth != null;
      
      // Permitir leitura para usuários autenticados
      allow read: if request.auth != null;
      
      // Limitar tamanho do arquivo (5MB)
      allow write: if request.resource.size < 5 * 1024 * 1024;
      
      // Permitir apenas arquivos de áudio
      allow write: if request.resource.contentType.matches('audio/.*');
    }
    
    // Regras para imagens de perfil (já existentes)
    match /perfil/{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## ✅ Checklist de Implementação

### Backend:
- [ ] Atualizar modelo de dados (adicionar `tipo`, `audio_url`, `audio_duracao`)
- [ ] Atualizar endpoint `POST /mensagens`
- [ ] Atualizar endpoint `GET /mensagens/:conversaId`
- [ ] Adicionar validações para mensagens de áudio
- [ ] Testar endpoints com Postman/Insomnia

### Firebase:
- [ ] Atualizar regras de segurança do Firebase Storage
- [ ] Testar upload/download de áudio manualmente
- [ ] Verificar estrutura do Realtime Database

### Frontend - Modelos:
- [ ] Criar enum `TipoMensagem` em `Mensagem.kt`
- [ ] Atualizar `data class Mensagem`
- [ ] Atualizar `data class MensagemRequest`
- [ ] Atualizar `data class MensagemFirebase`
- [ ] Atualizar função de conversão `toMensagem()`

### Frontend - Serviços:
- [ ] Criar `FirebaseAudioService.kt`
- [ ] Criar `AudioRecorder.kt`
- [ ] Criar `AudioPlayer.kt`
- [ ] Atualizar `FirebaseMensagemService.kt`

### Frontend - Componentes UI:
- [ ] Criar `AudioRecordButton.kt`
- [ ] Criar `AudioMessageBubble.kt`
- [ ] Criar função `formatarDuracaoAudio()`

### Frontend - ViewModel:
- [ ] Adicionar propriedades de áudio no `ChatViewModel`
- [ ] Implementar `iniciarGravacao()`
- [ ] Implementar `pararGravacao()`
- [ ] Implementar `cancelarGravacao()`
- [ ] Implementar `enviarMensagemAudio()`
- [ ] Implementar `reproduzirAudio()`
- [ ] Implementar `pararAudio()`
- [ ] Atualizar `onCleared()`

### Frontend - UI:
- [ ] Atualizar imports no `ChatScreen.kt`
- [ ] Adicionar estados de áudio
- [ ] Adicionar permissão de RECORD_AUDIO
- [ ] Modificar LazyColumn para suportar áudio
- [ ] Criar `ChatInputFieldWithAudio`
- [ ] Atualizar lógica de renderização de mensagens

### Permissões:
- [ ] Adicionar `RECORD_AUDIO` no `AndroidManifest.xml`
- [ ] Adicionar `WRITE_EXTERNAL_STORAGE` no `AndroidManifest.xml`
- [ ] Solicitar permissão em runtime

### Testes:
- [ ] Testar gravação de áudio
- [ ] Testar cancelamento de gravação
- [ ] Testar upload para Firebase Storage
- [ ] Testar criação de mensagem na API
- [ ] Testar sincronização com Firebase Database
- [ ] Testar reprodução de áudio
- [ ] Testar pausa/resume
- [ ] Testar múltiplos áudios
- [ ] Testar em diferentes dispositivos
- [ ] Testar com conexão lenta
- [ ] Testar offline (upload deve falhar graciosamente)

---

## 🧪 Testes

### Teste 1: Gravação de Áudio
1. Abrir uma conversa
2. Clicar no botão de microfone
3. Verificar se a permissão é solicitada
4. Falar por alguns segundos
5. Verificar se o cronômetro está funcionando
6. Clicar em cancelar - áudio deve ser descartado
7. Gravar novamente
8. Clicar em enviar - áudio deve ser enviado

### Teste 2: Reprodução de Áudio
1. Receber uma mensagem de áudio
2. Clicar no botão play
3. Verificar se o áudio reproduz
4. Verificar se a barra de progresso atualiza
5. Clicar em pause - áudio deve pausar
6. Clicar novamente - áudio deve continuar

### Teste 3: Múltiplos Áudios
1. Enviar vários áudios
2. Reproduzir um áudio
3. Enquanto reproduz, clicar em outro áudio
4. Verificar se o primeiro para e o segundo começa

### Teste 4: Edge Cases
- [ ] Áudio muito curto (< 1 segundo)
- [ ] Áudio muito longo (> 5 minutos)
- [ ] Sem conexão com internet
- [ ] Firebase Storage indisponível
- [ ] Permissão negada
- [ ] Microfone em uso por outro app

---

## 📊 Estimativa de Tempo

| Tarefa | Tempo Estimado |
|--------|----------------|
| Backend (API + DB) | 2-3 horas |
| Firebase Rules | 30 minutos |
| Modelos e Serviços | 2 horas |
| Componentes UI | 3 horas |
| ViewModel | 2 horas |
| Integração UI | 2 horas |
| Testes e Ajustes | 3 horas |
| **TOTAL** | **14-15 horas** |

---

## 🚨 Pontos de Atenção

### 1. Tamanho dos Arquivos
- Limite recomendado: 5MB por áudio
- Formato M4A com AAC geralmente resulta em ~1MB por minuto
- Áudios de 5 minutos = ~5MB

### 2. Custos Firebase
- Firebase Spark (gratuito): 5GB storage, 1GB/dia download
- Monitorar uso se houver muitos usuários

### 3. Performance
- Não manter múltiplos MediaPlayers ativos
- Liberar recursos quando não estiver em uso
- Cache local de áudios reproduzidos recentemente

### 4. UX
- Feedback visual claro durante gravação
- Indicador de progresso durante upload
- Mensagem de erro amigável se falhar

### 5. Segurança
- Validar tipo de arquivo no upload
- Limitar tamanho máximo
- Regras do Firebase bem configuradas
- Não permitir upload de arquivos maliciosos

---

## 📞 Suporte

Se encontrar problemas durante a implementação:

1. Verificar logs do Android Studio (Logcat)
2. Verificar console do Firebase
3. Verificar resposta da API no Postman
4. Revisar este documento para verificar se todos os passos foram seguidos

---

## 🎉 Conclusão

Após seguir todos os passos deste documento, o chat terá suporte completo para mensagens de áudio:

✅ Gravação de áudio com interface intuitiva  
✅ Upload seguro para Firebase Storage  
✅ Sincronização em tempo real  
✅ Reprodução com controles completos  
✅ Experiência de usuário fluida  

**Boa implementação! 🚀**

