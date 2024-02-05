package com.datoh.learnlines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.datoh.learnlines.ui.LearnLinesApp
import com.datoh.learnlines.ui.theme.LearnLinesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LearnLinesTheme {
                LearnLinesApp()
            }
        }
    }
}
