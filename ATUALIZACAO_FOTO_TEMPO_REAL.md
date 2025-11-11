# Atualização de Foto de Atividade em Tempo Real

## Problema Resolvido
A foto da atividade só estava sendo atualizada quando você trocava completamente de tela. Agora a foto é atualizada imediatamente na lista de atividades após a edição.

## Alterações Realizadas

### 1. **AtividadeViewModel.kt**
Adicionado método `atualizarFotoAtividade()` que atualiza a foto de uma atividade específica na lista sem precisar recarregar todas as atividades:

```kotlin
fun atualizarFotoAtividade(atividadeId: Int, novaFotoUrl: String) {
    val currentState = _atividadesState.value
    if (currentState is AtividadesState.Success) {
        val atividadesAtualizadas = currentState.atividades.map { atividade ->
            if (atividade.atividade_id == atividadeId) {
                atividade.apply {
                    atividade_foto = novaFotoUrl
                }
            } else {
                atividade
            }
        }
        _atividadesState.value = AtividadesState.Success(atividadesAtualizadas)
        Log.d("AtividadeViewModel", "✅ Foto atualizada na lista para atividade $atividadeId")
    }
}
```

**Benefício**: Atualização eficiente sem necessidade de nova chamada à API.

### 2. **DetalhesAtividadeScreen.kt**
Adicionada chamada ao novo método após o upload da foto:

```kotlin
// 4. Atualizar a foto na lista de atividades (para refletir mudança ao voltar)
viewModel.atualizarFotoAtividade(atividadeId, versionedUrl)
```

**Benefício**: A lista de atividades é atualizada imediatamente quando você muda a foto.

### 3. **AtividadesScreen.kt**
Adicionado reload da lista ao voltar da tela de detalhes (backup, caso o método acima falhe):

```kotlin
onBack = {
    viewModel.limparDetalhe()
    // Recarregar lista de atividades ao voltar
    instituicaoId?.let { instId ->
        viewModel.buscarAtividadesPorInstituicao(instId)
    }
    telaAtual = TelaAtividade.LISTA
}
```

**Benefício**: Garante que a lista esteja sempre atualizada ao voltar.

### 4. **AtividadeCardAPI.kt & ResumoAtividadeCardAPI.kt**
Alterada a política de cache das imagens:

**Antes**:
```kotlin
.diskCachePolicy(CachePolicy.READ_ONLY)
```

**Depois**:
```kotlin
.diskCachePolicy(CachePolicy.ENABLED)
```

**Benefício**: Permite que o Coil carregue novas versões das imagens quando a URL muda (graças ao parâmetro de versão `?v=timestamp`).

## Como Funciona Agora

### Fluxo de Atualização de Foto

1. **Usuário seleciona nova foto** na tela de detalhes da atividade
2. **Upload para Azure** é feito com URL versionada (`?v=timestamp`)
3. **Salvamento local** no DataStore (fallback)
4. **Atualização na API** (se disponível)
5. **Atualização no estado de detalhes** (tela atual)
6. **✨ NOVO: Atualização imediata na lista** via `atualizarFotoAtividade()`
7. **Reload ao voltar** (backup adicional)

### Resultado

✅ A foto é atualizada **imediatamente** na lista de atividades  
✅ Não é necessário trocar de tela para ver a mudança  
✅ A atualização é **eficiente** (sem recarregar todas as atividades)  
✅ O sistema continua funcionando mesmo se a API falhar (usa cache local)

## Observações Técnicas

- A URL com parâmetro de versão (`?v=timestamp`) garante que o cache seja invalidado
- O método `atualizarFotoAtividade()` é chamado assim que o upload é concluído
- O Compose reage automaticamente às mudanças no `StateFlow`
- A política de cache `ENABLED` permite que o Coil gerencie o cache de forma inteligente

## Testes Recomendados

1. ✓ Editar foto de uma atividade
2. ✓ Verificar se a foto aparece imediatamente na lista ao voltar
3. ✓ Verificar se funciona com múltiplas atividades
4. ✓ Testar com conexão lenta
5. ✓ Testar com falha de API (deve usar cache local)

