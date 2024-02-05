package com.datoh.learnlines.model

fun playParser(content: String) : Play {
    val characters = mutableSetOf<Character>()
    val acts = mutableListOf<Act>()
    var scenes = mutableListOf<Scene>()
    var lines = mutableListOf<Line>()
    var playName = ""
    var actName = ""
    var sceneName = ""
    var line = ""
    var character = Character("")
    val contentLines = content.split("\n").map { it.trim() }
    val stageDirectionBeginSeparator = "("
    val stageDirectionEndSeparator = ")"
    val playNameSeparator = "#"
    val actNameSeparator = "##"
    val sceneNameSeparator = "###"
    val characterNameSeparator = ':'

    fun finishLine() {
        if (character.name.isNotEmpty()) {
            lines.add(Line(character, line))
            line = ""
            character = Character("")
        }
    }
    fun finishScene() {
        if (sceneName.isNotEmpty()) {
            scenes.add(Scene(sceneName, lines))
            lines = mutableListOf()
        }
    }
    fun finishAct() {
        if (actName.isNotEmpty()) {
            acts.add(Act(actName, scenes))
            scenes = mutableListOf()
            sceneName = ""
        }
    }
    contentLines.forEachIndexed { contentLineIndex, contentLine ->
        if (contentLine.startsWith(stageDirectionBeginSeparator)) {
            if (!contentLine.endsWith(stageDirectionEndSeparator))
                throw IllegalArgumentException("Invalid stage direction at line $contentLineIndex: $contentLine")
            line = if (line.isEmpty()) contentLine else (line + System.lineSeparator() + contentLine)
        }
        else if (contentLine.startsWith(playNameSeparator) && !contentLine.startsWith(actNameSeparator) && !contentLine.startsWith(sceneNameSeparator)) {
            if (playName.isNotEmpty())
                throw IllegalArgumentException("Play name declared twice at line $contentLineIndex: $contentLine")
            playName = contentLine.substring(playNameSeparator.length).trim()
        }
        else if (contentLine.startsWith(actNameSeparator) && !contentLine.startsWith(sceneNameSeparator)) {
            if (playName.isEmpty())
                throw IllegalArgumentException("Act is declared before play name at line $contentLineIndex: $contentLine")
            finishLine()
            finishScene()
            if (actName.isNotEmpty() && scenes.isEmpty())
                throw IllegalArgumentException("Illegal empty act: $actName")
            finishAct()
            actName = contentLine.substring(actNameSeparator.length).trim()
        }
        else if (contentLine.startsWith(sceneNameSeparator)) {
            if (actName.isEmpty())
                throw IllegalArgumentException("Scene is declared before act name at line $contentLineIndex: $contentLine")
            finishLine()
            if (sceneName.isNotEmpty() && lines.isEmpty())
                throw IllegalArgumentException("Illegal empty scene: $sceneName ($actName)")
            finishScene()
            sceneName = contentLine.substring(sceneNameSeparator.length).trim()
        }
        else {
            val characterNameSeparatorIndex = contentLine.indexOfFirst { it == characterNameSeparator }
            if (characterNameSeparatorIndex < 0)
                throw IllegalArgumentException("Invalid character name at line $contentLineIndex: $contentLine")
            val characterName = contentLine.substring(0, characterNameSeparatorIndex).trim()
            val contentLineTrimmed = contentLine.substring(characterNameSeparatorIndex + 1).trim()
            if (contentLineTrimmed.isEmpty())
                throw IllegalArgumentException("Invalid line at line $contentLineIndex: $contentLine")
            val lineCharacter = characters.firstOrNull { it.name == characterName } ?: Character(characterName)
            characters.add(lineCharacter)
            finishLine()
            character = lineCharacter
            line = if (line.isEmpty()) contentLineTrimmed else (line + System.lineSeparator() + contentLineTrimmed)
        }
    }
    finishLine()
    finishScene()
    finishAct()
    return Play(playName, acts)
}
