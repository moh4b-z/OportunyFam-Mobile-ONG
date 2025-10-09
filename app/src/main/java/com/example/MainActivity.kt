package com.example
import android.os.Bundle
import android.window.SplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.oportunyfam.SplashScreen
import com.example.screens.PerfilScreen
import com.example.screens.RegistroScreen
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
                    startDestination = "tela_splash"
                ) {
                    composable("tela_splash") {
                        SplashScreen(navController)
                    }

                    composable("tela_registro") {
                        RegistroScreen(navController)
                    }

                    composable("tela_perfil") {
                        PerfilScreen(navController,
                            onLogout = {})

                    }
                }
            }
        }
    }
}
