package me.sudodios.codewalker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.ui.theme.ColorTheme

@Composable
fun TooltipC(
    text : String
) {
    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(ColorTheme.colorText.copy(0.2f)).padding(8.dp)) {
        Txt(text = text, color = ColorTheme.colorText, style = MaterialTheme.typography.labelMedium)
    }
}