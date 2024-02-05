package com.datoh.learnlines.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.datoh.learnlines.AppViewModelProvider
import com.datoh.learnlines.R
import com.datoh.learnlines.model.Act
import com.datoh.learnlines.model.Line
import com.datoh.learnlines.model.PREFS_KEY
import com.datoh.learnlines.model.PREFS_READ_SCROLL_POSITION_KEY
import com.datoh.learnlines.model.Scene
import com.datoh.learnlines.ui.theme.LearnLinesTheme
import com.datoh.learnlines.viewmodel.PlayViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, FlowPreview::class)
@Composable
fun Read(
    context: Context,
    playViewModel: PlayViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by lazy {
        context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    }

    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = prefs.getInt(PREFS_READ_SCROLL_POSITION_KEY, 0)
    )

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex
        }
            .debounce(500L)
            .collectLatest { index ->
                prefs.edit()
                    .putInt(PREFS_READ_SCROLL_POSITION_KEY, index)
                    .apply()
            }
    }

    val coroutineScope = rememberCoroutineScope()
    val me by remember { playViewModel.me }
    val lines by remember { playViewModel.lines }

    if (lines.isEmpty())
        Box(modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(id = R.string.read_empty), textAlign = TextAlign.Center)
        }
    else
        Box {
            LazyColumn(state = lazyListState, modifier = modifier) {
                lines.forEach { (pairActScene, lines) ->
                    stickyHeader {
                        ActSceneHeader(
                            playViewModel,
                            pairActScene.first,
                            pairActScene.second,
                            filterByMe = false) { act: Act, scene: Scene ->
                                coroutineScope.launch {
                                    val index = playViewModel.indexOfFirstLine(act, scene)
                                    lazyListState.animateScrollToItem(index)
                                }
                            }
                    }

                    items(lines) { line ->
                        Line(playViewModel = playViewModel, line = line, isUserMe = me == line.character)
                    }
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActSceneHeader(
    playViewModel: PlayViewModel,
    act: Act,
    scene: Scene,
    filterByMe: Boolean,
    modifier: Modifier = Modifier,
    onActSceneClick: (Act, Scene) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val allActScenes by remember { playViewModel.actScenes }
    val myActScenes by remember { playViewModel.myActScenes }
    val actScenes = if (filterByMe) myActScenes else allActScenes

    ElevatedCard (
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface,
        ),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            text = PlayViewModel.text(act, scene),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        DropdownMenu(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            actScenes.forEach {
                DropdownMenuItem(
                    text = { Text(PlayViewModel.text(it.first, it.second)) },
                    onClick = {
                        expanded = false
                        onActSceneClick(it.first, it.second)
                    }
                )
            }
        }
    }
}

@Composable
fun Line(
    playViewModel: PlayViewModel,
    line: Line,
    isUserMe: Boolean = false,
    isFirstMessageByAuthor: Boolean = true,
    isLastMessageByAuthor: Boolean = true
) {
    val spaceBetweenAuthors = if (isLastMessageByAuthor) Modifier.padding(top = 8.dp) else Modifier
    Row(modifier = spaceBetweenAuthors) {
        if (isLastMessageByAuthor) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                tint = playViewModel.color(line.character) ?: LocalContentColor.current,
                contentDescription = line.character.name,
                modifier = Modifier
                    .padding(end = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(74.dp))
        }
        AuthorAndTextMessage(
            line = line,
            isUserMe = isUserMe,
            isFirstMessageByAuthor = isFirstMessageByAuthor,
            isLastMessageByAuthor = isLastMessageByAuthor,
            modifier = Modifier
                .padding(end = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
fun AuthorAndTextMessage(
    line: Line,
    isUserMe: Boolean,
    isFirstMessageByAuthor: Boolean,
    isLastMessageByAuthor: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (isLastMessageByAuthor) {
            AuthorName(line)
        }
        ChatItemBubble(line, isUserMe)
        if (isFirstMessageByAuthor) {
            // Last bubble before next author
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            // Between bubbles
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun AuthorName(line: Line) {
    Row(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        Text(
            text = line.character.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .alignBy(LastBaseline)
                .paddingFrom(LastBaseline, after = 8.dp) // Space to 1st bubble
        )
    }
}

private val ChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

@Composable
fun ChatItemBubble(
    line: Line,
    isUserMe: Boolean
) {
    val backgroundBubbleColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column {
        Surface(
            color = backgroundBubbleColor,
            shape = ChatBubbleShape
        ) {
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun LinePreview() {
    LearnLinesTheme {
        val playViewModel: PlayViewModel = viewModel(factory = AppViewModelProvider.Factory)
        val line = playViewModel.lines.value.first().second.first()
        Line(playViewModel, line)
    }
}

@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun ReadPreview() {
    LearnLinesTheme {
        val playViewModel: PlayViewModel = viewModel(factory = AppViewModelProvider.Factory)
        Read(LocalContext.current, playViewModel)
    }
}
