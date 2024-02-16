package me.sudodios.codewalker

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import me.sudodios.codewalker.core.Global
import me.sudodios.codewalker.core.LibCore
import me.sudodios.codewalker.ui.Routes
import me.sudodios.codewalker.ui.dialogs.BaseDialog
import me.sudodios.codewalker.ui.scenes.MainScreen
import me.sudodios.codewalker.ui.scenes.SplashScreen
import me.sudodios.codewalker.ui.theme.AppRippleTheme
import me.sudodios.codewalker.ui.theme.ColorTheme
import me.sudodios.codewalker.ui.theme.Fonts
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import java.awt.Dimension

@Composable
fun App() {

    /*init core*/
    LibCore.init()
    LibCore.initDB("${Global.DB_CORE_PATH}/code_walker.db")

    /*UI*/
    PreComposeApp {

        val navigator = rememberNavigator()

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    surface = ColorTheme.colorCard1,
                    primary = ColorTheme.colorPrimary,
                    primaryContainer = ColorTheme.colorPrimaryDark
                ),
                typography = Fonts.getTypography()
            ) {
                CompositionLocalProvider(LocalRippleTheme provides AppRippleTheme) {
                    NavHost(
                        navigator = navigator,
                        navTransition = NavTransition(),
                        initialRoute = Routes.SPLASH_SCREEN
                    ) {
                        scene(
                            route = Routes.SPLASH_SCREEN,
                            navTransition = NavTransition()
                        ) {
                            SplashScreen(navigator)
                        }
                        scene(
                            route = Routes.MAIN_SCREEN,
                            navTransition = NavTransition()
                        ) {
                            MainScreen(navigator)
                        }
                    }
                }

                //loading dialog
                BaseDialog(
                    expanded = Global.Alert.openLoading.value,
                    backgroundColor = ColorTheme.colorCard2
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(20.dp).size(48.dp),
                        strokeCap = StrokeCap.Round,
                        strokeWidth = 3.dp
                    )
                }
            }
        }

    }
}

fun main() = application {
    Window(
        title = "Code Walker",
        onCloseRequest = ::exitApplication
    ) {
        window.minimumSize = Dimension(920,640)
        val icon = painterResource("icons/app-icon.png")
        val density = LocalDensity.current
        SideEffect {
            window.iconImage = icon.toAwtImage(density, LayoutDirection.Ltr, Size(192f,192f))
        }
        App()
    }
}
