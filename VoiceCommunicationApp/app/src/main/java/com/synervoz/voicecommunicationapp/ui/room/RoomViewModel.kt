package com.synervoz.voicecommunicationapp.ui.room

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.synervoz.voicecommunicationapp.domain.AudioSystem
import com.synervoz.voicecommunicationapp.domain.CommunicationDelegate
import com.synervoz.voicecommunicationapp.domain.CommunicationSystem
import com.synervoz.voicecommunicationapp.ui.common.BaseViewModel
import com.synervoz.voicecommunicationapp.ui.common.SideEffect
import com.synervoz.voicecommunicationapp.ui.common.ViewState
import kotlinx.coroutines.launch

sealed class RoomViewState : ViewState {
    object Loading : RoomViewState()
    object Left : RoomViewState()
    object Joined : RoomViewState()
}

sealed class RoomSideEffect : SideEffect {
    data class UpdatedUsers(val users: List<String>): RoomSideEffect()

    data class Error(val error: String) : RoomSideEffect()
}

class RoomViewModel(
    private val audioSystem: AudioSystem,
    private val communicationSystem: CommunicationSystem
) : BaseViewModel<RoomViewState>(RoomViewState.Left), CommunicationDelegate {

    init {
        communicationSystem.delegate = this
    }

    fun toggleJoin(name: String, roomID: String) {
        viewModelScope.launch {
            emitViewState(RoomViewState.Loading)

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
            emitViewState(RoomViewState.Joined)
            emitSideEffect(RoomSideEffect.UpdatedUsers(communicationSystem.users))
        }
    }

    override fun left() {
        viewModelScope.launch {
            emitViewState(RoomViewState.Left)
            emitSideEffect(RoomSideEffect.UpdatedUsers(communicationSystem.users))
        }
    }

    override fun updatedUsers() {
        viewModelScope.launch {
            emitSideEffect(RoomSideEffect.UpdatedUsers(communicationSystem.users))
        }
    }

    override fun receivedError(error: Error) {
        viewModelScope.launch {
            emitSideEffect(RoomSideEffect.Error(error.localizedMessage))
            emitViewState(RoomViewState.Left)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val communicationSystem = CommunicationSystem(application.applicationContext)
                val audioSystem = AudioSystem(communicationSystem.roomManager)
                RoomViewModel(
                    audioSystem = audioSystem,
                    communicationSystem = communicationSystem
                )
            }
        }
    }
}