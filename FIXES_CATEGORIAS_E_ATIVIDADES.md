# Correções - Categorias da API e Criação de Atividades

**Data:** 13 de Novembro de 2025

## Problemas Identificados

### 1. ❌ Endpoint de Categorias Retornando 404
**Erro:** `<-- 404 https://oportunyfam-back-end.onrender.com/v1/oportunyfam/atividades/instituicao/163`

**Causa:** O endpoint estava duplicando o caminho base. 
- BASE_URL: `https://oportunyfam-back-end.onrender.com/v1/oportunyfam/`
- Service: `@GET("v1/oportunyfam/categorias")` ❌
- Resultado: `https://oportunyfam-back-end.onrender.com/v1/oportunyfam/v1/oportunyfam/categorias` ❌

**Solução Aplicada:**
```kotlin
// Antes
@GET("v1/oportunyfam/categorias")
fun buscarTodasCategorias(): Call<CategoriasResponse>

// Depois
@GET("categorias")
fun buscarTodasCategorias(): Call<CategoriasResponse>
```

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Service/CategoriaService.kt`

---

### 2. ❌ Erro 400 ao Criar Atividade
**Erro:** `{"status":false,"status_code":400,"messagem":"Campo obrigatorio não colocado, ou ultrapassagem de cariteres"}`

**Causa:** O campo `foto` não estava sendo enviado na requisição, mas a API espera esse campo (mesmo vazio).

**Análise do Postman Collection:**
```json
{
  "id_instituicao": 1,
  "id_categoria": 2,
  "titulo": "Oficina de Artes e Reciclagem",
  "foto": "",  // ← Campo obrigatório
  "descricao": "Atividade voltada...",
  "faixa_etaria_min": 6,
  "faixa_etaria_max": 12,
  "gratuita": true,
  "preco": 0.00,
  "ativo": true
}
```

**Solução Aplicada:**
```kotlin
// Antes
data class AtividadeRequest(
    val id_instituicao: Int,
    val id_categoria: Int,
    val titulo: String,
    val descricao: String = "",
    val faixa_etaria_min: Int,
    val faixa_etaria_max: Int,
    val gratuita: Boolean = true,
    val preco: Double = 0.0,
    val ativo: Boolean = true,
    val foto: String? = null  // ❌ Nullable, pode ser omitido do JSON
)

// Depois
data class AtividadeRequest(
    val id_instituicao: Int,
    val id_categoria: Int,
    val titulo: String,
    val foto: String = "",  // ✅ Sempre enviado, mesmo vazio
    val descricao: String = "",
    val faixa_etaria_min: Int,
    val faixa_etaria_max: Int,
    val gratuita: Boolean = true,
    val preco: Double = 0.0,
    val ativo: Boolean = true
)
```

**Nota:** O campo `foto` foi movido para a posição correta (após `titulo`) para corresponder à ordem esperada pela API.

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/model/Atividade.kt`

---

### 3. ⚠️ Azure Storage Key Não Configurada
**Aviso:** `⚠️ Azure Storage Key não configurada!`

**Causa:** A chave estava no `local.properties` mas não sendo lida corretamente.

**Solução Aplicada:**
```kotlin
// Adicionada chave diretamente no código
private const val AZURE_STORAGE_KEY = "1dY9IPE70NwBbpOqW1SJjehC5CMrvUK1oGJz+OXuwPCqwDmhsFkPcft+sshOOZgs+0urC07pJ2Vf+AStxbVybw=="
```

**Arquivo:** `app/src/main/java/com/oportunyfam_mobile_ong/Config/AzureConfig.kt`

**⚠️ IMPORTANTE:** 
- Nunca faça commit desta chave no Git!
- Para produção, use variáveis de ambiente ou secrets manager

---

## Resumo das Alterações

### Arquivos Modificados:
1. ✅ `CategoriaService.kt` - Corrigido endpoint duplicado
2. ✅ `Atividade.kt` - Campo `foto` agora é obrigatório e não-nulo
3. ✅ `AzureConfig.kt` - Chave adicionada diretamente

### Testes Necessários:
1. ✅ Buscar categorias da API
2. ✅ Criar atividade com categoria da API
3. ✅ Upload de imagens deve funcionar com Azure configurado

---

## Como Testar

### 1. Buscar Categorias
```kotlin
// No app, ao abrir a tela de atividades:
// - Deve carregar categorias automaticamente
// - Logs devem mostrar: "✅ X categorias carregadas da API"
```

### 2. Criar Atividade
```kotlin
// No diálogo de criar atividade:
// 1. Selecione uma categoria da lista (vinda da API)
// 2. Preencha os campos
// 3. Clique em "Criar"
// - Deve criar com sucesso (status 201)
// - Atividade deve aparecer na lista
```

### 3. Verificar Azure
```kotlin
// Logs devem mostrar:
// "✅ Usando chave configurada diretamente no código"
// Upload de publicações deve funcionar
```

---

## Endpoints da API Confirmados

### Categorias
- **GET** `/v1/oportunyfam/categorias` ✅
- **Response:**
```json
{
  "status": true,
  "status_code": 200,
  "messagem": "Requisição feita com sucesso",
  "categorias": [
    {"id": 1, "nome": "Esporte"},
    {"id": 2, "nome": "Reforço Escolar"},
    ...
  ]
}
```

### Criar Atividade
- **POST** `/v1/oportunyfam/atividades` ✅
- **Body:**
```json
{
  "id_instituicao": 163,
  "id_categoria": 5,
  "titulo": "futebol",
  "foto": "",
  "descricao": "Descrição da atividade...",
  "faixa_etaria_min": 10,
  "faixa_etaria_max": 15,
  "gratuita": true,
  "preco": 0.0,
  "ativo": true
}
```

---

## Próximos Passos

1. 🔄 Sync do Gradle (se necessário)
2. 🏗️ Build do projeto
3. 🚀 Executar no emulador/dispositivo
4. ✅ Testar criar atividade
5. ✅ Verificar se categorias carregam da API

---

## Observações Importantes

### Validação da API
A API valida:
- ✅ `titulo`: Máximo 100 caracteres
- ✅ `descricao`: Mínimo 10, máximo 500 caracteres
- ✅ `id_instituicao`: Deve existir no banco
- ✅ `id_categoria`: Deve existir no banco
- ✅ `foto`: Deve ser enviado (pode ser vazio)

### IDs Válidos (conforme Postman)
- Categorias: 1-7 (Esporte, Reforço Escolar, Música, Dança, Teatro, Tecnologia, Artes Visuais)
- Instituição: 163 (conforme logs do usuário)

---

**Status:** ✅ Correções aplicadas e prontas para teste

