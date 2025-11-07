package com.oportunyfam_mobile_ong.oportunyfam

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.oportunyfam_mobile_ong.R
import kotlinx.coroutines.delay

/**
 * SplashScreen - Tela de abertura do aplicativo
 *
 * Exibe o logo com animação de escala e redireciona para a tela de registro
 * após um breve período.
 *
 * @param navController Controlador de navegação para redirecionar após a animação
 */
@Composable
fun SplashScreen(navController: NavController) {
    SplashScreenContent(
        onAnimationEnd = {
            navController.navigate("tela_registro") {
                popUpTo("tela_splash") { inclusive = true }
            }
        }
    )
}

/**
 * Preview da SplashScreen sem funcionalidade de navegação
 */
@Composable
fun SplashScreenPreviewContent() {
    SplashScreenContent(onAnimationEnd = {})
}

/**
 * Conteúdo principal da SplashScreen com animação
 *
 * @param onAnimationEnd Callback executado quando a animação termina
 */
@Composable
private fun SplashScreenContent(onAnimationEnd: () -> Unit) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Anima o logo de 0 a 1 (escala completa)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
        // Aguarda 1 segundo antes de navegar
        delay(1000)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xA9FFEDDB)), // Cor laranja característica
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_inicio),
            contentDescription = "Logo OportunyFam",
            modifier = Modifier
                .size(200.dp)
                .scale(scale.value)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    SplashScreenPreviewContent()
}
