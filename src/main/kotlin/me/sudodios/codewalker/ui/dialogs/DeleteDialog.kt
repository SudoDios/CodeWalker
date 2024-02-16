package me.sudodios.codewalker.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.ui.components.Txt
import me.sudodios.codewalker.ui.theme.ColorTheme

@Composable
fun DeleteDialog(
    projectName : String,
    onCancelClicked : () -> Unit,
    onOKClicked : () -> Unit
) {

    Column(modifier = Modifier.width(340.dp).background(ColorTheme.colorCard2).padding(20.dp)) {
        Txt(
            text = "Delete Project",
            color = ColorTheme.colorText,
            style = MaterialTheme.typography.titleMedium
        )
        Txt(
            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
            text = "Are you sure to delete project $projectName ?\nthis action not have undo option",
            color = ColorTheme.colorText.copy(0.7f),
            style = MaterialTheme.typography.bodyMedium
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                modifier = Modifier.padding(end = 8.dp).weight(1f),
                colors = ButtonDefaults.buttonColors(
                    contentColor = ColorTheme.colorText,
                    containerColor = ColorTheme.colorText.copy(0.2f)
                ),
                onClick = {
                    onCancelClicked.invoke()
                }
            ) {
                Txt("Cancel")
            }
            Button(
                modifier = Modifier.padding(start = 8.dp).weight(1f),
                onClick = {
                    onOKClicked.invoke()
                }
            ) {
                Txt("Delete")
            }
        }
    }

}