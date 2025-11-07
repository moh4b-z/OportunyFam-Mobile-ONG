# Configuração de Segurança - Azure Storage

## ⚠️ IMPORTANTE: Segurança das Chaves

**NUNCA** commite chaves de acesso diretamente no código ou no Git!

## 📋 Configuração

### Opção 1: Variável de Ambiente (Recomendado para Produção)

**Windows (PowerShell):**
```powershell
$env:AZURE_STORAGE_KEY="SUA_CHAVE_AQUI"
```

**Windows (CMD):**
```cmd
set AZURE_STORAGE_KEY=SUA_CHAVE_AQUI
```

**Linux/Mac:**
```bash
export AZURE_STORAGE_KEY="SUA_CHAVE_AQUI"
```

### Opção 2: Arquivo `.env` (Recomendado para Desenvolvimento)

1. Copie o arquivo `.env.example` para `.env`:
   ```bash
   cp .env.example .env
   ```

2. Edite o arquivo `.env` e adicione sua chave:
   ```
   AZURE_STORAGE_KEY=SUA_CHAVE_AQUI
   ```

3. O arquivo `.env` está no `.gitignore` e **NÃO será commitado**.

### Opção 3: local.properties (Android Studio)

Adicione no arquivo `local.properties`:
```properties
azure.storage.key=SUA_CHAVE_AQUI
```

## 🔧 Como Funciona

O arquivo `AzureConfig.kt` busca a chave na seguinte ordem:

1. **Variável de ambiente** `AZURE_STORAGE_KEY`
2. **Propriedade do sistema** `azure.storage.key`
3. Se nenhuma for encontrada, lança erro

## 🚫 O Que NÃO Fazer

❌ **NÃO** adicione chaves diretamente no código:
```kotlin
val key = "1dY9IPE70..." // ERRADO!
```

✅ **SIM** use AzureConfig:
```kotlin
val key = AzureConfig.getStorageKey() // CORRETO!
```

## 🔐 Recuperando a Chave

Se você perdeu a chave ou precisa regerar:

1. Acesse o [Azure Portal](https://portal.azure.com)
2. Navegue para sua Storage Account: `oportunyfamstorage`
3. Vá em **Access Keys**
4. Copie uma das chaves (key1 ou key2)
5. Configure seguindo as opções acima

## 📝 Commit no Git

Agora você pode fazer commit sem expor chaves:

```bash
git add .
git commit -m "Sua mensagem"
git push
```

O GitHub não bloqueará mais o push! 🎉

