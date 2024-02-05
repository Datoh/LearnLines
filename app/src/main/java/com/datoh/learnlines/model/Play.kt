package com.datoh.learnlines.model

import androidx.compose.runtime.Immutable

@Immutable
data class Character(
    val name: String,
)

@Immutable
data class Line(
    val character: Character,
    val text: String,
)

@Immutable
data class Scene(
    val name: String,
    val lines: List<Line>,
)

@Immutable
data class Act(
    val name: String,
    val scenes: List<Scene>,
)

@Immutable
data class Play(
    val name: String,
    val acts: List<Act>,
)
