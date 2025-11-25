# Implementação: Conversa com Alunos da Instituição

## Resumo
Implementada funcionalidade para listar todos os alunos da instituição e iniciar conversas com eles através do botão FAB (Floating Action Button) na tela de conversas.

## Alterações Realizadas

### 1. ChatViewModel.kt
**Localização:** `app/src/main/java/com/oportunyfam_mobile_ong/viewmodel/ChatViewModel.kt`

#### Adicionado:
- **Service:** `InstituicaoService` e `CriancaService` para buscar alunos e detalhes das crianças
- **State Flows:**
  - `_instituicaoId` e `instituicaoId` - ID da instituição logada
  - `_alunos` e `alunos` - Lista de alunos da instituição
  - `_isLoadingAlunos` e `isLoadingAlunos` - Estado de carregamento dos alunos

#### Novas Funções:
1. **`carregarAlunos()`**
   - Busca todos os alunos da instituição usando endpoint `/v1/oportunyfam/instituicoes/alunos/`
   - Filtra parâmetros: `instituicao_id` (obrigatório), `atividade_id` e `status_id` (opcionais)
   - Remove alunos duplicados agrupando por `crianca_id`
   - Atualiza o StateFlow `_alunos`

2. **`criarOuBuscarConversa(criancaIdOrPessoaId: Int)`**
   - Recebe o `crianca_id` do aluno selecionado
   - Busca detalhes da criança via `/v1/oportunyfam/criancas/{id}` para obter o `pessoa_id`
   - Verifica se já existe uma conversa com esse aluno
   - Se existir, retorna o ID da conversa existente
   - Se não existir, cria nova conversa via POST `/v1/oportunyfam/conversas` com `participantes: [instituicao_pessoa_id, aluno_pessoa_id]`
   - Recarrega lista de conversas após criar nova conversa
   - Retorna o ID da conversa (nova ou existente)

### 2. ConversasScreen.kt
**Localização:** `app/src/main/java/com/oportunyfam_mobile_ong/Screens/ConversasScreen.kt`

#### Adicionado:
- **Imports:** `Aluno`, `kotlinx.coroutines.launch`
- **State:** `showAlunosDialog` para controlar exibição do diálogo
- **StateFlow Observers:** `alunos`, `isLoadingAlunos`
- **CoroutineScope:** Para executar operações assíncronas

#### Modificações:
1. **FloatingActionButton**
   - onClick agora chama `viewModel.carregarAlunos()` e abre o diálogo
   ```kotlin
   onClick = { 
       viewModel.carregarAlunos()
       showAlunosDialog = true 
   }
   ```

2. **Novo Componente: AlunosDialog**
   - Diálogo modal que exibe lista de alunos
   - Mostra loading enquanto carrega
   - Mostra mensagem quando não há alunos
   - Lista alunos em cards clicáveis
   - Ao clicar em um aluno:
     - Fecha o diálogo
     - Chama `viewModel.criarOuBuscarConversa(aluno.crianca_id)`
     - Navega para ChatScreen com o conversaId retornado

3. **Novo Componente: AlunoItem**
   - Card individual para cada aluno
   - Mostra:
     - Avatar com inicial do nome
     - Nome do aluno
     - Nome da atividade matriculada
     - Status da inscrição (Aprovada/Pendente) com cores diferenciadas
   - Totalmente clicável

## Fluxo de Uso

1. **Usuário na tela de Conversas**
2. **Clica no botão FAB (+)**
3. **Sistema carrega lista de alunos da instituição**
4. **Diálogo é exibido com todos os alunos**
5. **Usuário seleciona um aluno**
6. **Sistema:**
   - Busca o `pessoa_id` da criança
   - Verifica se já existe conversa
   - Se não existe, cria nova conversa
7. **Navega para tela de Chat com o aluno**

## API Endpoints Utilizados

### GET `/v1/oportunyfam/instituicoes/alunos/`
**Query Params:**
- `instituicao_id` (obrigatório) - ID da instituição
- `atividade_id` (opcional) - Filtrar por atividade
- `status_id` (opcional) - Filtrar por status

**Response:**
```json
{
  "status": true,
  "alunos": [
    {
      "crianca_id": 1,
      "crianca_nome": "Maria Oliveira",
      "crianca_foto": "url",
      "atividade_titulo": "Oficina de Artes",
      "status_inscricao": "Aprovada",
      ...
    }
  ]
}
```

### GET `/v1/oportunyfam/criancas/{id}`
**Busca detalhes da criança para obter pessoa_id**

**Response:**
```json
{
  "crianca": {
    "crianca_id": 1,
    "pessoa_id": 2,
    "nome": "Maria Oliveira",
    ...
  }
}
```

### POST `/v1/oportunyfam/conversas`
**Body:**
```json
{
  "participantes": [1, 2]
}
```

**Response:**
```json
{
  "conversa": {
    "id": 5,
    "participantes": [...]
  }
}
```

## Tratamento de Erros

1. **Sem alunos:** Mostra mensagem "Nenhum aluno encontrado"
2. **Erro ao carregar:** Exibe mensagem de erro no dialog
3. **Erro ao criar conversa:** Mostra mensagem via `_errorMessage` no ViewModel
4. **Sem conexão:** Mantém diálogo aberto com erro

## Design

- **Cores:** Paleta laranja (#FF6F00, #FFA726) seguindo o tema do app
- **Cards:** Arredondados (20dp radius) com elevação sutil
- **Loading:** CircularProgressIndicator laranja com texto
- **Avatar:** Círculo laranja com inicial do nome em branco
- **Status:** Verde (Aprovada), Laranja (Pendente), Cinza (outros)

## Observações

- A lista de alunos é carregada toda vez que o botão FAB é clicado
- Alunos duplicados (mesma criança em várias atividades) são filtrados
- Se o aluno já tem conversa existente, navega direto sem criar nova
- O `pessoa_id` é obtido automaticamente pelo ViewModel via API de criancas

## Próximas Melhorias Sugeridas

1. Cache da lista de alunos para evitar chamadas repetidas
2. Busca/filtro de alunos no diálogo
3. Paginação para instituições com muitos alunos
4. Mostrar foto do aluno (se disponível) em vez de avatar com inicial
5. Indicar visualmente quais alunos já têm conversa ativa

