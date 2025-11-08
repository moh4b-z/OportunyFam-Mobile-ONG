package com.oportunyfam_mobile_ong.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oportunyfam_mobile_ong.R

@Composable
fun CardEsporte(
    nomeEsporte: String,
    quantidade: Int,
    icone: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = icone),
                    contentDescription = nomeEsporte,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(end = 10.dp)
                )

                Text(
                    text = nomeEsporte,
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = String.format("%02d", quantidade),
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardEsportePreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        CardEsporte("Futebol", 15, R.drawable.futebolicon)
        CardEsporte("Luta", 7, R.drawable.lutaicon)
        CardEsporte("Volei", 20, R.drawable.voleiicon)
    }
}
