# ☁️ Configuração do Azure Storage

## ✅ Status Atual
**Azure Storage está CONFIGURADO e PRONTO para uso!**

### 📋 Credenciais Configuradas
- **Storage Account**: `oportunyfamstorage`
- **Access Key**: Configurada ✅
- **Containers**:
  - `imagens-perfil` - Para fotos de perfil das instituições
  - `imagens-publicacoes` - Para imagens das publicações

---

## 🔧 Como Funciona

### 1. Configuração Direta no Código (ATUAL)
A chave está configurada diretamente em `AzureConfig.kt`:
```kotlin
private const val AZURE_STORAGE_KEY = "1dY9IPE70NwB..."
```

**✅ Vantagem**: Funciona imediatamente, sem necessidade de Gradle sync  
**⚠️ Desvantagem**: Chave fica exposta no código-fonte

---

### 2. Configuração via local.properties (RECOMENDADO)
A chave também está em `local.properties`:
```properties
azure.storage.key=1dY9IPE70NwB...
azure.storage.account=oportunyfamstorage
```

**Para usar esta configuração:**
1. Remova a constante `AZURE_STORAGE_KEY` de `AzureConfig.kt`
2. Execute: `File > Sync Project with Gradle Files`
3. Rebuild o projeto

**✅ Vantagem**: Mais seguro, `local.properties` nunca vai pro Git  
**⚠️ Desvantagem**: Requer Gradle sync após mudanças

---

## 🚀 Testando o Upload

### Upload de Foto de Perfil
1. Abra o app e faça login como instituição
2. Vá para a tela de Perfil
3. Clique no botão de câmera na foto de perfil
4. Selecione uma imagem
5. A imagem será enviada para o Azure e atualizada automaticamente

### Upload de Imagem em Publicação
1. Na tela de Perfil, clique no botão "+"
2. Preencha título e descrição
3. Clique em "Selecionar Imagem"
4. Escolha uma imagem
5. Publique

---

## 📊 Logs de Debug

Verifique os logs no Logcat:
```
AzureConfig: ✅ Usando chave configurada diretamente no código
PerfilScreen: Iniciando upload da imagem...
PerfilScreen: Upload retornou URL: https://oportunyfamstorage.blob.core.windows.net/...
PerfilScreen: ✅ Foto de perfil atualizada com sucesso!
PerfilScreen: 🔄 Trigger de reload incrementado para: 1
```

---

## 🔒 Segurança

### ⚠️ IMPORTANTE - Antes de Fazer Commit

**Remova a chave do código:**
1. Edite `AzureConfig.kt`
2. Mude a constante para:
```kotlin
private const val AZURE_STORAGE_KEY = "" // Configurar no local.properties
```
3. Mantenha apenas no `local.properties` (que está no .gitignore)

### Arquivos que NÃO devem ir para o Git:
- ✅ `local.properties` - Já está no .gitignore
- ✅ Chaves de API no código - REMOVER antes do commit

---

## 🛠️ Troubleshooting

### Problema: "Azure Storage Key não configurada"
**Solução**: 
- Verifique se `AzureConfig.kt` tem a constante `AZURE_STORAGE_KEY` preenchida
- OU faça Gradle sync se estiver usando `local.properties`

### Problema: Upload falha com erro 403
**Solução**:
- Verifique se a chave está correta
- Confirme que os containers existem no Azure

### Problema: Imagem não atualiza após upload
**Solução**:
- Verifique os logs: deve aparecer "🔄 Trigger de reload incrementado"
- O sistema usa cache disabled e URL versionada
- O `reloadTrigger` força recomposição da imagem

---

## 📱 Estrutura dos Arquivos

```
app/
├── src/main/java/.../Config/
│   └── AzureConfig.kt              # Configuração principal
├── src/main/java/.../Screens/
│   └── PerfilScreen.kt             # Upload de foto de perfil
├── src/main/java/.../Network/
│   └── AzureBlobRetrofit.kt        # Cliente de upload
└── build.gradle.kts                # Lê local.properties

local.properties                     # Chaves (não vai pro Git)
```

---

## ✨ Próximos Passos

1. ✅ Testar upload de foto de perfil
2. ✅ Testar upload de imagem em publicação
3. ⚠️ Remover chave do código antes do commit
4. ✅ Configurar containers no Azure (se necessário)

---

**Data da Configuração**: 2025-11-11  
**Configurado por**: Sistema Automatizado  
**Status**: ✅ ATIVO E FUNCIONANDO

