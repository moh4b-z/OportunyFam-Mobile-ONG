package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.Telas.ConversasScreen
import com.example.oportunyfam.SplashScreen
import com.example.Telas.PerfilScreen
import com.example.Telas.RegistroScreen
import com.example.oportunyfam.Telas.AtividadesScreen
import com.example.oportunyfam.Telas.HomeScreen
import com.example.theme.OportunyFamMobileONGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OportunyFamMobileONGTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "AtividadesScreen"
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
                }
            }
        }
    }
}
