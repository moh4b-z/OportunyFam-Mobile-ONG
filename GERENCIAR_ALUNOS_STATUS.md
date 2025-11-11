# Sistema de Gerenciamento de Alunos - Status das Inscrições

## ✅ Funcionalidades Implementadas

### 1. Tela de Gerenciamento de Alunos (`GerenciarAlunosScreen.kt`)

#### Características:
- ✅ Lista de alunos inscritos em uma atividade específica
- ✅ Pesquisa de alunos por nome
- ✅ Foto de perfil do aluno (com fallback para ícone padrão)
- ✅ Dropdown para alterar status da inscrição
- ✅ Botão para remover aluno
- ✅ Feedback visual com Snackbar
- ✅ Estados de carregamento, sucesso e erro

### 2. Status de Inscrição Disponíveis

Conforme enum `StatusInscricao.kt`:

| ID | Nome | Cor | Descrição |
|----|------|-----|-----------|
| 2 | Cancelada | Cinza | Inscrição cancelada |
| 3 | Pendente | Laranja | Aguardando aprovação |
| 4 | Aprovada | Verde | Aluno aprovado na atividade |
| 5 | Negada | Vermelho | Inscrição negada |

**Nota**: ID 1 (Sugerida Pela Criança) foi removido conforme solicitado.

### 3. Fluxo de Atualização de Status

```
Usuário clica no dropdown de status
    ↓
Seleciona novo status (ex: "Aprovada")
    ↓
GerenciarAlunosScreen chama: onStatusChange(statusId = 4)
    ↓
InscricaoViewModel.atualizarStatusInscricao(inscricaoId, 4, atividadeId)
    ↓
PUT /v1/oportunyfam/inscricoes/{inscricaoId}
Body: {"id_status": 4}
    ↓
API retorna 200 OK com inscrição atualizada
    ↓
ViewModel recarrega lista de inscrições
    ↓
Snackbar mostra: "Status atualizado com sucesso!"
    ↓
Lista atualizada mostra novo status
```

### 4. Endpoints da API Utilizados

#### Buscar Inscrições por Atividade
```
GET /v1/oportunyfam/inscricoes/atividade/{idAtividade}
```
**Fallback** (se 404): `GET /v1/oportunyfam/inscricoes` + filtro local

#### Atualizar Status da Inscrição
```
PUT /v1/oportunyfam/inscricoes/{id}
Content-Type: application/json

{
  "id_status": 4  // ID do novo status
}
```

**Response**:
```json
{
  "status": true,
  "status_code": 200,
  "messagem": "Item atualizado",
  "inscricao": {
    "id": 1,
    "id_crianca": 1,
    "id_atividade": 11,
    "id_responsavel": 1,
    "id_status": 4,
    "observacao": "Aceitamos",
    "criado_em": "2025-11-11T13:50:16.000Z",
    "atualizado_em": "2025-11-11T14:21:17.000Z"
  }
}
```

#### Remover Aluno
```
DELETE /v1/oportunyfam/inscricoes/{id}
```

### 5. Logs do Sistema

O sistema possui logs detalhados para debug:

```kotlin
// Logs na GerenciarAlunosScreen
Log.e("GerenciarAlunos", "🎯 DROPDOWN CLICADO!")
Log.e("GerenciarAlunos", "Status selecionado: ID=$statusId, Nome=$statusNome")

// Logs no InscricaoViewModel
Log.d("InscricaoViewModel", "✏️ Atualizando inscrição ID: $inscricaoId para status: $novoStatus")
Log.d("InscricaoViewModel", "✅ Status atualizado com sucesso")
```

### 6. Componentes do Sistema

#### Models:
- `InscricaoDetalhada` - Dados completos da inscrição
- `InscricaoUpdateRequest` - Request para atualizar status
- `StatusInscricao` - Enum com status disponíveis

#### ViewModels:
- `InscricaoViewModel` - Gerencia inscrições e atualizações

#### Screens:
- `GerenciarAlunosScreen` - Tela principal de gerenciamento
- `AlunoCard` - Card individual de cada aluno

#### States:
- `InscricoesState` - Loading, Success, Error
- `AtualizarInscricaoState` - Idle, Loading, Success, Error

## 📋 Como Usar

### Para ONG/Instituição:

1. **Acessar Gerenciamento:**
   - Vá para Detalhes da Atividade
   - Clique em "Gerenciar Alunos"

2. **Visualizar Alunos Inscritos:**
   - Lista mostra foto, nome e status atual
   - Use barra de pesquisa para filtrar

3. **Alterar Status:**
   - Clique no dropdown de status
   - Selecione novo status (Aprovada, Negada, Cancelada, Pendente)
   - Sistema atualiza automaticamente
   - Feedback via Snackbar

4. **Remover Aluno:**
   - Clique no ícone de lixeira
   - Confirme remoção
   - Aluno é removido da lista

## 🔧 Tratamento de Erros

### Endpoint Não Existe (404)
```kotlin
// Sistema usa fallback automático
response.code() == 404 -> {
    Log.w("InscricaoViewModel", "⚠️ Endpoint /atividade/:id não existe, usando fallback")
    buscarTodasEFiltrar(atividadeId)
}
```

### Erro de Conexão
```kotlin
override fun onFailure(call: Call<InscricoesResponse>, t: Throwable) {
    Log.e("InscricaoViewModel", "❌ Falha na conexão, tentando fallback", t)
    buscarTodasEFiltrar(atividadeId)
}
```

### Rate Limit (429)
- Sistema mostra mensagem apropriada
- Tenta novamente automaticamente após delay

## 📊 Exemplo de Dados da API

### Response: Buscar Todas as Inscrições
```json
{
  "status": true,
  "status_code": 200,
  "messagem": "Requisição feita com sucesso",
  "inscricoes": [
    {
      "inscricao_id": 1,
      "instituicao_id": 12,
      "instituicao_nome": "Bruno",
      "atividade_id": 11,
      "atividade_titulo": "Futebol",
      "crianca_id": 1,
      "crianca_nome": "Maria Oliveira",
      "crianca_foto": "https://meuservidor.com/imagens/maria_oliveira.png",
      "status_id": 4,
      "status_inscricao": "Aprovada",
      "data_inscricao": "2025-11-11T13:50:16.000Z",
      "observacao": "Aceitamos",
      "id_responsavel": 1
    }
  ]
}
```

## 🎨 Interface Visual

### Card do Aluno
```
┌──────────────────────────────────────┐
│  📷 [Foto]  Maria Oliveira           │
│             Observação: "Aceitamos"  │
│                                  🗑️  │
│                                      │
│  Status da Inscrição: ▼              │
│  🟢 Aprovada                         │
│     [Dropdown com opções]            │
└──────────────────────────────────────┘
```

### Dropdown de Status
```
Aprovada      🟢 (atual - não aparece)
Negada        🔴
Cancelada     ⚫
Pendente      🟠
```

## ✅ Checklist de Funcionalidades

- [x] Listar alunos inscritos por atividade
- [x] Buscar aluno por nome
- [x] Exibir foto do aluno
- [x] Dropdown para alterar status
- [x] Cores visuais por status
- [x] Atualização via API (PUT)
- [x] Recarregamento automático após atualização
- [x] Feedback visual (Snackbar)
- [x] Remover aluno (DELETE)
- [x] Tratamento de erros
- [x] Fallback quando endpoint não existe
- [x] Logs detalhados para debug

## 🚀 Status Atual

✅ **SISTEMA FUNCIONANDO CORRETAMENTE**

Baseado nos logs fornecidos:
- Inscrições sendo buscadas com sucesso
- Status sendo atualizado na API (HTTP 200)
- Fallback funcionando quando necessário
- Lista sendo recarregada após atualizações

## 📝 Notas Importantes

1. **ID do Status**: Sempre use o ID numérico (2, 3, 4, 5), não o nome
2. **Fallback**: Sistema busca todas as inscrições e filtra localmente se endpoint específico não existir
3. **Recarregamento**: Lista é recarregada automaticamente após qualquer alteração
4. **Validação**: Status atual não aparece no dropdown (evita atualização desnecessária)
5. **Observação**: Campo opcional, pode estar vazio

## 🔍 Debug

Para verificar se o sistema está funcionando, observe os logs:

```bash
# Busca de inscrições
adb logcat | grep "InscricaoViewModel.*Buscando"

# Atualização de status
adb logcat | grep "InscricaoViewModel.*Atualizando"

# Dropdown clicado
adb logcat | grep "GerenciarAlunos.*DROPDOWN"

# Response da API
adb logcat | grep "okhttp.OkHttpClient"
```

## 🎯 Conclusão

O sistema de gerenciamento de alunos está completamente funcional:
- ✅ Interface intuitiva com dropdown de status
- ✅ Integração completa com API
- ✅ Tratamento robusto de erros
- ✅ Feedback visual adequado
- ✅ Logs detalhados para debug

**Pronto para uso em produção!** 🚀

