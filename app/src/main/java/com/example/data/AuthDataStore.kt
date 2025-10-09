package com.example.data

import android.content.Context
import com.example.oportunyfam.model.Instituicao
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Utilitário para salvar e carregar dados da instituição logada usando SharedPreferences.
 * É necessário o Gson no build.gradle: implementation 'com.google.code.gson:gson:2.10.1'
 */
class AuthDataStore(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "OportunyFamAuth"
        private const val KEY_INSTITUICAO_JSON = "instituicao_json"
    }

    /**
     * Salva o objeto Instituicao no SharedPreferences como uma string JSON.
     * CHAME ESTE MÉTODO APÓS O SUCESSO DO CADASTRO/LOGIN.
     * @param instituicao O objeto Instituicao a ser salvo.
     */
    fun saveInstituicao(instituicao: Instituicao) {
        val json = gson.toJson(instituicao)
        sharedPreferences.edit()
            .putString(KEY_INSTITUICAO_JSON, json)
            .apply()
    }

    /**
     * Carrega o objeto Instituicao do SharedPreferences.
     * @return O objeto Instituicao se encontrado, ou null se não houver dados salvos.
     */
    fun loadInstituicao(): Instituicao? {
        val json = sharedPreferences.getString(KEY_INSTITUICAO_JSON, null) ?: return null
        return try {
            gson.fromJson(json, Instituicao::class.java)
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Verifica se há uma instituição logada.
     * @return true se houver dados salvos, false caso contrário.
     */
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.contains(KEY_INSTITUICAO_JSON)
    }

    /**
     * Limpa todos os dados de autenticação.
     */
    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
}
