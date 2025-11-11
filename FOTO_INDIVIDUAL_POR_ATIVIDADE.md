# ✅ SOLUÇÃO: Foto Individual por Atividade

## 🎯 Problema Resolvido

**ANTES**: Quando você mudava a foto de uma atividade, todas as atividades da mesma instituição mudavam juntas (porque usavam `instituicao_foto`).

**AGORA**: Cada atividade tem sua **própria foto individual**, independente das outras atividades!

---

## 🔧 Como Funciona

### 1. **Sistema de Duas Fotos**

Cada atividade agora pode ter:
- **`atividade_foto`**: Foto específica desta atividade (armazenada localmente)
- **`instituicao_foto`**: Foto da instituição (vem da API - backup)

**Prioridade**: `atividade_foto` > `instituicao_foto` > ícone padrão

```kotlin
// No código dos cards:
val fotoUrl = atividade.atividade_foto ?: atividade.instituicao_foto

if (!fotoUrl.isNullOrEmpty()) {
    // Mostra foto
} else {
    // Mostra ícone padrão
}
```

---

## 📁 Arquivos Criados/Modificados

### ✅ Novos Arquivos:

1. **`AtividadeFotoDataStore.kt`** - Armazena fotos individuais por atividade
2. **`AtividadeViewModelFactory.kt`** - Factory para passar contexto ao ViewModel

### ✅ Arquivos Modificados:

1. **`Atividade.kt`** - Adicionado campo `atividade_foto`
2. **`AtividadeViewModel.kt`** - Carrega fotos salvas do DataStore
3. **`DetalhesAtividadeScreen.kt`** - Salva foto apenas da atividade atual
4. **`AtividadeCardAPI.kt`** - Prioriza `atividade_foto`
5. **`ResumoAtividadeCardAPI.kt`** - Prioriza `atividade_foto`
6. **`AtividadesScreen.kt`** - Usa ViewModelFactory com contexto

---

## 💾 Armazenamento

### DataStore (Local)
```kotlin
// Chave: "atividade_foto_{id}"
atividade_foto_11 = "https://storage.../foto1.jpg"
atividade_foto_12 = "https://storage.../foto2.jpg"
atividade_foto_13 = null // usa foto da instituição
```

### Como Funciona:
1. Foto enviada para Azure Blob Storage
2. URL salva no DataStore com chave `atividade_foto_{id}`
3. ViewModel carrega fotos ao buscar atividades
4. Cards mostram foto individual se existir

---

## 🎨 Fluxo de Upload

```
┌──────────────────────────────────────┐
│ Usuário abre Atividade "Natacao"    │
│ (ID: 11)                             │
└─────────────┬────────────────────────┘
              │
              ▼
┌──────────────────────────────────────┐
│ Clica no ícone 📷                    │
│ Seleciona uma foto                   │
└─────────────┬────────────────────────┘
              │
              ▼
┌──────────────────────────────────────┐
│ Upload para Azure                    │
│ URL: https://...foto_natacao.jpg     │
└─────────────┬────────────────────────┘
              │
              ▼
┌──────────────────────────────────────┐
│ Salva no DataStore                   │
│ Key: "atividade_foto_11"             │
│ Value: "https://...foto_natacao.jpg" │
└─────────────┬────────────────────────┘
              │
              ▼
┌──────────────────────────────────────┐
│ ✅ APENAS Atividade 11 muda!         │
│ Atividade 12 (Futebol): inalterada  │
│ Atividade 13 (Yoga): inalterada     │
└──────────────────────────────────────┘
```

---

## 📊 Exemplos de Uso

### Exemplo 1: Três Atividades, Três Fotos Diferentes

```
Instituição: "Academia Sport"
├── Atividade 11: Natação
│   └── atividade_foto: foto_piscina.jpg ✅
├── Atividade 12: Futebol  
│   └── atividade_foto: foto_campo.jpg ✅
└── Atividade 13: Yoga
    └── atividade_foto: null (usa foto da instituição)
```

### Exemplo 2: Uma Atividade Sem Foto

```
Atividade: "Natacao" (ID: 11)
├── atividade_foto: null
├── instituicao_foto: "foto_academia.jpg"
└── Resultado: Mostra foto_academia.jpg ✅
```

### Exemplo 3: Atividade Sem Nenhuma Foto

```
Atividade: "Natacao" (ID: 11)
├── atividade_foto: null
├── instituicao_foto: null
└── Resultado: Mostra ícone padrão ✅
```

---

## 🔍 Código-Chave

### 1. Modelo de Dados
```kotlin
// Atividade.kt
data class AtividadeResponse(
    val atividade_id: Int,
    val instituicao_foto: String?,
    // ...outros campos...
    var atividade_foto: String? = null  // ✅ NOVO!
)
```

### 2. DataStore
```kotlin
// AtividadeFotoDataStore.kt
suspend fun salvarFotoAtividade(atividadeId: Int, fotoUrl: String) {
    dataStore.edit { preferences ->
        preferences[atividadeFotoKey(atividadeId)] = fotoUrl
    }
}

suspend fun buscarFotoAtividade(atividadeId: Int): String? {
    return dataStore.data.map { preferences ->
        preferences[atividadeFotoKey(atividadeId)]
    }.first()
}
```

### 3. Upload (DetalhesAtividadeScreen)
```kotlin
// Salva foto APENAS desta atividade
atividadeFotoDataStore.salvarFotoAtividade(atividadeId, versionedUrl)
Log.d("DetalhesAtividade", "✅ Foto da atividade $atividadeId salva")

// Atualiza estado
atividade.atividade_foto = versionedUrl
```

### 4. ViewModel Carrega Fotos
```kotlin
// AtividadeViewModel.kt
suspend fun carregarFotosSalvas(atividades: List<AtividadeResponse>) {
    val fotoDataStore = AtividadeFotoDataStore(context!!)
    atividades.forEach { atividade ->
        val fotoSalva = fotoDataStore.buscarFotoAtividade(atividade.atividade_id)
        if (fotoSalva != null) {
            atividade.atividade_foto = fotoSalva
        }
    }
}
```

### 5. Cards Priorizam Foto Individual
```kotlin
// AtividadeCardAPI.kt e ResumoAtividadeCardAPI.kt
val fotoUrl = atividade.atividade_foto ?: atividade.instituicao_foto

if (!fotoUrl.isNullOrEmpty() && fotoUrl != "null") {
    AsyncImage(model = fotoUrl, ...)
} else {
    Image(painter = R.drawable.instituicao, ...)
}
```

---

## 🎯 Casos de Teste

### ✅ Teste 1: Adicionar Foto Individual
```
1. Abrir Atividade "Natacao" (ID: 11)
2. Clicar no ícone 📷
3. Selecionar foto de piscina
4. ✅ Apenas Natacao muda
5. ✅ Outras atividades inalteradas
```

### ✅ Teste 2: Trocar Foto Individual
```
1. Atividade "Natacao" já tem foto_piscina1.jpg
2. Clicar no ícone 📷
3. Selecionar foto_piscina2.jpg
4. ✅ foto_piscina2 substitui foto_piscina1
5. ✅ Outras atividades inalteradas
```

### ✅ Teste 3: Múltiplas Atividades, Cada Uma com Sua Foto
```
1. Atividade "Natacao": adicionar foto_piscina.jpg
2. Atividade "Futebol": adicionar foto_campo.jpg
3. Atividade "Yoga": adicionar foto_sala.jpg
4. ✅ Cada uma mostra sua própria foto
5. ✅ Nenhuma interfere nas outras
```

### ✅ Teste 4: Atividade Sem Foto Individual
```
1. Atividade "Dança" não tem foto individual
2. Instituição tem foto_academia.jpg
3. ✅ Dança mostra foto_academia.jpg (fallback)
```

### ✅ Teste 5: Reiniciar App
```
1. Adicionar foto para Atividade "Natacao"
2. Fechar app
3. Abrir app novamente
4. ✅ Foto de Natacao ainda lá (DataStore persistente)
```

---

## 🚀 Tecnologias Usadas

### DataStore
```kotlin
// Armazenamento local persistente
implementation "androidx.datastore:datastore-preferences:1.0.0"
```

### Azure Blob Storage
```kotlin
// Upload de imagens
AzureBlobRetrofit.uploadImageToAzure(...)
```

### Coil
```kotlin
// Carregamento de imagens
AsyncImage(model = fotoUrl, ...)
```

### ViewModel + Factory
```kotlin
// Gerenciamento de estado com contexto
AtividadeViewModelFactory(context)
```

---

## 📝 Logs de Debug

```bash
# Ver quando foto é salva
adb logcat | grep "AtividadeFoto.*salva"

# Ver quando foto é carregada
adb logcat | grep "AtividadeViewModel.*Foto carregada"

# Ver qual foto está sendo usada
adb logcat | grep "DetalhesAtividade"
```

**Exemplos de Logs:**
```
✅ Foto da atividade 11 salva: https://...
📷 Foto carregada para atividade 11: https://...
📤 Fazendo upload da foto da atividade ID: 11
```

---

## ⚙️ Arquitetura

```
┌─────────────────────────────────────────┐
│         DetalhesAtividadeScreen         │
│  (Usuário clica 📷, seleciona foto)     │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│          Azure Blob Storage             │
│   (Upload e retorna URL da imagem)      │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│       AtividadeFotoDataStore            │
│  (Salva: atividade_foto_11 = "url")    │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│         AtividadeViewModel              │
│  (Carrega fotos ao buscar atividades)  │
└────────────┬────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────┐
│    AtividadeCardAPI / ResumoCard        │
│  (Prioriza atividade_foto, mostra)     │
└─────────────────────────────────────────┘
```

---

## ✅ Checklist de Funcionalidades

- [x] Campo `atividade_foto` no modelo
- [x] DataStore para armazenar fotos por atividade
- [x] ViewModelFactory com contexto
- [x] Upload salva foto da atividade específica
- [x] ViewModel carrega fotos salvas
- [x] Cards priorizam `atividade_foto`
- [x] Fallback para `instituicao_foto`
- [x] Fallback para ícone padrão
- [x] Fotos persistem após reiniciar app
- [x] Cada atividade independente
- [x] Não interfere nas outras atividades
- [x] Logs para debug
- [x] Documentação completa

---

## 🎉 Resultado Final

### ✅ PROBLEMA RESOLVIDO!

**Antes:**
```
Mudo foto de Natacao → Todas atividades mudam ❌
```

**Agora:**
```
Mudo foto de Natacao → APENAS Natacao muda ✅
Futebol: Mantém sua foto ✅
Yoga: Mantém sua foto ✅
Dança: Mantém ícone padrão ✅
```

---

## 📚 Resumo Técnico

### O Que Foi Feito:
1. ✅ Adicionado campo `atividade_foto` opcional no modelo
2. ✅ Criado `AtividadeFotoDataStore` para persistência local
3. ✅ Criado `AtividadeViewModelFactory` para contexto
4. ✅ Modificado upload para salvar foto por atividade
5. ✅ ViewModel carrega fotos salvas automaticamente
6. ✅ Cards priorizam foto individual da atividade

### Por Que Funciona:
- Cada atividade tem ID único
- DataStore usa chave `atividade_foto_{id}`
- Cards verificam `atividade_foto` primeiro
- Fallback para `instituicao_foto` se null
- Sistema completamente independente

### Vantagens:
- ✅ Cada atividade pode ter foto única
- ✅ Não afeta outras atividades
- ✅ Persistente (sobrevive reinício)
- ✅ Fallback inteligente
- ✅ Fácil de manter
- ✅ Performance otimizada

---

**Status**: ✅ IMPLEMENTADO E FUNCIONANDO
**Data**: 11/11/2025
**Versão**: Final

**🎊 Agora cada atividade tem sua própria foto individual! 🎊**

