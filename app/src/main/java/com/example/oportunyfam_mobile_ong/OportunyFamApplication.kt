package com.example.oportunyfam_mobile_ong

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

/**
 * Classe Application customizada para inicializar o Firebase
 * e outras configurações globais do app
 */
class OportunyFamApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Inicializa o Firebase
            FirebaseApp.initializeApp(this)

            // Habilita persistência offline do Firebase Realtime Database
            // Isso permite que o app funcione mesmo sem conexão
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)

            Log.d("OportunyFamApp", "✅ Firebase inicializado com sucesso")
            Log.d("OportunyFamApp", "📱 Project ID: ${FirebaseApp.getInstance().options.projectId}")
            Log.d("OportunyFamApp", "🔑 App ID: ${FirebaseApp.getInstance().options.applicationId}")
        } catch (e: Exception) {
            Log.e("OportunyFamApp", "❌ Erro ao inicializar Firebase", e)
        }
    }
}

