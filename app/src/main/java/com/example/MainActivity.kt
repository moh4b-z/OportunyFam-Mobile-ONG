package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.Screens.ConversasScreen
import com.example.oportunyfam.SplashScreen
import com.example.Screens.PerfilScreen
import com.example.Screens.RegistroScreen
import com.example.oportunyfam.Telas.AtividadesScreen
import com.example.oportunyfam.Telas.HomeScreen
import com.example.theme.OportunyFamMobileONGTheme

/**
 * MainActivity - Atividade principal do aplicativo OportunyFam
 *
 * Gerencia a navegação entre as telas do aplicativo utilizando
 * Jetpack Compose Navigation.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OportunyFamMobileONGTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.SPLASH
                ) {
                    // Splash Screen
                    composable(NavRoutes.SPLASH) {
                        SplashScreen(navController)
                    }

                    // Tela de Registro/Login
                    composable(NavRoutes.REGISTRO) {
                        RegistroScreen(navController)
                    }

                    // Tela de Perfil
                    composable(NavRoutes.PERFIL) {
                        PerfilScreen(navController = navController)
                    }

                    // Tela Home
                    composable(NavRoutes.HOME) {
                        HomeScreen(navController)
                    }

                    // Tela de Atividades
                    composable(NavRoutes.ATIVIDADES) {
                        AtividadesScreen(navController)
                    }

                    // Tela de Conversas
                    composable(NavRoutes.CONVERSAS) {
                        ConversasScreen(navController)
                    }
                }
            }
        }
    }

    /**
     * Objeto contendo todas as rotas de navegação do aplicativo
     */
    companion object NavRoutes {
        const val SPLASH = "tela_splash"
        const val REGISTRO = "tela_registro"
        const val PERFIL = "tela_perfil"
        const val HOME = "HomeScreen"
        const val ATIVIDADES = "AtividadesScreen"
        const val CONVERSAS = "ConversasScreen"
    }
}
