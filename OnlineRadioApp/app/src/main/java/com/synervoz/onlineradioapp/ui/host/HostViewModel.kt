package com.synervoz.onlineradioapp.ui.host

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.synervoz.onlineradioapp.domain.CommunicationDelegate
import com.synervoz.onlineradioapp.domain.CommunicationSystem
import com.synervoz.onlineradioapp.domain.HostAudioSystem
import com.synervoz.onlineradioapp.ui.common.BaseViewModel
import com.synervoz.onlineradioapp.ui.common.SideEffect
import com.synervoz.onlineradioapp.ui.common.ViewState
import com.synervoz.switchboard.sdk.Codec
import com.synervoz.switchboard.sdk.utils.AssetLoader
import kotlinx.coroutines.launch

sealed class HostViewState : ViewState {
    object Loading : HostViewState()
    object Left : HostViewState()
    object Joined : HostViewState()
}

sealed class HostSideEffect : SideEffect {
    data class MusicPlaying(val isPlaying: Boolean): HostSideEffect()
    data class Error(val error: String) : HostSideEffect()
}

class HostViewModel(
    context: Context,
    private val audioSystem: HostAudioSystem,
    private val communicationSystem: CommunicationSystem
) : BaseViewModel<HostViewState>(HostViewState.Left), CommunicationDelegate {

    init {
        communicationSystem.delegate = this
        audioSystem.musicPlayerNode.load(AssetLoader.load(context, "EMH-My_Lover.mp3"), Codec.createFromFileName("EMH-My_Lover.mp3"))
        audioSystem.effectsPlayerNode.load(AssetLoader.load(context, "airhorn.mp3"), Codec.createFromFileName("airhorn.mp3"))
    }

    fun toggleJoin(name: String, roomID: String) {
        viewModelScope.launch {
            emitViewState(HostViewState.Loading)

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
                emitSideEffect(HostSideEffect.MusicPlaying(false))
            }
        } else {
            audioSystem.playMusic()
            viewModelScope.launch {
                emitSideEffect(HostSideEffect.MusicPlaying(true))
            }
        }
    }

    fun toggleSoundEffect() {
        audioSystem.playSoundEffect()
    }

    override fun joined() {
        viewModelScope.launch {
            emitViewState(HostViewState.Joined)
        }
    }

    override fun left() {
        viewModelScope.launch {
            emitViewState(HostViewState.Left)
        }
    }

    override fun receivedError(error: Error) {
        viewModelScope.launch {
            emitSideEffect(HostSideEffect.Error(error.localizedMessage))
            emitViewState(HostViewState.Left)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                val communicationSystem = CommunicationSystem(application.applicationContext, isHost = true)
                val audioSystem = HostAudioSystem(application.applicationContext, communicationSystem.roomManager)
                HostViewModel(
                    context = application.applicationContext,
                    audioSystem = audioSystem,
                    communicationSystem = communicationSystem
                )
            }
        }
    }
}