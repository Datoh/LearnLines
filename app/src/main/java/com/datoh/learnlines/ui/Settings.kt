package com.datoh.learnlines.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.datoh.learnlines.AppViewModelProvider
import com.datoh.learnlines.R
import com.datoh.learnlines.model.playParser
import com.datoh.learnlines.ui.theme.LearnLinesTheme
import com.datoh.learnlines.viewmodel.PlayViewModel
import com.datoh.learnlines.viewmodel.PlaysViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    context: Context,
    playsViewModel: PlaysViewModel,
    playViewModel: PlayViewModel,
    modifier: Modifier = Modifier
) {
    var expandedPlay by remember { mutableStateOf(false) }
    var expandedCharacter by remember { mutableStateOf(false) }
    val playName by remember { mutableStateOf(playViewModel.playName) }
    val me by remember { mutableStateOf(playViewModel.me) }
    val plays by remember { mutableStateOf(playsViewModel.plays) }

    val openDeletionDialog = remember { mutableStateOf(false) }
    val openErrorImportDialog = remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri == null)
                return@rememberLauncherForActivityResult
            var inputStream: InputStream? = null
            var reader: BufferedReader? = null
            try {
                openErrorImportDialog.value = null
                inputStream = context.contentResolver.openInputStream(uri)
                reader = inputStream?.bufferedReader()
                val content = reader?.use { it.readText() }
                if (content != null) {
                    val play = playParser(content)
                    playViewModel.insertAndSetPlay(play.name, content)
                }
            } catch (e: Exception) {
                openErrorImportDialog.value = "An error occurred: ${e.message}"
            } finally {
                try {
                    reader?.close()
                    inputStream?.close()
                } catch (e: Exception) {
                    println("An error occurred while closing the file: ${e.message}")
                }
            }
        }
    )

    if (!openErrorImportDialog.value.isNullOrEmpty()) {
        AlertDialog(
            title = { Text(text = stringResource(R.string.settings_dialog_import_failed_title)) },
            text = { Text(text = openErrorImportDialog.value!!) },
            confirmButton = {
                TextButton(
                    onClick = { openErrorImportDialog.value = null }
                ) {
                    Text(stringResource(R.string.settings_dialog_import_failed_ok))
                }
            },
            onDismissRequest = { openErrorImportDialog.value = null }
        )
    }

    if (openDeletionDialog.value) {
        AlertDialog(
            title = { Text(text = stringResource(R.string.settings_dialog_delete_play_title)) },
            text = { Text(text = stringResource(R.string.settings_dialog_delete_play_text, playName.value!!)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        playViewModel.clearPlay()
                        openDeletionDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.settings_dialog_delete_play_yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDeletionDialog.value = false }
                ) {
                    Text(stringResource(R.string.settings_dialog_delete_play_no))
                }
            },
            onDismissRequest = { openDeletionDialog.value = false }
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Column {
            ExposedDropdownMenuBox(
                modifier = Modifier.padding(bottom = 16.dp),
                expanded = expandedPlay,
                onExpandedChange = {
                    expandedPlay = !expandedPlay
                }
            ) {
                Column {
                    Text(
                        stringResource(id = R.string.settings_play),
                        modifier = Modifier.padding(8.dp)
                    )

                    TextField(
                        value = playName.value ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { TrailingIcon(expanded = expandedPlay) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedPlay,
                        onDismissRequest = { expandedPlay = false }
                    ) {
                        plays.value.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item) },
                                onClick = {
                                    expandedPlay = false
                                    playViewModel.setPlay(item)
                                }
                            )
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(
                modifier = Modifier.padding(bottom = 16.dp),
                expanded = expandedCharacter,
                onExpandedChange = {
                    expandedCharacter = !expandedCharacter
                }
            ) {
                Column {
                    Text(
                        stringResource(id = R.string.settings_character),
                        modifier = Modifier.padding(8.dp)
                    )

                    TextField(
                        value = me.value?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { TrailingIcon(expanded = expandedCharacter) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCharacter,
                        onDismissRequest = { expandedCharacter = false }
                    ) {
                        playViewModel.characters.value.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item.name) },
                                onClick = {
                                    expandedCharacter = false
                                    playViewModel.setMe(item)
                                }
                            )
                        }
                    }
                }
            }
            Button(
                modifier = modifier.fillMaxWidth().padding(bottom = 16.dp, top = 32.dp),
                onClick = { filePicker.launch("text/plain") }) {
                Text(stringResource(R.string.settings_import_play))
            }
            if (!playName.value.isNullOrEmpty()) {
                Button(
                    modifier = modifier.fillMaxWidth().padding(bottom = 16.dp),
                    onClick = { openDeletionDialog.value = true}) {
                    Text(stringResource(R.string.settings_delete_play))
                }
            }
        }
    }
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f, showBackground = true)
@Composable
fun SettingsPreview() {
    LearnLinesTheme {
        val playsViewModel: PlaysViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val playViewModel: PlayViewModel = viewModel(factory = AppViewModelProvider.Factory)
        Settings(LocalContext.current, playsViewModel, playViewModel)
    }
}
