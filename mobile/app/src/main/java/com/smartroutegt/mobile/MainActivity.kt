package com.smartroutegt.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smartroutegt.mobile.ui.screens.WelcomeScreen
import com.smartroutegt.mobile.ui.theme.SmartRouteGTTheme
import com.smartroutegt.mobile.ui.screens.LoginScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartRouteGTTheme {
                var showLogin by remember {
                    mutableStateOf(false)
                }

                if (showLogin) {
                    LoginScreen()
                } else {
                    WelcomeScreen(
                        onContinue = {
                            showLogin = true
                        }
                    )
                }
            }
        }
    }
}