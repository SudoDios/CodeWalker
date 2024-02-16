package me.sudodios.codewalker.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.ui.components.Txt
import me.sudodios.codewalker.ui.components.TxtField
import me.sudodios.codewalker.ui.theme.ColorTheme

@Composable
fun AddPatternDialog(
    onCloseRequest: () -> Unit,
    onSelectPattern : (String) -> Unit
) {

    var pattern by remember { mutableStateOf("") }

    Column(Modifier.width(380.dp).background(ColorTheme.colorPrimaryDark)) {
        DialogToolbar(title = "Write new pattern",
            content = {
                Button(
                    enabled = pattern.isNotEmpty(),
                    onClick = {
                        onSelectPattern.invoke(pattern)
                    }
                ) {
                    Txt("Accept")
                }
            },
            onCloseClicked = {
                onCloseRequest.invoke()
            }
        )
        TxtField(modifier = Modifier.padding(20.dp).fillMaxWidth(),
            label = "Write pattern here",
            value = pattern,
            onValueChange = {
                pattern = it
            }
        )
    }

}