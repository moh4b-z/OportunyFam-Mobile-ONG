package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AuthDataStore
import com.example.screens.PerfilScreen
import com.example.screens.RegistroScreen
import com.example.theme.OportunyFamMobileONGTheme

object NavRoutes {
    const val REGISTRO = "registro"
    const val PERFIL = "perfil"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val authDataStore = remember { AuthDataStore(applicationContext) }
            OportunyFamMobileONGTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(authDataStore = authDataStore)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(authDataStore: AuthDataStore) {
    val navController = rememberNavController()
    val startDestination = if (authDataStore.isUserLoggedIn()) NavRoutes.PERFIL else NavRoutes.REGISTRO

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.REGISTRO) {
            RegistroScreen(navController = navController)
        }
        composable(NavRoutes.PERFIL) {
            val instituicao = authDataStore.loadInstituicao()
            // VERIFICAÇÃO DUPLA: instituicao não nula E nome não nulo
            if (instituicao != null && instituicao.nome != null) {
                PerfilScreen(
                    navController = navController,
                    instituicaoNome = instituicao.nome,
                    instituicaoEmail = instituicao.email ?: "Email não informado",
                    onLogout = {
                        authDataStore.logout()
                        navController.navigate(NavRoutes.REGISTRO) {
                            popUpTo(NavRoutes.PERFIL) { inclusive = true }
                        }
                    }
                )
            } else {
                // Se instituição ou nome forem nulos, vai para registro
                navController.navigate(NavRoutes.REGISTRO) {
                    popUpTo(NavRoutes.PERFIL) { inclusive = true }
                }
            }
        }
    }
}