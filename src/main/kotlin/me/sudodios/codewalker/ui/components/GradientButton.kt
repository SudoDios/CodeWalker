package me.sudodios.codewalker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.ui.theme.ColorTheme

@Composable
fun GradientButton(
    modifier: Modifier = Modifier,
    text : String,
    onClicked : () -> Unit
) {

    Row(modifier = modifier.height(36.dp).clip(RoundedCornerShape(50))
        .background(brush = Brush.linearGradient(colorStops = arrayOf(
            0.0f to ColorTheme.colorPrimary,
            0.5f to ColorTheme.colorPrimary2,
            1.0f to ColorTheme.colorPrimary3
        ))).clickable {
        onClicked.invoke()
    }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        Txt(text = text, color = ColorTheme.colorPrimaryDark, style = MaterialTheme.typography.bodyMedium)
    }

}