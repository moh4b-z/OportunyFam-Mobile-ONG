# 📅 Sistema de Calendário de Aulas - Documentação Completa

## ✅ Implementação Concluída

Foi implementado um sistema completo de gerenciamento de aulas com calendário nativo do Android na tela "Gerenciar Aulas".

---

## 🎯 Funcionalidades Implementadas

### 1️⃣ **Calendário Nativo Android**
- ✅ Usa o componente `CalendarView` nativo do Android
- ✅ Permite selecionar múltiplas datas tocando em cada uma
- ✅ Não permite selecionar datas passadas (minDate = hoje)
- ✅ Feedback visual das datas selecionadas
- ✅ Sistema toggle: toque novamente para desmarcar

### 2️⃣ **Criação de Aulas**

#### **Aula Individual**
- Selecione 1 data
- Define horário de início e fim
- Define número de vagas
- API: `POST /atividades/aulas`

#### **Aulas em Lote**
- Selecione múltiplas datas
- Mesmo horário para todas
- Mesmo número de vagas
- API: `POST /atividades/aulas/lote`

### 3️⃣ **Seleção de Horários**
- ✅ TimePickerDialog nativo do Android
- ✅ Formato 24 horas
- ✅ Horário de início e fim separados
- ✅ Validação automática

### 4️⃣ **Visualização de Aulas**
- ✅ Lista todas as aulas da atividade
- ✅ Carrega dados da API
- ✅ Mostra status da aula (Futura, Hoje, Encerrada)
- ✅ Exibe vagas disponíveis
- ✅ Cards com design diferenciado por status

---

## 📁 Arquivos Criados/Modificados

### ✨ Novos Arquivos

1. **`AulaViewModel.kt`**
   - ViewModel para gerenciar estado das aulas
   - Métodos: buscarAulasPorAtividade, criarAula, criarAulasLote, deletarAula
   - Estados: AulasState, CriarAulaState

2. **`CriarAulaDialog.kt`**
   - Diálogo modal com calendário nativo
   - Seleção múltipla de datas
   - Pickers de horário nativos
   - Input de vagas totais

### 🔄 Arquivos Modificados

3. **`CalendarioAulasScreen.kt`**
   - Integração com AulaViewModel
   - Exibição de aulas por atividade
   - FAB para criar novas aulas
   - Snackbar para feedback
   - Recarregamento automático após criar aulas

4. **`Aulas.kt`** (modelo)
   - Adicionado método `toAulaDetalhe()` para conversão
   - Removida redeclaração de Participante

---

## 🔌 Integração com API

### Endpoints Utilizados

```kotlin
// Buscar aulas por instituição (filtradas por atividade no app)
GET /atividades/aulas/instituicao/:idInstituicao

// Criar uma aula individual
POST /atividades/aulas
Body: {
  "id_atividade": Int,
  "data_aula": "YYYY-MM-DD",
  "hora_inicio": "HH:MM:SS",
  "hora_fim": "HH:MM:SS",
  "vagas_total": Int,
  "vagas_disponiveis": Int,
  "ativo": Boolean
}

// Criar múltiplas aulas de uma vez
POST /atividades/aulas/lote
Body: {
  "id_atividade": Int,
  "hora_inicio": "HH:MM:SS",
  "hora_fim": "HH:MM:SS",
  "vagas_total": Int,
  "datas": ["YYYY-MM-DD", "YYYY-MM-DD", ...]
}

// Deletar uma aula
DELETE /atividades/aulas/:id
```

---

## 🎨 Interface do Usuário

### Tela de Calendário de Aulas

```
┌─────────────────────────────────────┐
│  ← Calendário de Aulas             │
├─────────────────────────────────────┤
│                                     │
│  Nome da Atividade                  │
│                                     │
│  Aulas Cadastradas (5)              │
│  ┌─────────────────────────────┐   │
│  │ 15/11/2025  09:00 - 10:00  │   │
│  │ 10/20 vagas disponíveis    │   │
│  └─────────────────────────────┘   │
│  ┌─────────────────────────────┐   │
│  │ 18/11/2025  09:00 - 10:00  │   │
│  │ 10/20 vagas disponíveis    │   │
│  └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
                                   [+]
```

### Diálogo de Criar Aulas

```
┌─────────────────────────────────────┐
│  Cadastrar Aulas              [X]   │
├─────────────────────────────────────┤
│                                     │
│  Selecione as datas das aulas:      │
│  Toque em cada data que deseja      │
│                                     │
│  ┌───────────────────────────┐     │
│  │     CALENDÁRIO NATIVO     │     │
│  │                           │     │
│  │   Novembro 2025           │     │
│  │  D  S  T  Q  Q  S  S      │     │
│  │     1  2  3  4  5  6  7   │     │
│  │  8  9 10 11 12 13 14      │     │
│  │ 15⭕16 17 18⭕19 20 21     │     │
│  └───────────────────────────┘     │
│                                     │
│  Datas selecionadas (2):            │
│  • 15/11/2025                       │
│  • 18/11/2025                       │
│                                     │
│  Horários:                          │
│  [🕐 09:00] até [🕐 10:00]         │
│                                     │
│  Vagas totais:                      │
│  [10                  ]             │
│                                     │
│  [Cancelar]  [Criar 2 Aulas]       │
└─────────────────────────────────────┘
```

---

## 🚀 Como Usar

### Para o Usuário Final

1. **Abrir Calendário de Aulas**
   - Na tela de detalhes da atividade
   - Clicar em "Ver Calendário"

2. **Criar Nova Aula**
   - Clicar no botão flutuante `+` (laranja)
   - Selecionar data(s) no calendário
   - Definir horários de início e fim
   - Definir número de vagas
   - Clicar em "Criar Aula" ou "Criar X Aulas"

3. **Visualizar Aulas**
   - Lista automática após criação
   - Cards coloridos por status
   - Informações de data, horário e vagas

---

## 🔄 Fluxo de Dados

```
┌─────────────────────┐
│ CalendarioAulasScreen│
│    (UI Layer)       │
└──────────┬──────────┘
           │
           ↓
    ┌─────────────┐
    │ AulaViewModel│
    └──────┬───────┘
           │
           ↓
   ┌──────────────────┐
   │ AtividadeService │
   │   (Retrofit)     │
   └────────┬─────────┘
            │
            ↓
   ┌────────────────┐
   │   API Backend  │
   │   /atividades  │
   └────────────────┘
```

---

## ✅ Testes Realizados

- ✅ Compilação bem-sucedida
- ✅ Calendário nativo carrega corretamente
- ✅ Seleção múltipla de datas funciona
- ✅ TimePickerDialog abre e salva horários
- ✅ Criação de aula individual
- ✅ Criação de aulas em lote
- ✅ Listagem de aulas por atividade
- ✅ Recarregamento após criação

---

## 🎨 Design Patterns Utilizados

1. **MVVM (Model-View-ViewModel)**
   - Separação clara de responsabilidades
   - ViewModel gerencia estado e lógica de negócio
   - View (Screen) apenas exibe dados

2. **State Management com StateFlow**
   - Estados reativos
   - UI atualiza automaticamente

3. **Repository Pattern (via Retrofit)**
   - Acesso à API centralizado
   - Callbacks assíncronos

4. **Compose Navigation**
   - Navegação declarativa
   - Estados preservados

---

## 📱 Componentes Android Nativos Utilizados

1. **CalendarView**
   - Componente nativo do Android
   - Exibição de calendário mês a mês
   - Listener de seleção de datas

2. **TimePickerDialog**
   - Diálogo nativo para seleção de hora
   - Formato 24h
   - Validação automática

3. **AndroidView (Compose)**
   - Integração de views Android tradicionais no Compose
   - Permite usar CalendarView no Jetpack Compose

---

## 🔮 Melhorias Futuras (Opcionais)

1. **Edição de Aulas**
   - Permitir editar horários e vagas
   - PUT /atividades/aulas/:id

2. **Exclusão de Aulas**
   - Swipe para deletar
   - DELETE /atividades/aulas/:id

3. **Filtros e Busca**
   - Filtrar por período
   - Buscar por data específica

4. **Visualização de Participantes**
   - Ver quem está inscrito em cada aula
   - Lista de presença

5. **Recorrência**
   - Criar aulas recorrentes (toda semana)
   - Padrões de repetição

6. **Notificações**
   - Lembrete de aula próxima
   - Push notifications

---

## 🐛 Troubleshooting

### Problema: Aulas não aparecem
**Solução**: Verificar se a instituição está logada e se a API retorna dados

### Problema: Calendário não abre
**Solução**: Verificar permissões do AndroidManifest

### Problema: Horários salvam incorretamente
**Solução**: Verificar formato HH:MM:SS no backend

---

## 📄 Resumo Técnico

| Aspecto | Detalhes |
|---------|----------|
| **ViewModel** | AulaViewModel.kt |
| **UI Component** | CriarAulaDialog.kt |
| **Screen** | CalendarioAulasScreen.kt |
| **API Service** | AtividadeService.kt |
| **Modelos** | AulaRequest, AulaLoteRequest, AulaDetalhada |
| **Calendário** | CalendarView (Android nativo) |
| **Time Picker** | TimePickerDialog (Android nativo) |
| **Arquitetura** | MVVM + Compose |
| **Status** | ✅ Implementado e Testado |

---

**Desenvolvido com ❤️ usando Jetpack Compose e Android Native Components**

*Build Status: ✅ SUCCESS*

