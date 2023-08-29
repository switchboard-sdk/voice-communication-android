package com.synervoz.duckingandsoundeffectsapp.ui.room

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.synervoz.duckingandsoundeffectsapp.domain.AudioSystem
import com.synervoz.duckingandsoundeffectsapp.domain.CommunicationDelegate
import com.synervoz.duckingandsoundeffectsapp.domain.CommunicationSystem
import com.synervoz.duckingandsoundeffectsapp.ui.common.BaseViewModel
import com.synervoz.duckingandsoundeffectsapp.ui.common.SideEffect
import com.synervoz.duckingandsoundeffectsapp.ui.common.ViewState
import com.synervoz.switchboard.sdk.Codec
import com.synervoz.switchboard.sdk.utils.AssetLoader
import kotlinx.coroutines.launch

sealed class RoomViewState : ViewState {
    object Loading : RoomViewState()
    object Left : RoomViewState()
    object Joined : RoomViewState()
}

sealed class RoomSideEffect : SideEffect {
    data class UpdatedUsers(val users: List<String>): RoomSideEffect()
    data class MusicPlaying(val isPlaying: Boolean): RoomSideEffect()
    data class Error(val error: String) : RoomSideEffect()
}

class RoomViewModel(
    context: Context,
    private val audioSystem: AudioSystem,
    private val communicationSystem: CommunicationSystem
) : BaseViewModel<RoomViewState>(RoomViewState.Left), CommunicationDelegate {

    init {
        communicationSystem.delegate = this
        audioSystem.musicPlayerNode.load(AssetLoader.load(context, "EMH-My_Lover.mp3"), Codec.createFromFileName("EMH-My_Lover.mp3"))
        audioSystem.effectsPlayerNode.load(AssetLoader.load(context, "airhorn.mp3"), Codec.createFromFileName("airhorn.mp3"))
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

    fun toggleMusic() {
        if (audioSystem.isPlaying) {
            audioSystem.pauseMusic()
            viewModelScope.launch {
                emitSideEffect(RoomSideEffect.MusicPlaying(false))
            }
        } else {
            audioSystem.playMusic()
            viewModelScope.launch {
                emitSideEffect(RoomSideEffect.MusicPlaying(true))
            }
        }
    }

    fun toggleSoundEffect() {
        audioSystem.playSoundEffect()
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
                    context = application.applicationContext,
                    audioSystem = audioSystem,
                    communicationSystem = communicationSystem
                )
            }
        }
    }
}
