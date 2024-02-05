package com.datoh.learnlines

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.datoh.learnlines.viewmodel.PlayViewModel
import com.datoh.learnlines.viewmodel.PlaysViewModel


/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            PlaysViewModel(
                playApplication().container.playsRepository,
            )
        }
        initializer {
            PlayViewModel(
                playApplication().container.context,
                playApplication().container.playsRepository,
                playApplication().container.playInfoRepository,
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [LearnLinesApplication].
 */
fun CreationExtras.playApplication(): LearnLinesApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as LearnLinesApplication)
