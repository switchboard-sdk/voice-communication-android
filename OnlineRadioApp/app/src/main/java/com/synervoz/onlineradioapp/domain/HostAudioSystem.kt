package com.synervoz.onlineradioapp.domain

import android.content.Context
import com.synervoz.switchboard.sdk.audioengine.AudioEngine
import com.synervoz.switchboard.sdk.audiograph.AudioGraph
import com.synervoz.switchboard.sdk.audiographnodes.AudioPlayerNode
import com.synervoz.switchboard.sdk.audiographnodes.BusSplitterNode
import com.synervoz.switchboard.sdk.audiographnodes.MixerNode
import com.synervoz.switchboard.sdk.audiographnodes.MultiChannelToMonoNode
import com.synervoz.switchboard.sdk.audiographnodes.MusicDuckingNode
import com.synervoz.switchboard.sdk.audiographnodes.ResampledSinkNode
import com.synervoz.switchboardagora.rooms.RoomManager

class HostAudioSystem(context: Context, roomManager: RoomManager) {
    val audioEngine = AudioEngine(context = context, microphoneEnabled = true)
    val audioGraph = AudioGraph()

    val agoraResampledSinkNode = ResampledSinkNode()

    val musicPlayerNode = AudioPlayerNode()
    val effectsPlayerNode = AudioPlayerNode()
    val playerMixerNode = MixerNode()

    val inputSplitterNode = BusSplitterNode()
    val inputMultiChannelToMonoNode = MultiChannelToMonoNode()

    val musicDuckingNode = MusicDuckingNode()
    val duckingSplitterNode = BusSplitterNode()

    val agoraOutputMixerNode = MixerNode()
    val multiChannelToMonoNode = MultiChannelToMonoNode()

    val isPlaying: Boolean
        get() = musicPlayerNode.isPlaying

    init {
        agoraResampledSinkNode.setSinkNode(roomManager.sinkNode)
        agoraResampledSinkNode.internalSampleRate = roomManager.getAudioBus().sampleRate

        musicPlayerNode.isLoopingEnabled = true

        audioGraph.addNode(agoraResampledSinkNode)
        audioGraph.addNode(musicPlayerNode)
        audioGraph.addNode(effectsPlayerNode)
        audioGraph.addNode(playerMixerNode)
        audioGraph.addNode(inputSplitterNode)
        audioGraph.addNode(inputMultiChannelToMonoNode)
        audioGraph.addNode(musicDuckingNode)
        audioGraph.addNode(duckingSplitterNode)
        audioGraph.addNode(agoraOutputMixerNode)
        audioGraph.addNode(multiChannelToMonoNode)

        audioGraph.connect(effectsPlayerNode, playerMixerNode)

        audioGraph.connect(musicPlayerNode, playerMixerNode)
        audioGraph.connect(playerMixerNode, musicDuckingNode)

        audioGraph.connect(audioGraph.inputNode, inputSplitterNode)
        audioGraph.connect(inputSplitterNode, inputMultiChannelToMonoNode)
        audioGraph.connect(inputMultiChannelToMonoNode, musicDuckingNode)

        audioGraph.connect(musicDuckingNode, duckingSplitterNode)

        audioGraph.connect(inputSplitterNode, agoraOutputMixerNode)
        audioGraph.connect(duckingSplitterNode, agoraOutputMixerNode)
        audioGraph.connect(agoraOutputMixerNode, multiChannelToMonoNode)
        audioGraph.connect(multiChannelToMonoNode, agoraResampledSinkNode)

        audioGraph.connect(duckingSplitterNode, audioGraph.outputNode)
    }

    fun start() {
        audioEngine.start(audioGraph)
    }

    fun stop() {
        audioEngine.stop()
    }

    fun playMusic() {
        musicPlayerNode.play()
    }

    fun pauseMusic() {
        musicPlayerNode.pause()
    }

    fun playSoundEffect() {
        effectsPlayerNode.stop()
        effectsPlayerNode.play()
    }

    fun close() {
        audioGraph.close()
        audioEngine.close()
        agoraResampledSinkNode.close()
        musicPlayerNode.close()
        effectsPlayerNode.close()
        playerMixerNode.close()
        inputSplitterNode.close()
        inputMultiChannelToMonoNode.close()
        musicDuckingNode.close()
        duckingSplitterNode.close()
        agoraOutputMixerNode.close()
        multiChannelToMonoNode.close()
    }
}