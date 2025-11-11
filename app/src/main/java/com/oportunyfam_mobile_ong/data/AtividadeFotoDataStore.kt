package com.oportunyfam_mobile_ong.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * DataStore para armazenar fotos individuais das atividades
 * Cada atividade pode ter sua própria foto, independente da foto da instituição
 */
class AtividadeFotoDataStore(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "atividade_fotos")

        // Chave: atividade_foto_{id}
        private fun atividadeFotoKey(atividadeId: Int) = stringPreferencesKey("atividade_foto_$atividadeId")
    }

    /**
     * Salvar foto de uma atividade específica
     */
    suspend fun salvarFotoAtividade(atividadeId: Int, fotoUrl: String) {
        context.dataStore.edit { preferences ->
            preferences[atividadeFotoKey(atividadeId)] = fotoUrl
        }
    }

    /**
     * Buscar foto de uma atividade específica
     * Retorna null se não houver foto salva
     */
    suspend fun buscarFotoAtividade(atividadeId: Int): String? {
        return context.dataStore.data.map { preferences ->
            preferences[atividadeFotoKey(atividadeId)]
        }.first()
    }

    /**
     * Remover foto de uma atividade
     */
    suspend fun removerFotoAtividade(atividadeId: Int) {
        context.dataStore.edit { preferences ->
            preferences.remove(atividadeFotoKey(atividadeId))
        }
    }

    /**
     * Limpar todas as fotos salvas
     */
    suspend fun limparTodasFotos() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

