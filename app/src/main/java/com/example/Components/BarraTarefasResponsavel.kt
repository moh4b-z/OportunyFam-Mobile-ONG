package com.example.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.MainActivity.NavRoutes

/**
 * BarraTarefas - Barra de navegação inferior do aplicativo
 *
 * Exibe três opções principais: Início, Atividades e Perfil.
 * Possui gradiente horizontal laranja para melhor visual.
 *
 * @param navController Controlador de navegação (opcional para preview)
 * @param currentRoute Rota atual para destacar o item selecionado
 */
@Composable
fun BarraTarefas(
    navController: NavController? = null,
    currentRoute: String = ""
) {
    // Gradiente suave horizontal: laranja escuro à esquerda para laranja claro à direita
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000), // Laranja original
            Color(0xFFFFD27A)  // Laranja claro suave
        )
    )

    NavigationBar(
        modifier = Modifier
            .height(70.dp)
            .fillMaxWidth()
            .background(gradient),
        containerColor = Color.Transparent // Mantém transparente para o gradiente aparecer
    ) {
        // Item Início
        NavigationBarItem(
            selected = currentRoute == NavRoutes.HOME,
            onClick = { navController?.navigate(NavRoutes.HOME) },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Início",
                        modifier = Modifier.size(20.dp),
                        tint = if (currentRoute == NavRoutes.HOME) Color(0xFF5A3E1B) else Color(0x805A3E1B)
                    )
                    Text(
                        text = "Início",
                        fontSize = 10.sp,
                        color = if (currentRoute == NavRoutes.HOME) Color(0xFF5A3E1B) else Color(0x805A3E1B)
                    )
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF5A3E1B),
                unselectedIconColor = Color(0x805A3E1B),
                selectedTextColor = Color(0xFF5A3E1B),
                unselectedTextColor = Color(0x805A3E1B),
                indicatorColor = Color.Transparent
            )
        )

        // Item Atividades
        NavigationBarItem(
            selected = currentRoute == NavRoutes.ATIVIDADES,
            onClick = { navController?.navigate(NavRoutes.ATIVIDADES) },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = "Atividades",
                        modifier = Modifier.size(20.dp),
                        tint = if (currentRoute == NavRoutes.ATIVIDADES) Color(0xFF5A3E1B) else Color(0x805A3E1B)
                    )
                    Text(
                        text = "Atividades",
                        fontSize = 10.sp,
                        color = if (currentRoute == NavRoutes.ATIVIDADES) Color(0xFF5A3E1B) else Color(0x805A3E1B)
                    )
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF5A3E1B),
                unselectedIconColor = Color(0x805A3E1B),
                selectedTextColor = Color(0xFF5A3E1B),
                unselectedTextColor = Color(0x805A3E1B),
                indicatorColor = Color.Transparent
            )
        )

        // Item Perfil
        NavigationBarItem(
            selected = currentRoute == NavRoutes.PERFIL,
            onClick = { navController?.navigate(NavRoutes.PERFIL) },
            icon = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Face,
                        contentDescription = "Perfil",
                        modifier = Modifier.size(20.dp),
                        tint = if (currentRoute == NavRoutes.PERFIL) Color(0xFF5A3E1B) else Color(0x805A3E1B)
                    )
                    Text(
                        text = "Perfil",
                        fontSize = 10.sp,
                        color = if (currentRoute == NavRoutes.PERFIL) Color(0xFF5A3E1B) else Color(0x805A3E1B)
                    )
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF5A3E1B),
                unselectedIconColor = Color(0x805A3E1B),
                selectedTextColor = Color(0xFF5A3E1B),
                unselectedTextColor = Color(0x805A3E1B),
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 70)
@Composable
fun BarraTarefasPreview() {
    BarraTarefas()
}
