# Correção: Foto da Atividade/Instituição

## Problema
A foto da instituição estava aparecendo fixa nas atividades, mesmo quando uma nova instituição era criada sem foto.

## Solução Implementada

### 1. Melhorias nos Cards de Atividade

Foram atualizados dois componentes:
- `AtividadeCardAPI.kt` - Card de lista de atividades
- `ResumoAtividadeCardAPI.kt` - Card de resumo/detalhes da atividade

### 2. Mudanças Específicas

#### Verificação Aprimorada de Foto Nula
```kotlin
// ANTES:
if (!atividade.instituicao_foto.isNullOrEmpty()) {
    // Mostra AsyncImage
}

// DEPOIS:
if (!atividade.instituicao_foto.isNullOrEmpty() && atividade.instituicao_foto != "null") {
    // Mostra AsyncImage
}
```

**Motivo**: Às vezes a API retorna a string literal "null" ao invés de null, então agora verificamos ambos.

#### Políticas de Cache Configuradas
```kotlin
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(atividade.instituicao_foto)
        .crossfade(true)
        .diskCachePolicy(CachePolicy.READ_ONLY)  // ✅ NOVO
        .memoryCachePolicy(CachePolicy.ENABLED)  // ✅ NOVO
        .build(),
    // ...
)
```

**Motivo**: Previne que imagens antigas de outras instituições fiquem em cache e apareçam incorretamente.

### 3. Comportamento Esperado

#### Nova Instituição SEM Foto
- ✅ Atividades mostram ícone padrão (`R.drawable.instituicao`)
- ✅ Não herda foto de instituições anteriores

#### Instituição COM Foto
- ✅ Atividades mostram a foto da instituição
- ✅ Foto pode ser atualizada no Perfil

#### Atualização de Foto
1. Vá para a tela de Perfil
2. Clique no ícone de câmera sobre a foto
3. Selecione uma nova imagem
4. A foto será enviada para o Azure Blob Storage
5. As atividades automaticamente mostrarão a nova foto

### 4. Fluxo Técnico

```
Nova Instituição Criada
    ↓
instituicao_foto = null
    ↓
API retorna atividades com instituicao_foto = null
    ↓
Cards verificam: isNullOrEmpty() && != "null"
    ↓
Mostra ícone padrão (R.drawable.instituicao)
    ↓
[Usuário adiciona foto no Perfil]
    ↓
Upload para Azure → URL gerada
    ↓
PUT /instituicoes/:id com nova foto_perfil
    ↓
API retorna atividades com instituicao_foto = "https://..."
    ↓
Cards carregam imagem com Coil
```

### 5. Arquivos Modificados

1. **AtividadeCardAPI.kt**
   - Adicionada verificação de string "null"
   - Configuradas políticas de cache
   - Comentário melhorado

2. **ResumoAtividadeCardAPI.kt**
   - Adicionada verificação de string "null"
   - Configuradas políticas de cache
   - Comentário melhorado

### 6. Observações Importantes

⚠️ **Sobre o Azure Storage**
- As fotos são armazenadas no Azure Blob Storage
- A chave de acesso está configurada em `AzureConfig.kt`
- Container usado: `perfil-instituicoes`

⚠️ **Sobre Cache de Imagens**
- O Coil (biblioteca de imagens) pode fazer cache
- Usamos `diskCachePolicy(READ_ONLY)` para evitar cache incorreto
- URLs têm parâmetro `?v=timestamp` para forçar atualização

⚠️ **Sobre a API**
- O campo `instituicao_foto` vem da API backend
- É o mesmo campo usado no perfil da instituição
- Não há campo separado para foto da atividade

## Testando a Correção

1. **Criar nova instituição sem foto:**
   - Registre uma nova instituição
   - Crie uma atividade
   - Verifique que mostra ícone padrão ✅

2. **Adicionar foto à instituição:**
   - Vá para Perfil
   - Clique na câmera
   - Selecione uma imagem
   - Volte para Atividades
   - Verifique que a foto aparece ✅

3. **Trocar foto da instituição:**
   - Vá para Perfil
   - Clique na câmera novamente
   - Selecione outra imagem
   - Volte para Atividades
   - Verifique que a nova foto aparece ✅

## Conclusão

✅ Problema resolvido!
- Novas instituições mostram ícone padrão
- Fotos podem ser adicionadas/atualizadas no Perfil
- Cache não interfere mais
- String "null" da API é tratada corretamente

