package com.youme24.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.youme24.app.ui.navigation.YouMeNavGraph
import com.youme24.app.ui.theme.YouMeTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity entry point for the YouMe app.
 * Edge-to-edge enabled; all UI is Compose with a single NavHost.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YouMeTheme {
                YouMeNavGraph()
            }
        }
    }
}
