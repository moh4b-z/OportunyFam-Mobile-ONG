# Correções para Criar Atividade - Erro 400

## Problema Original
Ao tentar criar uma nova atividade, a API retornava erro 400 com a mensagem:
```
"Campo obrigatorio não colocado, ou ultrapassagem de cariteres"
```

## Análise
1. O backend estava rejeitando a requisição devido a problemas de validação
2. A descrição estava sendo enviada como `null` quando vazia
3. Não havia limites de caracteres nos campos de entrada
4. As mensagens de erro não eram detalhadas o suficiente

## Correções Implementadas

### 1. Modelo de Dados (`Atividade.kt`)
**Mudança:** Campo `descricao` agora é não-nulo com valor padrão vazio
```kotlin
// ANTES
descricao: String? = null,

// DEPOIS  
descricao: String = "",
```
**Motivo:** O backend pode ter validação que não aceita null para descrição

### 2. Diálogo de Criação (`CriarAtividadeDialog.kt`)

#### Limites de Caracteres
- **Título:** Máximo 100 caracteres (obrigatório)
- **Descrição:** Mínimo 10 caracteres, máximo 500 caracteres (obrigatório)
- Contador de caracteres visível abaixo de cada campo

#### Validação Melhorada
```kotlin
val isFormValid = titulo.isNotBlank() &&
    titulo.length <= 100 &&
    descricao.isNotBlank() &&
    descricao.length >= 10 &&
    descricao.length <= 500 &&
    faixaEtariaMin.isNotBlank() &&
    faixaEtariaMax.isNotBlank() &&
    faixaEtariaMin.toInt() <= faixaEtariaMax.toInt() &&
    faixaEtariaMin.toInt() >= 0 &&
    faixaEtariaMax.toInt() <= 99 &&
    (gratuita || preco.toDouble() > 0)
```

**Validações adicionadas:**
- **Descrição obrigatória** com mínimo de 10 caracteres
- Idade mínima >= 0
- Idade máxima <= 99
- Limites de caracteres respeitados

### 3. Tela de Lista (`ListaAtividadesScreen.kt`)
**Mudança:** Descrição vazia agora envia string vazia ao invés de null
```kotlin
descricao = descricao.ifEmpty { "" }
```

### 4. Tela de Detalhes (`DetalhesAtividadeScreen.kt`)
**Mudança:** Ao atualizar atividade, trata descrição nullable corretamente
```kotlin
descricao = atividade.descricao ?: "", // Handle nullable descricao
```
**Motivo:** O `AtividadeResponse` tem descrição nullable, mas `AtividadeRequest` agora espera non-null

### 5. ViewModel (`AtividadeViewModel.kt`)

#### Logging Melhorado
Adicionado log detalhado dos dados sendo enviados:
```kotlin
Log.d("AtividadeViewModel", "📋 Dados: titulo=${request.titulo}, descricao=${request.descricao}, " +
        "categoria=${request.id_categoria}, instituicao=${request.id_instituicao}, " +
        "idade=${request.faixa_etaria_min}-${request.faixa_etaria_max}, " +
        "gratuita=${request.gratuita}, preco=${request.preco}")
```

#### Mensagens de Erro Detalhadas
Agora extrai a mensagem de erro do JSON da API:
```kotlin
val errorMessage = try {
    errorBody?.let {
        val msgStart = it.indexOf("\"messagem\":\"") + 12
        val msgEnd = it.indexOf("\"", msgStart)
        if (msgStart > 11 && msgEnd > msgStart) {
            it.substring(msgStart, msgEnd)
        } else {
            "Erro ao criar atividade (${response.code()})"
        }
    } ?: "Erro ao criar atividade (${response.code()})"
} catch (e: Exception) {
    "Erro ao criar atividade (${response.code()})"
}
```

## Como Testar

1. **Teste 1: Atividade Básica Gratuita**
   - Título: "Aula de Futebol"
   - Descrição: "Aprenda a jogar futebol com instrutores qualificados" (mínimo 10 chars)
   - Categoria: Esportes
   - Idade: 6-12
   - Gratuita: Sim
   - ✅ Deve criar com sucesso

2. **Teste 2: Atividade com Preço**
   - Título: "Oficina de Artes"
   - Descrição: "Atividade voltada para crianças aprenderem técnicas de arte"
   - Categoria: Artes
   - Idade: 8-14
   - Gratuita: Não
   - Preço: 50.00
   - ✅ Deve criar com sucesso

3. **Teste 3: Validação de Descrição Curta**
   - Título: "Teste"
   - Descrição: "Curta" (menos de 10 caracteres)
   - ❌ Deve bloquear criação
   - ✅ Deve mostrar erro visual no campo

4. **Teste 4: Validação de Limites**
   - Tente digitar mais de 100 caracteres no título
   - ✅ Deve bloquear após 100 caracteres
   - Tente digitar mais de 500 caracteres na descrição
   - ✅ Deve bloquear após 500 caracteres

5. **Teste 5: Mensagens de Erro da API**
   - Se houver erro da API, deve mostrar a mensagem específica
   - ✅ Deve exibir mensagem detalhada do backend

## Próximos Passos (Opcional)

1. **Validação no Backend:** Verificar exatamente quais são os limites de caracteres no banco de dados
2. **Campos Obrigatórios:** Confirmar se todos os campos estão sendo enviados corretamente
3. **Testes Adicionais:** Testar com diferentes categorias e instituições
4. **Feedback Visual:** Adicionar mais feedback visual durante o processo de criação

## Comandos para Rebuild

```bash
# Limpar build
./gradlew clean

# Compilar e instalar
./gradlew assembleDebug
./gradlew installDebug
```

## Monitoramento de Logs

Para ver os logs detalhados:
```bash
adb logcat | grep "AtividadeViewModel\|okhttp"
```

---

**Data da Correção:** 13 de Novembro de 2025
**Arquivos Modificados:**
1. `app/src/main/java/com/oportunyfam_mobile_ong/model/Atividade.kt`
2. `app/src/main/java/com/oportunyfam_mobile_ong/Components/CriarAtividadeDialog.kt`
3. `app/src/main/java/com/oportunyfam_mobile_ong/Screens/ListaAtividadesScreen.kt`
4. `app/src/main/java/com/oportunyfam_mobile_ong/Screens/DetalhesAtividadeScreen.kt` (fix de compilação)
5. `app/src/main/java/com/oportunyfam_mobile_ong/viewmodel/AtividadeViewModel.kt`

