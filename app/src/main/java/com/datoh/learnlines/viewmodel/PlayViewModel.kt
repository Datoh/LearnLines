package com.datoh.learnlines.viewmodel

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datoh.learnlines.model.Act
import com.datoh.learnlines.model.Character
import com.datoh.learnlines.model.PlayInfoItem
import com.datoh.learnlines.model.PlayInfoRepository
import com.datoh.learnlines.model.PlayItem
import com.datoh.learnlines.model.PlaysRepository
import com.datoh.learnlines.model.Line
import com.datoh.learnlines.model.PREFS_CURRENT_PLAY_KEY
import com.datoh.learnlines.model.PREFS_KEY
import com.datoh.learnlines.model.Scene
import com.datoh.learnlines.model.playParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

@Immutable
data class LinePosition(
    val actSceneIndex: Int = 0,
    var lineIndex: Int = -1,
)

private val character_color_1 = Color.Blue
private val character_color_3 = Color.Green
private val character_color_4 = Color.Cyan
private val character_color_2 = Color.Red
private val character_color_5 = Color.DarkGray
private val character_color_6 = Color.Black
private val character_color_7 = Color.Yellow
private val character_color_8 = Color.LightGray
private val characters_color = listOf(character_color_1, character_color_2, character_color_3, character_color_4, character_color_5, character_color_6, character_color_7, character_color_8)

class PlayViewModel(
    context : Context,
    private val playsRepository: PlaysRepository,
    private val playInfoRepository: PlayInfoRepository,
) : ViewModel() {
    private val charactersColor = mutableMapOf<Character, Color>()

    private var learnPosition: LinePosition = LinePosition()

    private val scrollFlow = MutableStateFlow(0)
    fun getScrollFlow(): StateFlow<Int> = scrollFlow.asStateFlow()

    val playName: MutableState<String?> = mutableStateOf(null)
    val characters: MutableState<Set<Character>> = mutableStateOf(emptySet())
    val lines: MutableState<List<Pair<Pair<Act, Scene>, List<Line>>>> = mutableStateOf(emptyList())
    val actScenes: MutableState<List<Pair<Act, Scene>>> = mutableStateOf(emptyList())

    val me: MutableState<Character?> = mutableStateOf(null)
    val myActScenes: MutableState<List<Pair<Act, Scene>>> = mutableStateOf(emptyList())
    val linesCurrentActScene: MutableState<List<Pair<Pair<Act, Scene>, List<Line>>>> = mutableStateOf(emptyList())
    val hasNextCurrentActScene: MutableState<Boolean> = mutableStateOf(false)
    val hasNextCurrentLine: MutableState<Boolean> = mutableStateOf(false)

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    }

    companion object {
        fun text(act: Act, scene: Scene) =
            "${act.name} / ${scene.name}"
    }

    init {
        viewModelScope.launch {
            val name = prefs.getString(PREFS_CURRENT_PLAY_KEY, null)
            load(name)
        }
    }

    fun insertAndSetPlay(name: String, content: String) {
        viewModelScope.launch {
            val oldPlayItem = playsRepository.getPlay(name)
            if (oldPlayItem != null)
                playsRepository.deletePlay(oldPlayItem)
            playsRepository.insertPlay(PlayItem(name, content))
            setPlay(name)
        }
    }

    fun setPlay(name: String?) {
        viewModelScope.launch {
            load(name)
            save()
        }
    }

    fun clearPlay() {
        viewModelScope.launch {
            playsRepository.deletePlay(PlayItem(name = playName.value!!))
            prefs.edit().remove(PREFS_CURRENT_PLAY_KEY).apply()
            viewModelScope.launch {
                learnPosition = LinePosition()

                playName.value = null
                characters.value = emptySet()
                lines.value = emptyList()
                actScenes.value = emptyList()

                me.value = null
                myActScenes.value = emptyList()
                linesCurrentActScene.value = emptyList()
                hasNextCurrentActScene.value = false
                hasNextCurrentLine.value = false
            }
        }
    }

    fun setMe(character: Character?) {
        viewModelScope.launch {
            me.value = character
            myActScenes.value = actScenes.value.filter { actScene -> actScene.second.lines.any { scene -> scene.character == me.value } }
            setLearnActSceneIndex(0)
            save()
        }
    }

    fun setLearnActScene(act: Act, scene: Scene) {
        viewModelScope.launch {
            setLearnActSceneIndex(myActScenes.value.indexOf(Pair(act, scene)))
            save()
        }
    }

    fun resetLearnActScene() {
        viewModelScope.launch {
            setLearnActSceneIndex(learnPosition.actSceneIndex)
            save()
        }
    }

    fun nextLearnActScene() {
        viewModelScope.launch {
            var nextActSceneIndex = learnPosition.actSceneIndex
            nextActSceneIndex = min(nextActSceneIndex + 1, myActScenes.value.size - 1)
            setLearnActSceneIndex(nextActSceneIndex)
            save()
        }
    }

    fun nextLearnLine() {
        viewModelScope.launch {
            val lineIndex = setNextLearnLine()
            scrollFlow.value = if (lineIndex >= 0) lineIndex else scrollFlow.value
            save()
        }
    }

    fun color(character: Character) =
        charactersColor[character]

    fun indexOfFirstLine(act: Act, scene: Scene): Int {
        var count = 0
        lines.value.forEach {
            if (it.first.first == act && it.first.second == scene)
                return count
            count += it.second.size + 1
        }
        return -1
    }

    private suspend fun load(name: String?) {
        playName.value = null
        val playItem = playsRepository.getPlay(name ?: "") ?: return
        playName.value = name
        val play = playParser(playItem.content)
        characters.value = play.acts.map { itAct ->
            itAct.scenes.map { itScene ->
                itScene.lines.map { it.character }.toSet()
            }.flatten().toSet()
        }.flatten().toSet()
        lines.value = play.acts.map { itAct ->
            itAct.scenes.associate { itScene ->
                Pair(
                    Pair(
                        itAct,
                        itScene
                    ), itScene.lines
                )
            }
        }.flatMap { it.toList() }
        actScenes.value =
            play.acts.map { itAct -> itAct.scenes.map { itScene -> Pair(itAct, itScene) } }
                .flatten()

        val playInfoItem = playInfoRepository.getPlayInfo(playName.value!!)

        me.value = characters.value.firstOrNull { it.name == playInfoItem?.character }
        myActScenes.value = actScenes.value.filter { actScene -> actScene.second.lines.any { scene -> scene.character == me.value } }

        learnPosition = LinePosition(playInfoItem?.currentLearnInfoActSceneIndex ?: 0, playInfoItem?.currentLearnInfoLineIndex ?: -1)
        setNextLearnLine()

        var colorIndex = 0
        characters.value.toList().sortedBy { it.name }.forEach {
            colorIndex = if (colorIndex >= characters_color.size) 0 else colorIndex
            charactersColor[it] = characters_color[colorIndex++]
        }
    }

    private fun setLearnActSceneIndex(actSceneIndex: Int) {
        learnPosition = LinePosition(actSceneIndex)
        val lineIndex = setNextLearnLine()
        hasNextCurrentActScene.value = actSceneIndex >= 0 && actSceneIndex + 1 < myActScenes.value.size
        scrollFlow.value = if (lineIndex >= 0) lineIndex else scrollFlow.value
    }

    private fun setNextLearnLine(): Int {
        if (myActScenes.value.isEmpty())
            return 0

        val (act, scene) = myActScenes.value[learnPosition.actSceneIndex]

        val oldLineIndex = learnPosition.lineIndex
        val offsetIndex =
            scene.lines.filterIndexed { index, _ -> index > learnPosition.lineIndex }
                .indexOfFirst { it.character == me.value }
        learnPosition.lineIndex =
            if (offsetIndex < 0) scene.lines.size else (learnPosition.lineIndex + offsetIndex + 1)
        val linesFiltered = scene.lines.subList(0, learnPosition.lineIndex)
        linesCurrentActScene.value = listOf(Pair(Pair(act, scene), linesFiltered))
        hasNextCurrentLine.value = linesFiltered.size < scene.lines.size
        return oldLineIndex
    }

    private suspend fun save() {
        prefs.edit().putString(PREFS_CURRENT_PLAY_KEY, playName.value!!).apply()
        val playInfoItem = PlayInfoItem(
            name = playName.value!!,
            character = me.value?.name,
            currentLearnInfoActSceneIndex = learnPosition.actSceneIndex,
            currentLearnInfoLineIndex = learnPosition.lineIndex - 1,
            )
        playInfoRepository.insertPlayInfo(playInfoItem)
    }
}