package com.synervoz.voicecommunicationapp.ui.room

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.synervoz.voicecommunicationapp.domain.AudioSystem
import com.synervoz.voicecommunicationapp.domain.CommunicationSystem
import com.synervoz.voicecommunicationapp.ui.common.BaseViewModel
import com.synervoz.voicecommunicationapp.ui.common.SideEffect
import com.synervoz.voicecommunicationapp.ui.common.ViewState

sealed class RoomViewState : ViewState {
    object Loading : RoomViewState()
    object Ready : RoomViewState()
}

sealed class RoomSideEffect : SideEffect {

}

class RoomViewModel(
    private val audioSystem: AudioSystem,
    private val communicationSystem: CommunicationSystem
) : BaseViewModel<RoomViewState>(RoomViewState.Loading) {

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