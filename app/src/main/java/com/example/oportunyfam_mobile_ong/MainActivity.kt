package com.example.oportunyfam_mobile_ong

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.oportunyfam_mobile_ong.Screens.ChatScreen
import com.example.oportunyfam_mobile_ong.Screens.ConversasScreen
import com.example.oportunyfam_mobile_ong.Screens.SplashScreen
import com.example.oportunyfam_mobile_ong.Screens.PerfilScreen
import com.example.oportunyfam_mobile_ong.Screens.RegistroScreen
import com.example.oportunyfam_mobile_ong.Screens.AtividadesScreen
import com.example.oportunyfam_mobile_ong.Screens.HomeScreen
import com.example.oportunyfam_mobile_ong.theme.OportunyFamMobileONGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OportunyFamMobileONGTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "tela_splash"
                ) {
                    composable("tela_splash") {
                        SplashScreen(navController)
                    }

                    composable("tela_registro") {
                        RegistroScreen(navController)
                    }

                    composable("tela_perfil") {
                        PerfilScreen(
                            navController = navController
                        )
                    }

                    composable("HomeScreen") {
                        HomeScreen(navController)
                    }

                    composable("AtividadesScreen") {
                        AtividadesScreen(navController)
                    }

                    composable("ConversasScreen") {
                        ConversasScreen(navController)
                    }

                    composable(
                        route = "ChatScreen/{conversaId}/{nomeContato}/{pessoaId}",
                        arguments = listOf(
                            navArgument("conversaId") { type = NavType.IntType },
                            navArgument("nomeContato") { type = NavType.StringType },
                            navArgument("pessoaId") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val conversaId = backStackEntry.arguments?.getInt("conversaId") ?: 0
                        val nomeContato = backStackEntry.arguments?.getString("nomeContato") ?: ""
                        val pessoaId = backStackEntry.arguments?.getInt("pessoaId") ?: 0

                        ChatScreen(
                            navController = navController,
                            conversaId = conversaId,
                            nomeContato = nomeContato,
                            pessoaIdAtual = pessoaId
                        )
                    }
                }
            }
        }
    }
}
