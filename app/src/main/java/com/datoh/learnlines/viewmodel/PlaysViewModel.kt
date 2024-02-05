package com.datoh.learnlines.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datoh.learnlines.model.PlayItem
import com.datoh.learnlines.model.PlaysRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaysViewModel(
    private val playsRepository: PlaysRepository,
) : ViewModel() {

    val plays: StateFlow<List<String>> = playsRepository.getAllPlaysNameStream().filterNotNull()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
}