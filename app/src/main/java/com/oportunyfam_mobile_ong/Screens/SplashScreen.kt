package com.oportunyfam_mobile_ong.Screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.oportunyfam_mobile_ong.MainActivity.NavRoutes
import com.oportunyfam_mobile_ong.R
import com.oportunyfam_mobile_ong.data.InstituicaoAuthDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val authDataStore = remember { InstituicaoAuthDataStore(context) }

    SplashScreenContent(
        onAnimationEnd = {
            // Verifica se há usuário logado de forma assíncrona
            kotlinx.coroutines.MainScope().launch {
                try {
                    val isLoggedIn = authDataStore.isUserLoggedIn()
                    android.util.Log.d("SplashScreen", "Verificando login: $isLoggedIn")

                    if (isLoggedIn) {
                        android.util.Log.d("SplashScreen", "Usuário logado, navegando para HOME")
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    } else {
                        android.util.Log.d("SplashScreen", "Usuário não logado, navegando para REGISTRO")
                        navController.navigate(NavRoutes.REGISTRO) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SplashScreen", "Erro ao verificar login: ${e.message}", e)
                    // Em caso de erro, vai para tela de registro
                    navController.navigate(NavRoutes.REGISTRO) {
                        popUpTo(NavRoutes.SPLASH) { inclusive = true }
                    }
                }
            }
        }
    )
}


@Composable
private fun SplashScreenContent(onAnimationEnd: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Efeito bounce suave do logo
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        scale.animateTo(1f, animationSpec = tween(300))

        // Fade do fundo
        alpha.animateTo(1f, animationSpec = tween(1500))
        delay(1000)

        // Chama o callback após a animação
        onAnimationEnd()
    }

    // Fundo gradiente mais quente e moderno
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF69508), // Laranja
                        Color(0xFFFFC14D), // Amarelo
                        Color(0xFFFFF1D2)  // Suave creme
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Bolinhas animadas no fundo
        ParticlesLayer(alpha = alpha.value)

        // Logo animada
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo OportunyFam",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
                .alpha(alpha.value)
        )
    }
}

/**
 * Camada de partículas animadas — agora com movimento mais rápido
 */
@Composable
fun ParticlesLayer(alpha: Float) {
    val particles = remember {
        List(15) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextInt(4, 10),
                delay = Random.nextLong(0, 1000),
                speed = Random.nextFloat() * 0.005f + 0.0025f // 👈 mais rápido
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            var offsetY by remember { mutableStateOf(particle.y) }
            var rotation by remember { mutableStateOf(0f) }

            LaunchedEffect(Unit) {
                delay(particle.delay)
                while (true) {
                    offsetY -= particle.speed
                    rotation += 2.5f // 👈 rotação mais perceptível
                    if (offsetY < 0f) offsetY = 1f
                    delay(16)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
                    .offset(
                        x = ((particle.x - 0.5f) * 300).dp,
                        y = ((offsetY - 0.5f) * 600).dp
                    )
                    .size(particle.size.dp)
                    .background(
                        color = Color.White.copy(alpha = alpha * 0.35f),
                        shape = CircleShape
                    )
                    .alpha(alpha)
            )
        }
    }
}

data class Particle(
    val x: Float,
    var y: Float,
    val size: Int,
    val delay: Long,
    val speed: Float
)
