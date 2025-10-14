import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CardAviso(
    pergunta: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Atenção!",
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFFFB301)
            )
        },
        text = {
            Text(
                text = pergunta,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFFFFFFF)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Sim"
                    ,
                    color = Color(0xFFFFB301)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Não",
                    color = Color(0xFFFFFFFF)
                )
            }
        },
        containerColor = Color(0xFF363534)
    )
}

@Preview(showBackground = true)
@Composable
fun CardAvisoPreview() {
    CardAviso(
        pergunta = "Deseja realmente excluir este cliente?",
        onConfirm = {},
        onDismiss = {},
        onCancel = {}
    )
}
