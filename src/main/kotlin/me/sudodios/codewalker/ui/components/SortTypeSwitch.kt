package me.sudodios.codewalker.ui.components

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.ui.theme.ColorTheme

@Composable
fun SortTypeSwitch(
    modifier: Modifier = Modifier,
    isDesc : Boolean,
    onClick : () -> Unit
) {

    val animateOffset = animateOffsetAsState(if (!isDesc) Offset(96f / 4f,24f) else Offset((96f / 4f) * 3,24f))

    Row(modifier = modifier.clip(RoundedCornerShape(50)).background(ColorTheme.colorPrimaryDark)
        .clickable {
            onClick.invoke()
        }
        .drawBehind {
            drawCircle(
                color = ColorTheme.colorPrimary.copy(0.5f),
                center = animateOffset.value,
                radius = 20f
            )
        }
    ) {
        Icon(modifier = Modifier.size(48.dp).padding(12.dp),
            painter = painterResource("icons/sort-asc.svg"),
            contentDescription = null,
            tint = ColorTheme.colorText
        )
        Icon(modifier = Modifier.size(48.dp).padding(12.dp),
            painter = painterResource("icons/sort-desc.svg"),
            contentDescription = null,
            tint = ColorTheme.colorText
        )
    }

}