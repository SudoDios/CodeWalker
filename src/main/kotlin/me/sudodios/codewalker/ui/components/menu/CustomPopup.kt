package me.sudodios.codewalker.ui.components.menu

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.rememberCursorPositionProvider
import me.sudodios.codewalker.ui.theme.ColorTheme

@Suppress("ModifierParameter")
@Composable
internal fun DropdownMenuContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    modifier: Modifier = Modifier,
    extraPadding: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    // Menu open/close animation.
    val transition = updateTransition(expandedStates, "DropDownMenu")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(durationMillis = 200, easing = LinearOutSlowInEasing)
            } else {
                tween(durationMillis = 200, easing = LinearOutSlowInEasing)
            }
        }
    ) {
        if (it) { 1f } else { 0.8f }
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) { tween(durationMillis = 120) } else {
                tween(durationMillis = 120)
            }
        }
    ) {
        if (it) { 1f } else { 0f }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
        transformOrigin = transformOriginState.value
    }.shadow(3.dp, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp)).background(ColorTheme.colorCard1)) {
        Box(modifier = modifier) {
            Column(
                modifier = modifier.padding(vertical = if (extraPadding) 8.dp else 0.dp)
                    .width(IntrinsicSize.Max)
                    .verticalScroll(scrollState),
                content = content
            )
            if (scrollState.maxValue > 0) {
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    style = LocalScrollbarStyle.current
                        .copy(thickness = 4.dp, hoverColor = ColorTheme.colorText.copy(0.6f), unhoverColor = ColorTheme.colorText.copy(0.1f)),
                    adapter = rememberScrollbarAdapter(scrollState)
                )
            }
        }
    }
}

@Composable
private fun OpenDropdownMenu(
    expandedStates: MutableTransitionState<Boolean>,
    popupPositionProvider: PopupPositionProvider,
    transformOriginState: MutableState<TransformOrigin> =
        remember { mutableStateOf(TransformOrigin.Center) },
    onDismissRequest: () -> Unit,
    focusable: Boolean = true,
    modifier: Modifier = Modifier,
    extraPadding: Boolean,
    content: @Composable ColumnScope.() -> Unit
){
    var focusManager: FocusManager? by mutableStateOf(null)
    var inputModeManager: InputModeManager? by mutableStateOf(null)
    Popup(
        properties = PopupProperties(focusable = focusable),
        onDismissRequest = onDismissRequest,
        popupPositionProvider = popupPositionProvider,
    ) {
        focusManager = LocalFocusManager.current
        inputModeManager = LocalInputModeManager.current

        DropdownMenuContent(
            expandedStates = expandedStates,
            transformOriginState = transformOriginState,
            modifier = modifier,
            extraPadding = extraPadding,
            content = content
        )
    }
}

@Composable
fun CustomDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    focusable: Boolean = true,
    modifier: Modifier = Modifier,
    extraPadding : Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded

    if (expanded) {
        Popup(
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset = IntOffset.Zero
            },
            properties = PopupProperties(focusable = true),
            onDismissRequest = {},
        ) {
            Box(
                Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onDismissRequest.invoke()
                    })
        }
    }

    if (expandedStates.currentState || expandedStates.targetState) {
        OpenDropdownMenu(
            expandedStates = expandedStates,
            popupPositionProvider = rememberCursorPositionProvider(),
            onDismissRequest = onDismissRequest,
            focusable = focusable,
            modifier = modifier,
            extraPadding = extraPadding,
            content = content
        )
    }
}