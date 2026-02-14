package com.itechsolution.mufasapay

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.itechsolution.mufasapay.ui.navigation.NavigationHost
import com.itechsolution.mufasapay.ui.theme.MufasaPayTheme
import com.itechsolution.mufasapay.ui.theme.background

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MufasaPayTheme(dynamicColor = false) {
                SetSystemBars()
                NavigationHost()
            }
        }
    }
}


@Composable
fun SetSystemBars() {
    val view = LocalView.current
    val window = (view.context as Activity).window
    SideEffect {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor =background.toArgb()
        window.statusBarColor =  background.toArgb()
        WindowCompat.getInsetsController(window, view)
            .isAppearanceLightNavigationBars = true
    }
}

