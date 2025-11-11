# 🔍 TESTE: Verificar Se Foto Individual Está Funcionando

## ✅ Como Testar

### Passo 1: Verificar Logs
```bash
adb logcat | grep "AtividadeViewModel\|AtividadeFoto\|DetalhesAtividade"
```

**Logs esperados:**
```
📷 Carregando fotos salvas para X atividades
📷 Atividade 11 (Natacao) sem foto salva
📷 Foto carregada para atividade 12 (Futebol): https://...
✅ Carregamento de fotos concluído
```

---

### Passo 2: Testar Adicionar Foto

1. **Abra o app**
2. **Vá para "Atividades"**
3. **Clique em "Natacao"**
4. **Observe o card amarelo com a foto**
5. **Clique no ícone 📷 no canto da foto**
6. **Selecione uma foto**
7. **Aguarde mensagem "Foto da atividade atualizada!"**

**Logs esperados:**
```
📤 Fazendo upload da foto da atividade ID: 11
✅ Foto salva localmente para atividade 11
✅ Foto também atualizada na API!  ← NOVO!
```

**OU (se API não suporta campo 'foto'):**
```
📤 Fazendo upload da foto da atividade ID: 11
✅ Foto salva localmente para atividade 11
⚠️ API não aceitou campo 'foto' (400), mantido apenas local
```

---

### Passo 2B: Verificar Requisição na API

```bash
adb logcat | grep "okhttp.OkHttpClient"
```

**Procurar por:**
```
--> PUT https://.../atividades/11
Content-Type: application/json

{
  "titulo": "Natacao",
  "foto": "https://storage.../foto.jpg",  ← Deve aparecer!
  ...
}

<-- 200 OK  ← Se API aceitou
```

---

### Passo 3: Verificar Lista

1. **Volte para lista de atividades**
2. **Veja se APENAS "Natacao" mudou**
3. **Outras atividades devem estar inalteradas**

---

### Passo 4: Verificar Persistência

1. **Feche o app completamente**
2. **Reabra o app**
3. **Vá para "Atividades"**
4. **Veja se a foto de "Natacao" ainda está lá**

**Logs esperados:**
```
📷 Carregando fotos salvas para X atividades
📷 Foto carregada para atividade 11 (Natacao): https://...
```

---

## 🐛 Problemas Possíveis

### Problema 1: Contexto é Null
**Log:** `⚠️ Contexto é null, não pode carregar fotos salvas`

**Solução:** O ViewModel não foi criado com contexto. Verifique se está usando `AtividadeViewModelFactory`.

### Problema 2: Foto Não Aparece
**Possíveis causas:**
- Upload falhou (verifique Azure configurado)
- DataStore não salvou (verifique permissões)
- Cache do Coil (limpe cache do app)

### Problema 3: Todas Atividades Mudam
**Causa:** Código antigo ainda em uso.

**Solução:** Certifique-se de que:
- `AtividadeCardAPI.kt` usa `atividade.atividade_foto ?: atividade.instituicao_foto`
- `ResumoAtividadeCardAPI.kt` usa `atividade.atividade_foto ?: atividade.instituicao_foto`

---

## 🔧 Comandos Úteis

### Ver Logs em Tempo Real
```bash
adb logcat -c && adb logcat | grep -E "Atividade|Foto"
```

### Limpar Cache do App
```bash
adb shell pm clear com.oportunyfam_mobile_ong
```

### Reinstalar App
```bash
./gradlew.bat uninstallDebug installDebug
```

---

## ✅ Checklist de Verificação

- [ ] Logs mostram "📷 Carregando fotos salvas"
- [ ] Ícone 📷 aparece no card da atividade
- [ ] Clique no 📷 abre galeria
- [ ] Upload funciona (snackbar de sucesso)
- [ ] Foto aparece no card após upload
- [ ] Apenas atividade editada muda
- [ ] Outras atividades inalteradas
- [ ] Foto persiste após reiniciar app
- [ ] Logs mostram foto sendo carregada do DataStore

---

## 📊 Exemplo de Teste Completo

```
TESTE: Três Atividades

Situação Inicial:
- Natacao: sem foto (ícone padrão)
- Futebol: sem foto (ícone padrão)
- Yoga: sem foto (ícone padrão)

Ação 1: Adicionar foto para Natacao
1. Clique em "Natacao"
2. Clique em 📷
3. Selecione foto_piscina.jpg
4. ✅ Snackbar: "Foto da atividade atualizada!"

Resultado Esperado:
- Natacao: [Foto Piscina] ✅
- Futebol: [Ícone padrão] ✅
- Yoga: [Ícone padrão] ✅

Ação 2: Adicionar foto para Futebol
1. Clique em "Futebol"
2. Clique em 📷
3. Selecione foto_campo.jpg
4. ✅ Snackbar: "Foto da atividade atualizada!"

Resultado Esperado:
- Natacao: [Foto Piscina] ✅ (inalterada!)
- Futebol: [Foto Campo] ✅
- Yoga: [Ícone padrão] ✅

Ação 3: Reiniciar App
1. Feche o app
2. Reabra o app
3. Vá para "Atividades"

Resultado Esperado:
- Natacao: [Foto Piscina] ✅ (persistiu!)
- Futebol: [Foto Campo] ✅ (persistiu!)
- Yoga: [Ícone padrão] ✅

✅ TESTE PASSOU!
```

---

## 🆘 Se Nada Funcionar

### Passo 1: Verificar Azure
```kotlin
// AzureConfig.kt
isConfigured() deve retornar true
```

### Passo 2: Verificar ViewModel
```kotlin
// AtividadesScreen.kt
val viewModel: AtividadeViewModel = viewModel(
    factory = AtividadeViewModelFactory(context)  // ✅ Tem que ter!
)
```

### Passo 3: Limpar Tudo
```bash
# Limpar cache
adb shell pm clear com.oportunyfam_mobile_ong

# Rebuild
./gradlew.bat clean assembleDebug

# Reinstalar
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

**Boa sorte! Se precisar de ajuda, compartilhe os logs!** 🚀

