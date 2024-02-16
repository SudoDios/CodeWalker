package me.sudodios.codewalker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import me.sudodios.codewalker.ui.theme.ColorTheme
import me.sudodios.codewalker.ui.theme.Fonts
import me.sudodios.codewalker.utils.Utils.degreeToAngle
import me.sudodios.codewalker.utils.Utils.roundPlace
import kotlin.math.cos
import kotlin.math.sin

data class LanguagesChart(var name : String, var color: Color, var data : Int)

private fun Int.calcPercentage (total : Int) : Float { return (this * 100f / total.toFloat()) / 100f }

@Composable
fun LanguagesChart(
    modifier: Modifier = Modifier,
    data : ArrayList<LanguagesChart>,
    filled : Boolean = true
) {

    var sumOfData by remember { mutableStateOf(0) }
    var inData by remember { mutableStateOf<ArrayList<LanguagesChart>>(arrayListOf()) }

    val animate = remember { Animatable(0f) }
    LaunchedEffect(data) {
        sumOfData = data.sumOf { it.data }
        inData = data
        animate.snapTo(0f)
        animate.animateTo(1f, animationSpec = tween(400))
    }

    val textMeasurer = rememberTextMeasurer()
    val textToDraw by remember { mutableStateOf("2000%") }
    val style = TextStyle(fontSize = 12.sp, fontFamily = Fonts.local_fount)
    val textLayoutResult = remember(textToDraw) {
        textMeasurer.measure(textToDraw, style)
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val radius = width / 2f
        val strokeWidth = 20.dp.toPx()

        var startAngle = -90f

        for (index in 0..inData.lastIndex) {
            val chartItem = inData[index]
            if (chartItem.data > 0) {

                val chartItemPercentage = chartItem.data.calcPercentage(sumOfData) * animate.value
                val angleInRadians = (startAngle + (chartItemPercentage * 360f) / 2).degreeToAngle

                drawArc(
                    color = chartItem.color,
                    startAngle = startAngle,
                    sweepAngle = chartItemPercentage * 360f,
                    useCenter = filled,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(width - strokeWidth, width - strokeWidth),
                    style = if (filled) Fill else Stroke(strokeWidth, cap = StrokeCap.Round)
                )

                val makeSpace = if (filled) 20.dp.toPx() else 25.dp.toPx()
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${(chartItemPercentage * 100f).roundPlace(1)}%",
                    style = style.copy(color = chartItem.color),
                    size = textLayoutResult.size.toSize(),
                    topLeft = Offset(
                        -textLayoutResult.size.width / 2 + center.x + (radius + makeSpace) * cos(
                            angleInRadians
                        ),
                        -textLayoutResult.size.height / 2 + center.y + (radius + 20.dp.toPx()) * sin(
                            angleInRadians
                        )
                    )
                )

                startAngle += (chartItemPercentage * 360f)
            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguagesDotList(
    modifier: Modifier = Modifier,
    data: ArrayList<LanguagesChart>
) {

    FlowRow(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        if (data.isNotEmpty()) {
            for (index in 0..data.lastIndex) {
                val chartItem = data[index]
                Row(Modifier.padding(8.dp),verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.size(8.dp)) {
                        drawCircle(color = chartItem.color)
                    }
                    Txt(modifier = Modifier.padding(start = 8.dp), text = chartItem.name,
                        color = ColorTheme.colorText.copy(.9f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Txt(modifier = Modifier.padding(start = 4.dp), text = chartItem.data.toString(),
                        color = ColorTheme.colorText.copy(.4f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun LineSegmentChart(
    modifier: Modifier = Modifier,
    data: ArrayList<LanguagesChart>
) {

    var sumOfData by remember { mutableStateOf(0) }
    LaunchedEffect(data) {
        sumOfData = data.sumOf { it.data }
    }

    Canvas(modifier) {
        var startOffsetX = 0f
        data.forEach {
            val endSizeX = size.width * it.data.calcPercentage(sumOfData)
            drawRect(color = it.color, topLeft = Offset(startOffsetX,0f), size = Size(endSizeX,size.height))
            startOffsetX += endSizeX
        }
    }

}