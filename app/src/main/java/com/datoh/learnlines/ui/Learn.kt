package com.datoh.learnlines.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.datoh.learnlines.AppViewModelProvider
import com.datoh.learnlines.R
import com.datoh.learnlines.model.Act
import com.datoh.learnlines.model.PREFS_KEY
import com.datoh.learnlines.model.PREFS_LEARN_SCROLL_POSITION_KEY
import com.datoh.learnlines.model.Scene
import com.datoh.learnlines.ui.theme.LearnLinesTheme
import com.datoh.learnlines.viewmodel.PlayViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun Learn(
    context: Context,
    playViewModel: PlayViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by lazy {
        context.getSharedPreferences(PREFS_KEY, MODE_PRIVATE)
    }
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = prefs.getInt(PREFS_LEARN_SCROLL_POSITION_KEY, 0)
    )

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex
        }
            .debounce(500L)
            .collectLatest { index ->
                prefs.edit()
                    .putInt(PREFS_LEARN_SCROLL_POSITION_KEY, index)
                    .apply()
            }
    }

    val scrollIndex by playViewModel.getScrollFlow().collectAsState(0, Dispatchers.Default)
    LaunchedEffect(scrollIndex) {
        lazyListState.animateScrollToItem(scrollIndex)
    }

    val me by remember { playViewModel.me }
    val linesCurrentActScene by remember { playViewModel.linesCurrentActScene }
    val hasNextCurrentLine by remember { playViewModel.hasNextCurrentLine }
    val hasNextCurrentActScene by remember { playViewModel.hasNextCurrentActScene }

    Box(modifier.fillMaxSize()) {
        if (linesCurrentActScene.isEmpty())
            Box(modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.learn_empty), textAlign = TextAlign.Center)
            }
        else
            Column(modifier) {
                LazyColumn(state = lazyListState, modifier = modifier.weight(1f)) {
                    linesCurrentActScene.forEach { (pairActScene, lines) ->
                        stickyHeader {
                            ActSceneHeader(
                                playViewModel,
                                pairActScene.first,
                                pairActScene.second,
                                filterByMe = true
                            ) { act: Act, scene: Scene ->
                                playViewModel.setLearnActScene(act, scene)
                            }
                        }

                        items(lines) { line ->
                            Line(
                                playViewModel = playViewModel,
                                line = line,
                                isUserMe = me == line.character
                            )
                        }
                    }
                }
                Row (modifier = modifier.fillMaxWidth().padding(8.dp)) {
                    val modifierReset = if (hasNextCurrentLine || hasNextCurrentActScene) modifier else modifier.fillMaxWidth()
                    Button(
                        modifier = modifierReset,
                        onClick = {
                            playViewModel.resetLearnActScene()
                        }) {
                        Text(stringResource(R.string.reset))
                    }
                    if (hasNextCurrentLine) {
                        Button(
                            modifier = modifier.weight(1f).padding(start = 8.dp),
                            onClick = {
                                playViewModel.nextLearnLine()
                            }) {
                            Text(stringResource(R.string.next))
                        }
                    } else if (hasNextCurrentActScene)
                        Button(
                            modifier = modifier.weight(1f).padding(start = 8.dp),
                            onClick = {
                                playViewModel.nextLearnActScene()
                            }) {
                            Text(stringResource(R.string.next_scene))
                        }
                }
        }
    }
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun LearnPreview() {
    LearnLinesTheme {
        val playViewModel: PlayViewModel = viewModel(factory = AppViewModelProvider.Factory)
        Learn(LocalContext.current, playViewModel)
    }
}
