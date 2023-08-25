package com.synervoz.voicecommunicationapp.ui.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

interface ViewState
interface SideEffect

abstract class BaseViewModel<VS: ViewState>(initialState: ViewState) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _sideEffect = MutableSharedFlow<SideEffect>(replay = 1)
    val sideEffect = _sideEffect.asSharedFlow()

    fun emitViewState(state: VS) {
        _state.update { state }
    }

    suspend fun emitSideEffect(sideEffect: SideEffect) {
        _sideEffect.emit(sideEffect)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun receiveSideEffect(sideEffect: SideEffect, action: (SideEffect) -> Unit) {
        _sideEffect.resetReplayCache()
        action(sideEffect)
    }
}
