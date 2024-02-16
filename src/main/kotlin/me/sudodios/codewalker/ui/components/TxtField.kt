package me.sudodios.codewalker.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import me.sudodios.codewalker.ui.theme.ColorTheme
import me.sudodios.codewalker.ui.theme.Fonts

@Composable
fun TxtField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    enabled : Boolean = true,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle = LocalTextStyle.current,
    colors : TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedLabelColor = ColorTheme.colorPrimary,
        disabledTextColor = ColorTheme.colorText.copy(0.8f),
        disabledLabelColor = ColorTheme.colorText.copy(0.4f),
        disabledBorderColor = ColorTheme.colorText.copy(0.4f)
    )
) {
    OutlinedTextField(modifier = modifier, enabled = enabled,
        label = {
            Txt(text = label)
        },
        singleLine = true, value = value, onValueChange = onValueChange,
        shape = RoundedCornerShape(16.dp),
        textStyle = textStyle.copy(fontFamily = Fonts.local_fount),
        colors = colors
    )
}