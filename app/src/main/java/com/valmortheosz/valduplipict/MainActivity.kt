package com.valmortheosz.valduplipict

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.valmortheosz.valduplipict.core.designsystem.ValDupliPictTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ValDupliPictTheme {
                ValDupliPictApp()
            }
        }
    }
}
