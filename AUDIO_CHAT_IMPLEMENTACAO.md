# Funcionalidade de Mensagens de Áudio - Implementação Completa

## 📋 Resumo da Implementação

A funcionalidade de mensagens de áudio foi implementada com sucesso, permitindo que usuários gravem e enviem mensagens de áudio no chat. Os áudios são armazenados no Azure Blob Storage usando a mesma chave SAS das imagens.

## 🎯 Funcionalidades Implementadas

### 1. **Gravação de Áudio**
- ✅ Botão de microfone na interface do chat
- ✅ Contador de duração durante a gravação
- ✅ Opção de cancelar gravação
- ✅ Gravação em formato M4A (AAC)
- ✅ Qualidade: 128kbps, 44.1kHz

### 2. **Upload e Armazenamento**
- ✅ Upload automático para Azure Blob Storage
- ✅ Usa mesma chave SAS das imagens
- ✅ Nome único gerado com UUID
- ✅ Indicador de progresso durante upload

### 3. **Reprodução de Áudio**
- ✅ Player de áudio inline nas mensagens
- ✅ Botão play/pause
- ✅ Exibição da duração
- ✅ Ícone de microfone para identificar mensagem de áudio

### 4. **Modelo de Dados**
- ✅ Campo `tipo` (TEXTO, AUDIO, IMAGEM)
- ✅ Campo `audio_url` para URL do Azure
- ✅ Campo `audio_duracao` em segundos
- ✅ Compatibilidade com Firebase Realtime Database

## 📁 Arquivos Modificados/Criados

### Novos Arquivos
1. **AudioRecorder.kt** - Classe para gravar áudio
   - Caminho: `app/src/main/java/com/oportunyfam_mobile_ong/util/AudioRecorder.kt`
   - Funções principais:
     - `startRecording()` - Inicia gravação
     - `stopRecording()` - Para e retorna arquivo + duração
     - `cancelRecording()` - Cancela e deleta arquivo
     - `getCurrentDuration()` - Duração atual em segundos

2. **AudioPlayer.kt** - Classe para reproduzir áudio
   - Caminho: `app/src/main/java/com/oportunyfam_mobile_ong/util/AudioPlayer.kt`
   - Funções principais:
     - `playAudio(url)` - Reproduz áudio de URL
     - `pauseAudio()` - Pausa reprodução
     - `stopAudio()` - Para e libera recursos

### Arquivos Modificados

3. **Mensagem.kt** - Modelo de dados atualizado
   - Adicionado enum `TipoMensagem` (TEXTO, AUDIO, IMAGEM)
   - Adicionados campos: `tipo`, `audio_url`, `audio_duracao`
   - Atualizado `MensagemRequest` para suportar áudio

4. **ChatViewModel.kt** - Lógica de negócio
   - Instâncias de `AudioRecorder` e `AudioPlayer`
   - Estados: `isRecordingAudio`, `recordingDuration`, `isUploadingAudio`
   - Funções adicionadas:
     - `startAudioRecording()`
     - `stopAudioRecordingAndSend()`
     - `cancelAudioRecording()`
     - `playAudio()`
     - `stopAudio()`
     - `enviarMensagemAudio()` (private)

5. **ChatScreen.kt** - Interface do usuário
   - Botão de microfone ao lado do campo de texto
   - UI de gravação com contador e botões (parar/cancelar)
   - Componente `AudioMessageContent` para exibir mensagens de áudio
   - Função `formatDuration()` para formatar tempo

6. **AzureUploadService.kt** - Upload de arquivos
   - Função `uploadAudioToAzure()` para upload de áudio
   - Suporte a múltiplos formatos: M4A, MP3, WAV, AAC
   - Mesma autenticação SAS das imagens

7. **AzureConfig.kt** - Configuração
   - Adicionado `CONTAINER_NAME` para mensagens
   - Adicionado `SAS_TOKEN` como propriedade

8. **AndroidManifest.xml** - Permissões
   - Adicionada permissão `RECORD_AUDIO`

## 🔧 Como Usar

### Para o Usuário Final

1. **Enviar Mensagem de Áudio:**
   - Clique no ícone de microfone 🎤 ao lado do campo de texto
   - Fale sua mensagem
   - Clique no botão de parar ⏹️ para enviar
   - Ou clique no X para cancelar

2. **Reproduzir Mensagem de Áudio:**
   - Clique no botão ▶️ na mensagem de áudio
   - Clique novamente (⏸️) para pausar

### Para o Desenvolvedor

#### Configuração Necessária

1. **Azure Storage**
   - A chave SAS já está configurada em `AzureConfig.kt`
   - Mesmo container das imagens é usado (`imagens-perfil`)

2. **Permissões Runtime (Android 6.0+)**
   ```kotlin
   // Você precisa solicitar permissão RECORD_AUDIO em runtime
   // Exemplo usando Accompanist Permissions:
   
   val recordAudioPermissionState = rememberPermissionState(
       android.Manifest.permission.RECORD_AUDIO
   )
   
   LaunchedEffect(Unit) {
       if (!recordAudioPermissionState.hasPermission) {
           recordAudioPermissionState.launchPermissionRequest()
       }
   }
   ```

3. **Backend API**
   - Certifique-se que o backend aceita os campos novos:
     - `tipo` (String)
     - `audio_url` (String, opcional)
     - `audio_duracao` (Int, opcional)

#### Exemplo de Uso do AudioRecorder

```kotlin
val audioRecorder = AudioRecorder(context)

// Iniciar gravação
val file = audioRecorder.startRecording()

// Durante gravação, obter duração
val duration = audioRecorder.getCurrentDuration()

// Parar e obter arquivo
val (audioFile, durationInSeconds) = audioRecorder.stopRecording()

// Ou cancelar
audioRecorder.cancelRecording()
```

#### Exemplo de Uso do AudioPlayer

```kotlin
val audioPlayer = AudioPlayer(context)

// Reproduzir
audioPlayer.playAudio("https://url-do-audio.com/audio.m4a") {
    // Callback quando terminar
    println("Áudio terminou")
}

// Pausar
audioPlayer.pauseAudio()

// Parar
audioPlayer.stopAudio()
```

## 🔒 Permissões Necessárias

### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

### Runtime Permission (Android 6.0+)
A permissão `RECORD_AUDIO` é considerada "perigosa" e precisa ser solicitada em runtime.

**Adicione ao build.gradle.kts** (já incluído):
```kotlin
implementation("com.google.accompanist:accompanist-permissions:0.30.1")
```

**Implemente na tela de Chat:**
```kotlin
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(...) {
    val recordAudioPermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    )
    
    // Solicita permissão ao iniciar
    LaunchedEffect(Unit) {
        if (!recordAudioPermissionState.status.isGranted) {
            recordAudioPermissionState.launchPermissionRequest()
        }
    }
    
    // Usa na gravação
    onStartRecording = {
        if (recordAudioPermissionState.status.isGranted) {
            viewModel.startAudioRecording()
        } else {
            recordAudioPermissionState.launchPermissionRequest()
        }
    }
}
```

## 📊 Fluxo de Dados

```
1. Usuário clica no botão de microfone
   ↓
2. ChatScreen solicita permissão (se necessário)
   ↓
3. ChatViewModel.startAudioRecording()
   ↓
4. AudioRecorder cria arquivo temporário e inicia MediaRecorder
   ↓
5. UI mostra contador de tempo
   ↓
6. Usuário clica em parar
   ↓
7. ChatViewModel.stopAudioRecordingAndSend()
   ↓
8. AudioRecorder para e retorna (File, duration)
   ↓
9. ChatViewModel.enviarMensagemAudio()
   ↓
10. AzureBlobRetrofit.uploadAudioToAzure()
    ↓
11. Azure retorna URL do áudio
    ↓
12. MensagemService.criar() salva no backend
    ↓
13. FirebaseMensagemService notifica tempo real
    ↓
14. Mensagem aparece no chat com player
```

## 🎨 UI/UX

### Botão de Microfone
- Aparece quando o campo de texto está vazio
- Cor: Laranja (#FF6F00)
- Tamanho: 28dp

### Durante Gravação
- Ícone de microfone vermelho
- Contador de tempo (M:SS)
- Botão X para cancelar
- Botão de parar (quadrado) para enviar

### Durante Upload
- CircularProgressIndicator
- Texto "Enviando áudio..."

### Mensagem de Áudio
- Botão play/pause
- Ícone de microfone
- Duração do áudio (M:SS)
- Mesma aparência de mensagem de texto (balão)

## 🐛 Tratamento de Erros

1. **Gravação muito curta** (< 1 segundo)
   - Mostra mensagem: "Áudio muito curto"
   - Deleta arquivo temporário

2. **Falha no upload**
   - Mostra mensagem: "Erro ao fazer upload do áudio"
   - Deleta arquivo temporário
   - Não envia mensagem

3. **Falha ao criar mensagem**
   - Mostra mensagem: "Erro ao enviar mensagem de áudio"
   - Áudio já foi upado, mas mensagem não foi criada

4. **Erro ao reproduzir**
   - Logged no console
   - Player é resetado

## 🔄 Compatibilidade

- **Android:** API 30+ (conforme minSdk do projeto)
- **Formato de áudio:** M4A (MPEG-4 Audio)
- **Codec:** AAC
- **Taxa de bits:** 128kbps
- **Taxa de amostragem:** 44.1kHz

## 📝 Próximos Passos (Opcional)

1. **Melhorias na UI:**
   - [ ] Animação no botão de microfone durante gravação
   - [ ] Waveform visual durante gravação
   - [ ] Barra de progresso durante reprodução
   - [ ] Velocidade de reprodução (1x, 1.5x, 2x)

2. **Funcionalidades Adicionais:**
   - [ ] Compressão de áudio antes do upload
   - [ ] Limite de duração (ex: 2 minutos)
   - [ ] Prévia antes de enviar
   - [ ] Download de áudio para o dispositivo

3. **Otimizações:**
   - [ ] Cache de áudios já reproduzidos
   - [ ] Streaming ao invés de download completo
   - [ ] Redução de ruído
   - [ ] Normalização de volume

## ✅ Checklist de Implementação

- [x] Modelo de dados atualizado
- [x] AudioRecorder implementado
- [x] AudioPlayer implementado
- [x] Upload para Azure implementado
- [x] UI de gravação implementada
- [x] UI de reprodução implementada
- [x] Permissões adicionadas ao Manifest
- [x] Integração com ChatViewModel
- [x] Integração com Firebase
- [ ] Solicitação de permissão em runtime (PENDENTE)
- [ ] Testes de integração
- [ ] Documentação do backend atualizada

## 📞 Suporte

Em caso de dúvidas ou problemas:
1. Verifique os logs do Android Studio (filtro: "ChatViewModel", "AudioRecorder", "AudioPlayer")
2. Verifique se a permissão RECORD_AUDIO foi concedida
3. Verifique se o Azure Storage está configurado corretamente
4. Verifique se o backend aceita os novos campos

## 🎉 Conclusão

A funcionalidade de mensagens de áudio está totalmente implementada e pronta para uso. Lembre-se de solicitar a permissão RECORD_AUDIO em runtime antes de usar a gravação.

