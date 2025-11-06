# 🔐 Configuração de Credenciais do Azure Storage

## ⚠️ IMPORTANTE - Segurança

As credenciais do Azure Storage **NÃO DEVEM** ser commitadas no repositório público.

---

## 📝 Como Configurar

### **Opção 1: Variável de Ambiente (Recomendado)**

#### **Windows:**
```cmd
setx AZURE_STORAGE_KEY "SUA_CHAVE_AQUI"
```

#### **Linux/Mac:**
```bash
export AZURE_STORAGE_KEY="SUA_CHAVE_AQUI"
```

### **Opção 2: Arquivo local.properties**

1. Crie/edite o arquivo `local.properties` na raiz do projeto:
```properties
azure.storage.account=oportunyfamstorage
azure.storage.key=SUA_CHAVE_AQUI
azure.storage.container=imagens-perfil
```

2. Modifique o código para ler desse arquivo (já está no .gitignore).

### **Opção 3: Hardcode Temporário (Apenas para Desenvolvimento)**

⚠️ **NUNCA FAÇA COMMIT** com a chave hardcoded!

No arquivo `PerfilScreen.kt`, linha ~115:
```kotlin
val accountKey = "SUA_CHAVE_AQUI"
```

**Antes de commitar, sempre substitua por:**
```kotlin
val accountKey = System.getenv("AZURE_STORAGE_KEY") 
    ?: "CONFIGURE_SUA_CHAVE_AQUI"
```

---

## 🔑 Credenciais do Azure Storage

### **Informações Necessárias:**

- **Storage Account:** `oportunyfamstorage`
- **Account Key:** *(Obtenha no Portal do Azure)*
- **Container:** `imagens-perfil`

### **Onde Encontrar a Chave:**

1. Acesse o [Portal do Azure](https://portal.azure.com)
2. Navegue até **Storage Accounts**
3. Selecione `oportunyfamstorage`
4. Vá em **Access Keys** (no menu lateral)
5. Copie a **key1** ou **key2**

---

## ✅ Checklist Antes de Commitar

- [ ] A chave do Azure NÃO está hardcoded no código
- [ ] O arquivo `.idea/copilotDiffState.xml` está no .gitignore
- [ ] Nenhum arquivo de configuração local com secrets será commitado
- [ ] As credenciais estão em variáveis de ambiente ou `local.properties`

---

## 🚨 Se Você Commitou uma Chave por Engano

1. **Reverta o commit:**
   ```bash
   git reset --soft HEAD~1
   ```

2. **Remova a chave do código**

3. **Regenere a chave no Azure** (por segurança)

4. **Faça um novo commit limpo**

---

## 📚 Links Úteis

- [Azure Storage Documentation](https://docs.microsoft.com/azure/storage/)
- [Best Practices for Managing Secrets](https://docs.github.com/code-security/secret-scanning)
- [Android Gradle Build Configuration](https://developer.android.com/studio/build/gradle-tips)

---

**Data:** 2025-11-06  
**Status:** ✅ Configuração Necessária  
**Prioridade:** 🔴 Alta (Segurança)

