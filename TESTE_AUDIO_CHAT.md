# 🎤 Guia Rápido - Como Testar Mensagens de Áudio

## ✅ Pré-requisitos

1. **Dispositivo ou Emulador com microfone**
   - Dispositivo físico Android (recomendado)
   - Ou emulador com microfone virtual configurado

2. **Permissão RECORD_AUDIO**
   - Será solicitada automaticamente na primeira vez que clicar no botão de microfone
   - Você deve aceitar para poder gravar áudio

3. **Conexão com Internet**
   - Necessária para fazer upload do áudio para Azure
   - Necessária para reproduzir áudios de outros usuários

## 🧪 Passo a Passo para Testar

### 1. Enviar Mensagem de Áudio

1. Abra o app e navegue até uma conversa
2. Certifique-se que o campo de texto está vazio
3. Clique no botão de **microfone** 🎤 (aparece ao lado do campo de texto)
4. Na primeira vez, aceite a permissão de gravação de áudio
5. Fale sua mensagem (você verá um contador de tempo)
6. Clique no botão **quadrado vermelho** ⏹️ para parar e enviar
   - Ou clique no **X** para cancelar a gravação
7. Aguarde o upload (você verá "Enviando áudio...")
8. A mensagem de áudio aparecerá no chat

### 2. Reproduzir Mensagem de Áudio

1. Na mensagem de áudio, clique no botão **▶️** (play)
2. O áudio começará a tocar
3. Clique novamente para pausar (⏸️)

### 3. Enviar Mensagem de Texto

1. Digite uma mensagem no campo de texto
2. Observe que o botão de microfone **desaparece** quando há texto
3. Clique no botão de **enviar** ➤ que aparece no lugar

## 🔍 O Que Observar

### Durante a Gravação
- ✅ Ícone de microfone vermelho aparece
- ✅ Contador de tempo no formato "M:SS" (ex: 0:05, 1:23)
- ✅ Botão X (cancelar) à esquerda
- ✅ Botão quadrado (parar/enviar) à direita

### Durante o Upload
- ✅ Indicador de progresso circular
- ✅ Texto "Enviando áudio..."
- ✅ Campo de entrada desabilitado

### Mensagem de Áudio Enviada
- ✅ Balão de mensagem com cor diferente (verde claro se você enviou, branco se recebeu)
- ✅ Botão play/pause
- ✅ Ícone de microfone
- ✅ Duração do áudio (ex: "0:15")
- ✅ Hora de envio
- ✅ Indicador de lido (✓✓ ou ✓)

## 🐛 Problemas Comuns e Soluções

### 1. Botão de Microfone Não Aparece
**Problema:** O campo de texto tem conteúdo  
**Solução:** Apague todo o texto do campo

### 2. Permissão Negada
**Problema:** Você negou a permissão de áudio  
**Solução:** 
- Vá em Configurações do Android > Apps > OportunyFam > Permissões
- Ative a permissão "Microfone"

### 3. Erro ao Gravar
**Problema:** "Erro ao iniciar gravação de áudio"  
**Possíveis Causas:**
- Outro app está usando o microfone
- Microfone não disponível no emulador
- Permissão não concedida

**Solução:**
- Feche outros apps que usam o microfone
- Use um dispositivo físico
- Verifique as permissões

### 4. Erro ao Fazer Upload
**Problema:** "Erro ao fazer upload do áudio"  
**Possíveis Causas:**
- Sem conexão com Internet
- Azure Storage não configurado corretamente
- Token SAS expirado

**Solução:**
- Verifique a conexão com Internet
- Verifique os logs para detalhes do erro
- Confirme que `AzureConfig.STORAGE_KEY` está correto

### 5. Áudio Muito Curto
**Problema:** "Áudio muito curto"  
**Causa:** Gravação com menos de 1 segundo  
**Solução:** Grave por pelo menos 1 segundo antes de parar

### 6. Erro ao Reproduzir
**Problema:** Áudio não reproduz  
**Possíveis Causas:**
- URL do áudio inválida
- Sem conexão com Internet
- Arquivo corrompido

**Solução:**
- Verifique a conexão com Internet
- Tente reproduzir novamente
- Verifique os logs do AudioPlayer

## 📱 Testando no Emulador

### Configurar Microfone Virtual

1. No Android Studio, com o emulador aberto:
   - Vá em: **Tools > Device Manager**
   - Clique nos 3 pontos do emulador > **Settings**
   - Vá em: **Microphone**
   - Selecione: **Virtual microphone uses host audio input**

2. Ou use microfone de um arquivo:
   - Grave um áudio WAV no seu computador
   - Use o comando:
   ```bash
   adb push audio.wav /sdcard/Download/
   ```

### Limitações do Emulador
- ⚠️ Qualidade de áudio pode ser inferior
- ⚠️ Pode haver atrasos
- ⚠️ Recomenda-se testar em dispositivo real

## 📊 Verificando Logs

Para depurar problemas, filtre os logs por:

```
ChatViewModel
AudioRecorder
AudioPlayer
AzureBlobRetrofit
```

### Logs Importantes

**Gravação iniciada:**
```
D/AudioRecorder: 🎤 Gravação iniciada: /data/.../audio_123456.m4a
```

**Gravação finalizada:**
```
D/AudioRecorder: Gravação finalizada. Duração: 5 segundos
```

**Upload iniciado:**
```
I/System.out: 🎤 Iniciando upload de áudio para Azure Storage...
```

**Upload concluído:**
```
I/System.out: ✅ Upload de áudio bem-sucedido para: https://...
D/ChatViewModel: ✅ Upload concluído: https://...
```

**Mensagem enviada:**
```
D/ChatViewModel: ✅ Mensagem de áudio enviada: 123
```

## 🎯 Cenários de Teste

### Teste Básico
1. ✅ Gravar áudio de 5 segundos
2. ✅ Enviar
3. ✅ Reproduzir

### Teste de Cancelamento
1. ✅ Iniciar gravação
2. ✅ Gravar por 3 segundos
3. ✅ Clicar em cancelar (X)
4. ✅ Verificar que não aparece mensagem

### Teste de Áudio Curto
1. ✅ Iniciar gravação
2. ✅ Parar imediatamente (< 1 segundo)
3. ✅ Verificar mensagem de erro "Áudio muito curto"

### Teste de Múltiplos Áudios
1. ✅ Enviar 3 áudios seguidos
2. ✅ Verificar que todos aparecem
3. ✅ Reproduzir cada um

### Teste de Texto e Áudio
1. ✅ Enviar mensagem de texto
2. ✅ Enviar mensagem de áudio
3. ✅ Enviar outra mensagem de texto
4. ✅ Verificar que ambos os tipos aparecem corretamente

### Teste de Reprodução Simultânea
1. ✅ Reproduzir primeiro áudio
2. ✅ Enquanto toca, clicar em outro áudio
3. ✅ Verificar que o primeiro para e o segundo começa

### Teste de Permissão
1. ✅ Negar permissão na primeira vez
2. ✅ Tentar gravar novamente
3. ✅ Verificar que solicita permissão novamente
4. ✅ Aceitar e gravar

## ✨ Funcionalidades Esperadas

- [x] Botão de microfone aparece quando campo está vazio
- [x] Botão de enviar aparece quando há texto
- [x] Solicita permissão RECORD_AUDIO na primeira gravação
- [x] Mostra contador de tempo durante gravação
- [x] Pode cancelar gravação (X)
- [x] Pode parar e enviar (⏹️)
- [x] Mostra indicador de upload
- [x] Mensagem de áudio aparece no chat
- [x] Pode reproduzir áudio (▶️)
- [x] Pode pausar áudio (⏸️)
- [x] Duração do áudio é exibida
- [x] Áudios são salvos no Azure
- [x] Notificação em tempo real via Firebase

## 📈 Métricas de Sucesso

- ✅ Gravação funciona em 100% dos dispositivos Android 6.0+
- ✅ Upload completa em menos de 5 segundos (para áudio de 30s)
- ✅ Reprodução inicia em menos de 2 segundos
- ✅ Sem crashes durante gravação/reprodução
- ✅ Interface intuitiva (não requer tutorial)

## 🎓 Dicas para Usuários Finais

1. **Grave em Local Silencioso**
   - Menos ruído = melhor qualidade

2. **Mantenha o Telefone Próximo à Boca**
   - Distância ideal: 15-20cm

3. **Verifique a Conexão**
   - Wi-Fi ou 4G para melhor experiência

4. **Seja Breve**
   - Mensagens curtas são mais fáceis de ouvir

5. **Pré-visualize Mentalmente**
   - Pense no que vai dizer antes de gravar

## 🔧 Configuração para Produção

Antes de publicar:

1. ✅ Verificar que Token SAS não está expirado
2. ✅ Configurar limite de duração (opcional)
3. ✅ Implementar compressão de áudio (opcional)
4. ✅ Adicionar analytics para monitorar uso
5. ✅ Testar em diferentes dispositivos Android
6. ✅ Testar com conexões lentas (3G)
7. ✅ Documentar para equipe de suporte

---

**Data da Implementação:** 18/11/2025  
**Versão do App:** 1.0  
**Status:** ✅ Pronto para Testes

