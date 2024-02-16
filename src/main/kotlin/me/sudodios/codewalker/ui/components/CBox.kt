package me.sudodios.codewalker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.sudodios.codewalker.ui.theme.ColorTheme
import kotlin.math.abs

@Composable
fun CBox(
    modifier: Modifier = Modifier,
    checked : Boolean,
    bgColor : Color = ColorTheme.colorPrimary,
    iconColor : Color = ColorTheme.colorPrimaryDark,
    borderWidth : Dp = 2.dp
) {

    val animation = animateFloatAsState(if (checked) 0f else 1f, animationSpec = tween(durationMillis = 140))
    val painter = painterResource("/icons/check-icon.svg")

    Canvas(modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) {
        val validSize = size.width - borderWidth.toPx()
        drawCircle(
            color = bgColor.copy(0.6f),
            radius = validSize / 2f,
            style = Stroke(width = borderWidth.toPx())
        )
        drawCircle(
            color = bgColor,
            radius = (validSize - 1.dp.toPx()) / 2f,
            style = Fill
        )
        drawCircle(
            color = Color.Transparent,
            radius = ((validSize - 1.dp.toPx()) / 2f) * animation.value,
            blendMode = BlendMode.Clear,
            style = Fill
        )
        translate(left = size.width * .1f, top = size.height * .1f) {
            with(painter) {
                draw(size.times(.8f) * abs(animation.value - 1f), alpha = abs(animation.value - 1f), colorFilter = ColorFilter.tint(iconColor))
            }
        }
    }

}

@Composable
fun CBoxButton(
    modifier: Modifier = Modifier,
    isChecked : Boolean,
    text : String,
    enabled : Boolean = true,
    onClicked : () -> Unit
) {
    Row(modifier.clip(RoundedCornerShape(50)).clickable(enabled) { onClicked.invoke() }.padding(start = 4.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        CBox(modifier = Modifier.padding(8.dp).size(20.dp), checked = isChecked)
        Txt(
            modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp),
            text = text,
            color = ColorTheme.colorText.copy(0.8f),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp)
        )
    }
}