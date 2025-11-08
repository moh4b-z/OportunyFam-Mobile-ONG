package com.oportunyfam_mobile_ong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.oportunyfam_mobile_ong.Screens.ChatScreen
import com.oportunyfam_mobile_ong.Screens.ConversasScreen
import com.oportunyfam_mobile_ong.Screens.SplashScreen
import com.oportunyfam_mobile_ong.Screens.PerfilScreen
import com.oportunyfam_mobile_ong.Screens.RegistroScreen
import com.oportunyfam_mobile_ong.Screens.AtividadesScreen
import com.oportunyfam_mobile_ong.Screens.HomeScreen
import com.oportunyfam_mobile_ong.theme.OportunyFamMobileONGTheme

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

                    // Tela de Chat
                    composable(
                        route = "${NavRoutes.CHAT}/{conversaId}/{nomeContato}/{pessoaIdAtual}",
                        arguments = listOf(
                            navArgument("conversaId") { type = NavType.IntType },
                            navArgument("nomeContato") { type = NavType.StringType },
                            navArgument("pessoaIdAtual") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val conversaId = backStackEntry.arguments?.getInt("conversaId") ?: 0
                        val nomeContato = backStackEntry.arguments?.getString("nomeContato") ?: ""
                        val pessoaIdAtual = backStackEntry.arguments?.getInt("pessoaIdAtual") ?: 0

                        ChatScreen(
                            navController = navController,
                            conversaId = conversaId,
                            nomeContato = nomeContato,
                            pessoaIdAtual = pessoaIdAtual
                        )
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
        const val CHAT = "ChatScreen"
    }
}
