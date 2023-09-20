package com.synervoz.duckingandsoundeffectsapp.domain

import android.content.Context
import com.synervoz.switchboard.sdk.audioengine.AudioEngine
import com.synervoz.switchboard.sdk.audiograph.AudioGraph
import com.synervoz.switchboard.sdk.audiographnodes.AudioPlayerNode
import com.synervoz.switchboard.sdk.audiographnodes.BusSplitterNode
import com.synervoz.switchboard.sdk.audiographnodes.MixerNode
import com.synervoz.switchboard.sdk.audiographnodes.MonoToMultiChannelNode
import com.synervoz.switchboard.sdk.audiographnodes.MultiChannelToMonoNode
import com.synervoz.switchboard.sdk.audiographnodes.MusicDuckingNode
import com.synervoz.switchboard.sdk.audiographnodes.ResampledSinkNode
import com.synervoz.switchboard.sdk.audiographnodes.ResampledSourceNode
import com.synervoz.switchboardagora.rooms.RoomManager

class AudioSystem(context: Context, roomManager: RoomManager) {
    val audioEngine = AudioEngine(context = context, microphoneEnabled = true)
    val audioGraph = AudioGraph()

    val agoraResampledSourceNode = ResampledSourceNode()
    val agoraResampledSinkNode = ResampledSinkNode()

    val musicPlayerNode = AudioPlayerNode()
    val effectsPlayerNode = AudioPlayerNode()
    val effectsSplitterNode = BusSplitterNode()
    val playerMixerNode = MixerNode()

    val inputSplitterNode = BusSplitterNode()
    val inputMultiChannelToMonoNode = MultiChannelToMonoNode()

    val monoToMultiChannelNode = MonoToMultiChannelNode()

    val musicDuckingNode = MusicDuckingNode()

    val agoraOutputMixerNode = MixerNode()
    val multiChannelToMonoNode = MultiChannelToMonoNode()

    val agoraSourceSplitterNode = BusSplitterNode()
    val speakerMixerNode = MixerNode()

    val isPlaying: Boolean
        get() = musicPlayerNode.isPlaying

    init {
        agoraResampledSourceNode.setSourceNode(roomManager.sourceNode)
        agoraResampledSinkNode.setSinkNode(roomManager.sinkNode)

        agoraResampledSourceNode.internalSampleRate = roomManager.getAudioBus().sampleRate
        agoraResampledSinkNode.internalSampleRate = roomManager.getAudioBus().sampleRate

        musicPlayerNode.isLoopingEnabled = true

        audioGraph.addNode(agoraResampledSourceNode)
        audioGraph.addNode(agoraResampledSinkNode)
        audioGraph.addNode(musicPlayerNode)
        audioGraph.addNode(effectsPlayerNode)
        audioGraph.addNode(effectsSplitterNode)
        audioGraph.addNode(playerMixerNode)
        audioGraph.addNode(inputSplitterNode)
        audioGraph.addNode(inputMultiChannelToMonoNode)
        audioGraph.addNode(monoToMultiChannelNode)
        audioGraph.addNode(musicDuckingNode)
        audioGraph.addNode(agoraOutputMixerNode)
        audioGraph.addNode(multiChannelToMonoNode)
        audioGraph.addNode(agoraSourceSplitterNode)
        audioGraph.addNode(speakerMixerNode)

        audioGraph.connect(effectsPlayerNode, effectsSplitterNode)
        audioGraph.connect(effectsSplitterNode, playerMixerNode)

        audioGraph.connect(musicPlayerNode, playerMixerNode)
        audioGraph.connect(playerMixerNode, musicDuckingNode)

        audioGraph.connect(audioGraph.inputNode, inputSplitterNode)
        audioGraph.connect(inputSplitterNode, inputMultiChannelToMonoNode)
        audioGraph.connect(inputMultiChannelToMonoNode, musicDuckingNode)

        audioGraph.connect(inputSplitterNode, agoraOutputMixerNode)
        audioGraph.connect(effectsSplitterNode, agoraOutputMixerNode)
        audioGraph.connect(agoraOutputMixerNode, multiChannelToMonoNode)
        audioGraph.connect(multiChannelToMonoNode, agoraResampledSinkNode)

        audioGraph.connect(agoraResampledSourceNode, agoraSourceSplitterNode)
        audioGraph.connect(agoraSourceSplitterNode, musicDuckingNode)
        audioGraph.connect(agoraSourceSplitterNode, monoToMultiChannelNode)
        audioGraph.connect(monoToMultiChannelNode, speakerMixerNode)

        audioGraph.connect(musicDuckingNode, speakerMixerNode)
        audioGraph.connect(speakerMixerNode, audioGraph.outputNode)
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
        multiChannelToMonoNode.close()
        agoraResampledSourceNode.close()
        agoraResampledSinkNode.close()
        monoToMultiChannelNode.close()
    }
}
