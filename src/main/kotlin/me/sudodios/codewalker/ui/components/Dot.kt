package me.sudodios.codewalker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Dot(
    modifier: Modifier,
    color: Color = Color.White
) {
    Canvas(modifier) {
        drawCircle(color)
    }
}