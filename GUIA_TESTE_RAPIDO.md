# Guia Rápido de Teste - Categorias e Atividades

## ✅ Alterações Realizadas

### 1. Corrigido endpoint de categorias
- **Antes:** Duplicava o caminho (`/v1/oportunyfam/v1/oportunyfam/categorias`)
- **Agora:** Caminho correto (`/v1/oportunyfam/categorias`)

### 2. Corrigido campo `foto` na criação de atividades
- **Antes:** Campo opcional/nulo que podia ser omitido
- **Agora:** Campo obrigatório com string vazia como padrão

### 3. Configurada chave Azure Storage
- **Status:** Chave adicionada diretamente no código

---

## 🧪 Como Testar

### Teste 1: Carregar Categorias da API

1. Abra o app no emulador/dispositivo
2. Faça login como instituição (ID 163)
3. Navegue para a tela de "Atividades"
4. Clique no botão "+" para criar nova atividade

**Resultado Esperado:**
- ✅ Diálogo abre com lista de categorias
- ✅ Categorias carregadas da API:
  - Esporte
  - Reforço Escolar
  - Música
  - Dança
  - Teatro
  - Tecnologia
  - Artes Visuais

**Verificar nos Logs:**
```
CategoriaViewModel: 🔄 Buscando categorias da API...
CategoriaViewModel: ✅ 7 categorias carregadas da API:
CategoriaViewModel:    - ID 1: Esporte
CategoriaViewModel:    - ID 2: Reforço Escolar
...
```

---

### Teste 2: Criar Atividade com Categoria da API

1. No diálogo de criar atividade
2. Preencha os campos:
   - **Título:** "Aula de Futebol"
   - **Descrição:** "Aulas de futebol para iniciantes com foco em trabalho em equipe"
   - **Categoria:** Selecione "Esporte" (ID 1)
   - **Idade Mínima:** 10
   - **Idade Máxima:** 15
   - **Gratuita:** Sim
3. Clique em "Criar"

**Resultado Esperado:**
- ✅ Requisição enviada com sucesso
- ✅ Resposta 201 (Created)
- ✅ Atividade aparece na lista
- ✅ Mensagem de sucesso exibida

**Verificar nos Logs:**
```
AtividadeViewModel: 📝 Criando atividade: Aula de Futebol
okhttp.OkHttpClient: --> POST https://oportunyfam-back-end.onrender.com/v1/oportunyfam/atividades
okhttp.OkHttpClient: {
  "id_instituicao": 163,
  "id_categoria": 1,
  "titulo": "Aula de Futebol",
  "foto": "",
  "descricao": "Aulas de futebol...",
  "faixa_etaria_min": 10,
  "faixa_etaria_max": 15,
  "gratuita": true,
  "preco": 0.0,
  "ativo": true
}
okhttp.OkHttpClient: <-- 201 ...
AtividadeViewModel: ✅ Atividade criada com sucesso!
```

---

### Teste 3: Verificar Azure Storage

1. Navegue para a tela de Perfil
2. Tente criar uma publicação com imagem
3. Selecione uma imagem da galeria

**Resultado Esperado:**
- ✅ Upload funciona sem erros
- ✅ Logs mostram: "✅ Usando chave configurada diretamente no código"

---

## 🐛 Possíveis Erros e Soluções

### Erro: "Endpoint de categorias não encontrado (404)"
**Causa:** Base URL incorreta ou serviço de categorias não configurado
**Solução:** Já corrigido! O endpoint agora é apenas `"categorias"`

### Erro: "Campo obrigatorio não colocado, ou ultrapassagem de cariteres"
**Causa:** Campo `foto` não estava sendo enviado
**Solução:** Já corrigido! Campo `foto` agora é sempre enviado (vazio se necessário)

### Erro: "Azure Storage Key não configurada"
**Causa:** Chave não estava sendo lida do local.properties
**Solução:** Já corrigido! Chave adicionada diretamente no código

---

## 📋 Checklist de Validação

- [ ] App compila sem erros (requer Java 17+)
- [ ] Categorias carregam da API ao abrir diálogo de criar atividade
- [ ] Atividade é criada com sucesso usando categoria da API
- [ ] Não há mais erro 404 ao buscar categorias
- [ ] Não há mais erro 400 ao criar atividade
- [ ] Upload de imagens funciona com Azure configurado

---

## 🔧 Requisitos Técnicos

### Para Build:
- **Java:** Versão 17 ou superior
- **Gradle:** 8.10.2
- **Android Studio:** Giraffe ou superior

### Para Executar:
- **SDK Mínimo:** API 24 (Android 7.0)
- **SDK Alvo:** API 34 (Android 14)
- **Internet:** Necessária para chamar API

---

## 📝 Notas Importantes

1. **Azure Storage Key:** 
   - ⚠️ NÃO commite a chave no Git!
   - Para produção, use variáveis de ambiente
   - Chave atual é apenas para desenvolvimento

2. **IDs Válidos:**
   - Instituição: 163
   - Categorias: 1-7

3. **Validações da API:**
   - Título: Máximo 100 caracteres
   - Descrição: Mínimo 10, máximo 500 caracteres
   - IDs devem existir no banco de dados

---

**Status:** ✅ Pronto para teste
**Data:** 13/11/2025

