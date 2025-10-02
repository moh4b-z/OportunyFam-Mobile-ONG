package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.Screens.CadastroScreen
import com.example.theme.OportunyFamMobileONGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OportunyFamMobileONGTheme {
                // Surface preenche toda a tela
                Surface(modifier = Modifier.fillMaxSize()) {
                    CadastroScreen(navController = null) // Aqui chamamos sua tela
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    OportunyFamMobileONGTheme {
        CadastroScreen(navController = null)
    }
}
