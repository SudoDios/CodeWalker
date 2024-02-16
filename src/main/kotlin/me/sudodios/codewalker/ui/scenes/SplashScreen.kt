package me.sudodios.codewalker.ui.scenes

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.sudodios.codewalker.ui.Routes
import me.sudodios.codewalker.ui.theme.ColorTheme
import moe.tlaster.precompose.navigation.Navigator

@Composable
fun SplashScreen(navigator: Navigator) {

    LaunchedEffect(Unit) {
        delay(600)
        navigator.navigate(Routes.MAIN_SCREEN)
    }

    Column(Modifier.fillMaxSize().background(ColorTheme.colorPrimaryDark), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            modifier = Modifier.size(200.dp),
            painter = painterResource("icons/app-icon.png"),
            contentDescription = "app-icon"
        )
    }

}