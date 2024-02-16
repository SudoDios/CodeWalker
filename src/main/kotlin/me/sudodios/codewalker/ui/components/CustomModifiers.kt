package me.sudodios.codewalker.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.math.min

@Composable
fun <T: Any> rememberMutableStateListOf(vararg elements: T): SnapshotStateList<T> {
    return rememberSaveable(saver = listSaver(save = { stateList ->
                if (stateList.isNotEmpty()) {
                    val first = stateList.first()
                    if (!canBeSaved(first)) {
                        throw IllegalStateException("${first::class} cannot be saved. By default only types which can be stored in the Bundle class can be saved.")
                    }
                }
                stateList.toList() }, restore = { it.toMutableStateList() })
    ) {
        elements.toList().toMutableStateList()
    }
}
@Composable
fun Modifier.clickable() = this.then(
    clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
)

fun Modifier.fadingEdges(
    scrollState: ScrollState,
    topEdgeHeight: Dp = 48.dp,
    bottomEdgeHeight: Dp = 48.dp
): Modifier = this.then(
    Modifier.graphicsLayer { alpha = 0.99F }.drawWithContent {
            drawContent()
            val topColors = listOf(Color.Transparent, Color.Black)
            val topStartY = scrollState.value.toFloat()
            val topGradientHeight = min(topEdgeHeight.toPx(), topStartY)
            drawRect(brush = Brush.verticalGradient(colors = topColors, startY = topStartY, endY = topStartY + topGradientHeight), blendMode = BlendMode.DstIn)

            val bottomColors = listOf(Color.Black, Color.Transparent)
            val bottomEndY = size.height - scrollState.maxValue + scrollState.value
            val bottomGradientHeight = min(bottomEdgeHeight.toPx(), scrollState.maxValue.toFloat() - scrollState.value)
            if (bottomGradientHeight != 0f) drawRect(
                brush = Brush.verticalGradient(colors = bottomColors, startY = bottomEndY - bottomGradientHeight, endY = bottomEndY),
                blendMode = BlendMode.DstIn
            )
        }
)

@Composable
fun Modifier.areaBlur(
    leftTop : Offset = Offset(0f,0f),
    size : Offset,
    radius : Float = 0f
) : Modifier {

    val areaBlurSksl = """
                uniform shader content;
                uniform shader blur;
            
                uniform vec4 rectangle;
                uniform float radius;
            
                float roundedRectangleSDF(vec2 position, vec2 box, float radius) {
                    vec2 q = abs(position) - box + vec2(radius);
                    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
                }
                
                vec4 main(vec2 coord) {
                    vec2 shiftRect = (rectangle.zw - rectangle.xy) / 2.0;
                    vec2 shiftCoord = coord - rectangle.xy;
                    float distanceToClosestEdge = roundedRectangleSDF(
                        shiftCoord - shiftRect, shiftRect, radius);
                        
            
                    vec4 c = content.eval(coord);
                    if (distanceToClosestEdge > 0.0) {
                        if (distanceToClosestEdge < 2.0) {
                            float darkenFactor = (2.0 - distanceToClosestEdge) / 2.0;
                            darkenFactor = pow(darkenFactor, 1.6);
                            return c * (0.9 + (1.0 - darkenFactor) / 10.0);
                        }
                        return c;
                    }
            
                    vec4 b = blur.eval(coord);
                    return b;
                }
            """

    val compositeRuntimeEffect = RuntimeEffect.makeForShader(areaBlurSksl)
    val compositeShaderBuilder = RuntimeShaderBuilder(compositeRuntimeEffect)

    val density = LocalDensity.current.density
    compositeShaderBuilder.uniform("rectangle", leftTop.x * density, leftTop.y * density, size.x * density, (size.y) * density)
    compositeShaderBuilder.uniform("radius", radius * density)

    return graphicsLayer {
        this.renderEffect =
            ImageFilter.makeRuntimeShader(
                runtimeShaderBuilder = compositeShaderBuilder,
                shaderNames = arrayOf("content", "blur"),
                inputs = arrayOf(null, ImageFilter.makeBlur(sigmaX = 13.0f, sigmaY = 13.0f, mode = FilterTileMode.CLAMP))
            ).asComposeRenderEffect()
    }

}
