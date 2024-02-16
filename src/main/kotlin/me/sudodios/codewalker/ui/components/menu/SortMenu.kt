package me.sudodios.codewalker.ui.components.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.ui.components.Txt
import me.sudodios.codewalker.ui.theme.ColorTheme

@Composable
fun SortMenu(
    selectedItem : String,
    onClicked : (String) -> Unit
) {

    listOf("Files","Lines","Codes","Comments","Blanks").forEach {
        Row(modifier = Modifier.width(180.dp).background(if (selectedItem == it) ColorTheme.colorText.copy(0.1f) else Color.Transparent).clickable {
            onClicked.invoke(it)
        }.padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(visible = selectedItem == it) {
                Icon(modifier = Modifier.size(20.dp),
                    painter = painterResource("icons/tick-circle.svg"),
                    contentDescription = "tick-icon",
                    tint = ColorTheme.colorText.copy(0.8f)
                )
            }
            Txt(modifier = Modifier.padding(12.dp),
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = ColorTheme.colorText.copy(0.8f)
            )
        }
    }

}