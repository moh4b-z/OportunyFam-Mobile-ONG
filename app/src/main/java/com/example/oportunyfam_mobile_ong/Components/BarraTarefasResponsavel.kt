
package com.example.oportunyfam_mobile_ong.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun BarraTarefas(
    navController: NavHostController? = null,
    currentRoute: String = ""
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFFA000),
            Color(0xFFFFD27A)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // Barra de navegação
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
                .background(gradient),
            containerColor = Color.Transparent
        ) {
            // Item Início
            NavigationBarItem(
                selected = currentRoute == "HomeScreen",
                onClick = { navController?.navigate("HomeScreen") },
                icon = {
                    NavItemContent(
                        icon = Icons.Filled.Home,
                        label = "Início",
                        isSelected = currentRoute == "HomeScreen"
                    )
                }
            )

            // Item Atividades
            NavigationBarItem(
                selected = currentRoute == "AtividadesScreen",
                onClick = { navController?.navigate("AtividadesScreen") },
                icon = {
                    NavItemContent(
                        icon = Icons.Filled.CheckCircle,
                        label = "Atividades",
                        isSelected = currentRoute == "AtividadesScreen"
                    )
                }
            )

            // Espaço vazio para o botão flutuante
            Spacer(modifier = Modifier.weight(1f))

            // Item Perfil
            NavigationBarItem(
                selected = currentRoute == "tela_perfil",
                onClick = { navController?.navigate("tela_perfil") },
                icon = {
                    NavItemContent(
                        icon = Icons.Filled.Person,
                        label = "Perfil",
                        isSelected = currentRoute == "tela_perfil"
                    )
                }
            )
        }

        // Botão flutuante de conversas no centro
        FloatingActionButton(
            onClick = { navController?.navigate("ConversasScreen") },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp)
                .size(64.dp)
                .shadow(8.dp, CircleShape),
            containerColor = Color(0xFFFF6F00),
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = "Conversas",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItemContent(
    icon: ImageVector,
    label: String,
    isSelected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = if (isSelected) Color(0xFF5A3E1B) else Color(0xFF8D6E63)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Color(0xFF5A3E1B) else Color(0xFF8D6E63)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 80)
@Composable
fun BarraTarefasPreview() {
    BarraTarefas()
}
