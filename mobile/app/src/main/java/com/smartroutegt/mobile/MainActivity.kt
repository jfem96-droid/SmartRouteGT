package com.smartroutegt.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartroutegt.mobile.ui.screens.WelcomeScreen
import com.smartroutegt.mobile.ui.theme.SmartRouteGTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartRouteGTTheme {
                WelcomeScreen()
                }
            }
        }
    }