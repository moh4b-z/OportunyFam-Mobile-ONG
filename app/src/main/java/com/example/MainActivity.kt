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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.AuthDataStore
import com.example.screens.PerfilScreen
import com.example.screens.RegistroScreen // Assumindo que você tem essa tela
import com.example.theme.OportunyFamMobileONGTheme

// Definição das rotas para o NavHost
object NavRoutes {
    const val REGISTRO = "registro" // Tela de login/registro (onde o app inicia se deslogado)
    const val PERFIL = "perfil"     // Tela principal após o login
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Inicializa o AuthDataStore
            val authDataStore = remember { AuthDataStore(applicationContext) }
            OportunyFamMobileONGTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Chama a função principal de navegação
                    AppNavigation(authDataStore = authDataStore)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(authDataStore: AuthDataStore) {
    val navController = rememberNavController()

    // 1. Define a tela inicial com base no status de login
    val startDestination = if (authDataStore.isUserLoggedIn()) {
        NavRoutes.PERFIL
    } else {
        NavRoutes.REGISTRO
    }

    NavHost(navController = navController, startDestination = startDestination) {
        // Rota de Registro/Login
        composable(NavRoutes.REGISTRO) {
            // OBS: Sua RegistroScreen deve ter acesso ao AuthDataStore e ao NavController
            // para salvar os dados no sucesso e navegar para NavRoutes.PERFIL.
            RegistroScreen(navController = navController)
        }

        // Rota de Perfil/Tela principal
        composable(NavRoutes.PERFIL) {
            // 2. Carrega os dados salvos para exibir
            val instituicao = authDataStore.loadInstituicao()

            if (instituicao != null) {
                // Passa os dados carregados para a PerfilScreen
                PerfilScreen(
                    navController = navController,
                    instituicaoNome = instituicao.nome,
                    instituicaoEmail = instituicao.email,
                    // Define a ação de logout
                    onLogout = {
                        authDataStore.logout() // Limpa o SharedPreferences
                        // Navega para Registro e remove a tela Perfil do back stack
                        navController.navigate(NavRoutes.REGISTRO) {
                            popUpTo(NavRoutes.PERFIL) { inclusive = true }
                        }
                    }
                )
            } else {
                // Caso os dados sumam (por segurança), redireciona para a tela de login
                navController.navigate(NavRoutes.REGISTRO) {
                    popUpTo(NavRoutes.PERFIL) { inclusive = true }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    OportunyFamMobileONGTheme {
        RegistroScreen(navController = null)
    }
}
