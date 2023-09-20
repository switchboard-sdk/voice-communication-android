package com.synervoz.onlineradioapp.domain

import android.content.Context
import com.synervoz.switchboard.sdk.audioengine.AudioEngine
import com.synervoz.switchboard.sdk.audiograph.AudioGraph
import com.synervoz.switchboard.sdk.audiographnodes.MonoToMultiChannelNode
import com.synervoz.switchboard.sdk.audiographnodes.ResampledSourceNode
import com.synervoz.switchboardagora.rooms.RoomManager

class ListenerAudioSystem(context: Context, roomManager: RoomManager) {
    val audioEngine = AudioEngine(context = context, microphoneEnabled = true)
    val audioGraph = AudioGraph()
    val agoraResampledSourceNode = ResampledSourceNode()
    val monoToMultiChannelNode = MonoToMultiChannelNode()

    init {
        agoraResampledSourceNode.setSourceNode(roomManager.sourceNode)
        agoraResampledSourceNode.internalSampleRate = roomManager.getAudioBus().sampleRate

        audioGraph.addNode(agoraResampledSourceNode)
        audioGraph.addNode(monoToMultiChannelNode)

        audioGraph.connect(agoraResampledSourceNode, monoToMultiChannelNode)
        audioGraph.connect(monoToMultiChannelNode, audioGraph.outputNode)
    }

    fun start() {
        audioEngine.start(audioGraph)
    }

    fun stop() {
        audioEngine.stop()
    }

    fun close() {
        audioGraph.close()
        audioEngine.close()
        agoraResampledSourceNode.close()
        monoToMultiChannelNode.close()
    }
}