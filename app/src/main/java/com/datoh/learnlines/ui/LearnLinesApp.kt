package com.datoh.learnlines.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.datoh.learnlines.AppViewModelProvider
import com.datoh.learnlines.model.PREFS_CURRENT_NAV_KEY
import com.datoh.learnlines.model.PREFS_KEY
import com.datoh.learnlines.ui.theme.LearnLinesTheme
import com.datoh.learnlines.viewmodel.PlayViewModel
import com.datoh.learnlines.viewmodel.PlaysViewModel

@Composable
fun LearnLinesApp() {
    val prefs = LocalContext.current.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    LearnLinesTheme {
        val navController = rememberNavController()
        val playsViewModel: PlaysViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val playViewModel: PlayViewModel = viewModel(factory = AppViewModelProvider.Factory)
        NavHost(
            navController = navController,
            startDestination = prefs.getString(PREFS_CURRENT_NAV_KEY, null) ?: NavBarSections.SETTINGS.name,
        ) {
            composable(NavBarSections.READ.name) {
                Main(playViewModel, NavBarSections.READ.name, onNavigate = navController::navigate) {
                    prefs.edit().putString(PREFS_CURRENT_NAV_KEY, NavBarSections.READ.name).apply()
                    Read(LocalContext.current, playViewModel)
                }
            }
            composable(NavBarSections.LEARN.name) {
                Main(playViewModel, NavBarSections.LEARN.name, onNavigate = navController::navigate) {
                    prefs.edit().putString(PREFS_CURRENT_NAV_KEY, NavBarSections.LEARN.name).apply()
                    Learn(LocalContext.current, playViewModel)
                }
            }
//            composable(NavBarSections.QUIZ.name) {
//                Main(playViewModel, NavBarSections.QUIZ.name, onNavigate = navController::navigate) {
//                    prefs.edit().putString(PREFS_CURRENT_NAV_KEY, NavBarSections.QUIZ.name).apply()
//                    Read(playViewModel)
//                }
//            }
            composable(NavBarSections.SETTINGS.name) {
                Main(playViewModel, NavBarSections.SETTINGS.name, onNavigate = navController::navigate) {
                    prefs.edit().putString(PREFS_CURRENT_NAV_KEY, NavBarSections.SETTINGS.name).apply()
                    Settings(LocalContext.current, playsViewModel, playViewModel)
                }
            }
        }
    }
}

@Composable
fun Main(
    playViewModel: PlayViewModel,
    selectedSection: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopBar(playViewModel)
        },
        bottomBar = {
            LearnLinesNavBar(
                tabs = NavBarSections.entries.toTypedArray(),
                currentSectionName = selectedSection,
                onNavigate = onNavigate
            )
        },
    ) { paddingValues ->
        Surface(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box {
                content()
            }
        }
    }
}

@Composable
private fun TopBar(
    playViewModel: PlayViewModel,
) {
    val playName by remember { playViewModel.playName }
    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text (
            modifier = Modifier.padding(12.dp),
            text = playName ?: "")
    }
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
private fun LearnLinesAppPreview() {
    LearnLinesApp()
}
