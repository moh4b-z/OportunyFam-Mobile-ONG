# Solução Final - Erro 400 ao Criar Atividade

## 🎯 Problema Identificado

O erro 400 "Campo obrigatorio não colocado, ou ultrapassagem de cariteres" **NÃO** é sobre campos faltando ou limites de caracteres.

### Causa Raiz: **FOREIGN KEY CONSTRAINT**
- **Instituição ID: 163** - Provavelmente não existe no banco
- **Categoria ID: 5** - Pode não existir ou estar inativa

## ✅ Correções Implementadas

### 1. Logging Aprimorado (`AtividadeViewModel.kt`)

Agora o app mostra avisos claros quando tenta criar atividade:

```
⚠️ ATENÇÃO: Verifique se estes IDs existem no banco:
   - Instituição ID: 163
   - Categoria ID: 5
   Se o erro 400 persistir, esses IDs podem não existir!
```

E quando recebe erro 400:
```
❌ Erro 400: {...}
💡 Possíveis causas:
   1. Instituição ID 163 não existe
   2. Categoria ID 5 não existe
   3. Validação de campo falhou no backend
```

### 2. Mensagem de Erro Melhorada

A mensagem mostrada ao usuário agora é mais útil:
```
"Erro: Verifique se a instituição e categoria existem no sistema. 
 IDs: instituição=163, categoria=5"
```

### 3. Comentários de Alerta no Código

Adicionado em `CriarAtividadeDialog.kt`:
```kotlin
// ⚠️ ATENÇÃO: Estes IDs devem corresponder aos IDs reais no banco de dados!
// Se criar atividade retornar erro 400, verifique se a categoria existe.
// TODO: Buscar categorias da API: GET /v1/oportunyfam/categorias
```

## 🧪 Como Resolver AGORA

### Opção 1: Usar IDs Conhecidos (Teste Rápido)

Modifique temporariamente a criação para usar IDs que sabemos que funcionam:

**No arquivo:** `ListaAtividadesScreen.kt` (linha ~244)

```kotlin
val request = com.oportunyfam_mobile_ong.model.AtividadeRequest(
    id_instituicao = 1,     // ← MUDE de 163 para 1 (temporário)
    id_categoria = 2,        // ← MUDE de 5 para 2 (Artes)
    titulo = titulo,
    descricao = descricao.ifEmpty { "" },
    faixa_etaria_min = faixaMin,
    faixa_etaria_max = faixaMax,
    gratuita = gratuita,
    preco = preco,
    ativo = true
)
```

### Opção 2: Verificar IDs Válidos na API

Use Postman ou navegador para verificar:

**Instituições:**
```
GET https://oportunyfam-back-end.onrender.com/v1/oportunyfam/instituicoes
```

**Categorias:**
```
GET https://oportunyfam-back-end.onrender.com/v1/oportunyfam/categorias
```

Depois atualize as categorias em `CriarAtividadeDialog.kt` com os IDs reais.

### Opção 3: Buscar Categorias Dinamicamente (Solução Ideal)

Implemente um serviço para buscar categorias da API:

1. Criar `CategoriaService.kt`:
```kotlin
interface CategoriaService {
    @GET("v1/oportunyfam/categorias")
    fun buscarTodasCategorias(): Call<CategoriasResponse>
}
```

2. Atualizar `CriarAtividadeDialog` para receber categorias como parâmetro

3. No ViewModel, buscar categorias antes de mostrar o diálogo

## 📊 Dados de Teste que Funcionam

Baseado no Postman, use estes valores para teste:

```kotlin
titulo = "Oficina de Artes e Reciclagem"
descricao = "Atividade voltada para crianças aprenderem técnicas de arte com materiais recicláveis"
id_instituicao = 1
id_categoria = 2
faixa_etaria_min = 6
faixa_etaria_max = 12
gratuita = true
preco = 0.0
ativo = true
```

## 🔍 Como Identificar o Problema

Execute o app e veja os logs filtrados:
```bash
adb logcat | grep "AtividadeViewModel"
```

Procure por:
```
⚠️ ATENÇÃO: Verifique se estes IDs existem no banco
💡 Possíveis causas:
```

## 📝 Resumo das Validações Atuais

### ✅ Implementado:
- [x] Título: máximo 100 caracteres
- [x] Descrição: mínimo 10, máximo 500 caracteres
- [x] Faixa etária: min <= max, entre 0 e 99
- [x] Preço: obrigatório quando não gratuita
- [x] Logging detalhado de erros
- [x] Mensagens de erro específicas para erro 400

### ⚠️ Faltando (TODO):
- [ ] Validar se instituição existe antes de criar
- [ ] Buscar categorias da API ao invés de usar lista fixa
- [ ] Cache de categorias válidas
- [ ] Mensagem de erro específica do backend

## 🎬 Próximos Passos

1. **Imediato:** Teste com `id_instituicao = 1` e `id_categoria = 2`
2. **Curto Prazo:** Verifique quais IDs existem no banco via API
3. **Médio Prazo:** Implemente busca dinâmica de categorias
4. **Longo Prazo:** Solicite ao backend mensagens de erro mais específicas

## 📚 Documentos Relacionados

- `FIXES_CRIAR_ATIVIDADE.md` - Todas as correções de validação
- `ANALISE_ERRO_400_FINAL.md` - Análise detalhada do erro
- `README.md` - Documentação geral do projeto

---

**Última Atualização:** 13 de Novembro de 2025  
**Status:** ✅ Correções implementadas, aguardando teste com IDs válidos

