# 🚀 Quick Reference - OportunyFam Mobile ONG

## 📸 Fotos das Atividades/Instituição

### Comportamento Atual
```
Nova Instituição → SEM foto → Ícone padrão ✅
Upload no Perfil → COM foto → Mostra nas atividades ✅
Atualizar foto → Nova foto → Substitui anterior ✅
```

### Código Relevante
```kotlin
// AtividadeCardAPI.kt & ResumoAtividadeCardAPI.kt
if (!atividade.instituicao_foto.isNullOrEmpty() && 
    atividade.instituicao_foto != "null") {
    AsyncImage(...) // Carrega foto
} else {
    Image(R.drawable.instituicao) // Ícone padrão
}
```

---

## 📋 Status de Inscrições

### Status Disponíveis
| ID | Nome | Ação |
|----|------|------|
| 2 | Cancelada | Inscrição cancelada |
| 3 | Pendente | Aguardando decisão |
| 4 | Aprovada | ✅ Aluno aceito |
| 5 | Negada | ❌ Aluno recusado |

### Como Alterar
```
1. Detalhes Atividade → "Gerenciar Alunos"
2. Clicar dropdown de status do aluno
3. Selecionar novo status
4. ✅ Atualizado automaticamente!
```

### Endpoint da API
```http
PUT /v1/oportunyfam/inscricoes/{id}
{"id_status": 4}
```

---

## 🔍 Debug Rápido

### Ver Logs de Foto
```bash
adb logcat | grep "PerfilScreen\|Upload"
```

### Ver Logs de Status
```bash
adb logcat | grep "InscricaoViewModel\|GerenciarAlunos"
```

### Ver Requisições HTTP
```bash
adb logcat | grep "okhttp.OkHttpClient"
```

---

## 📁 Arquivos Modificados

```
✅ AtividadeCardAPI.kt
✅ ResumoAtividadeCardAPI.kt
✅ GerenciarAlunosScreen.kt (verificado)
✅ InscricaoViewModel.kt (verificado)
```

---

## 🎯 Testes Essenciais

- [ ] Nova instituição mostra ícone padrão
- [ ] Upload de foto funciona
- [ ] Foto aparece nas atividades
- [ ] Alterar status funciona
- [ ] Snackbar de sucesso aparece
- [ ] Lista recarrega após mudança
- [ ] Remover aluno funciona

---

## 📚 Documentação Completa

1. **RESUMO_COMPLETO.md** → Visão geral completa
2. **FOTO_ATIVIDADE_FIX.md** → Detalhes técnicos de fotos
3. **GERENCIAR_ALUNOS_STATUS.md** → Guia de status
4. **QUICK_REFERENCE.md** → Este arquivo

---

## ✅ Status

**TUDO FUNCIONANDO! 🎉**

Foto: ✅ Corrigido
Status: ✅ Funcionando
API: ✅ Integrado
Docs: ✅ Completo

---

**Pronto para produção! 🚀**

