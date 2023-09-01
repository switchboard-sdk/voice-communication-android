package com.synervoz.onlineradioapp.ui.listener

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.synervoz.onlineradioapp.domain.CommunicationDelegate
import com.synervoz.onlineradioapp.domain.CommunicationSystem
import com.synervoz.onlineradioapp.domain.ListenerAudioSystem
import com.synervoz.onlineradioapp.ui.common.BaseViewModel
import com.synervoz.onlineradioapp.ui.common.SideEffect
import com.synervoz.onlineradioapp.ui.common.ViewState
import kotlinx.coroutines.launch

sealed class ListenerViewState : ViewState {
    object Loading : ListenerViewState()
    object Left : ListenerViewState()
    object Joined : ListenerViewState()
}

sealed class ListenerSideEffect : SideEffect {
    data class Error(val error: String) : ListenerSideEffect()
}
class ListenerViewModel(
    private val audioSystem: ListenerAudioSystem,
    private val communicationSystem: CommunicationSystem
) : BaseViewModel<ListenerViewState>(ListenerViewState.Left), CommunicationDelegate {

    init {
        communicationSystem.delegate = this
    }

    fun toggleJoin(name: String, roomID: String) {
        viewModelScope.launch {
            emitViewState(ListenerViewState.Loading)

            if (communicationSystem.joined) {
                communicationSystem.leave()
            } else {
                communicationSystem.join(name, roomID)
            }
        }
    }

    fun start() {
        audioSystem.start()
    }

    fun stop() {
        communicationSystem.leave()
        audioSystem.stop()
    }

    override fun joined() {
        viewModelScope.launch {
            emitViewState(ListenerViewState.Joined)
        }
    }

    override fun left() {
        viewModelScope.launch {
            emitViewState(ListenerViewState.Left)
        }
    }

    override fun receivedError(error: Error) {
        viewModelScope.launch {
            emitViewState(ListenerViewState.Left)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val communicationSystem = CommunicationSystem(application.applicationContext, isHost = false)
                val audioSystem = ListenerAudioSystem(communicationSystem.roomManager)
                ListenerViewModel(
                    audioSystem = audioSystem,
                    communicationSystem = communicationSystem
                )
            }
        }
    }
}