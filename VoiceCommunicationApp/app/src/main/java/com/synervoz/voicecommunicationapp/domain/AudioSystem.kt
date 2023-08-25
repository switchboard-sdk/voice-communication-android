package com.synervoz.voicecommunicationapp.domain

import com.synervoz.switchboard.sdk.audioengine.AudioEngine
import com.synervoz.switchboard.sdk.audiograph.AudioGraph
import com.synervoz.switchboard.sdk.audiographnodes.MonoToMultiChannelNode
import com.synervoz.switchboard.sdk.audiographnodes.MultiChannelToMonoNode
import com.synervoz.switchboard.sdk.audiographnodes.ResampledSinkNode
import com.synervoz.switchboard.sdk.audiographnodes.ResampledSourceNode
import com.synervoz.switchboardagora.rooms.RoomManager

class AudioSystem(roomManager: RoomManager) {
    val audioEngine = AudioEngine(enableInput = true)
    val audioGraph = AudioGraph()
    val multiChannelToMonoNode = MultiChannelToMonoNode()
    val agoraResampledSourceNode = ResampledSourceNode()
    val agoraResampledSinkNode = ResampledSinkNode()
    val monoToMultiChannelNode = MonoToMultiChannelNode()

    init {
        agoraResampledSourceNode.setSourceNode(roomManager.sourceNode)
        agoraResampledSinkNode.setSinkNode(roomManager.sinkNode)

        agoraResampledSourceNode.internalSampleRate = roomManager.getAudioBus().sampleRate
        agoraResampledSinkNode.internalSampleRate = roomManager.getAudioBus().sampleRate

        audioGraph.addNode(multiChannelToMonoNode)
        audioGraph.addNode(agoraResampledSourceNode)
        audioGraph.addNode(agoraResampledSinkNode)
        audioGraph.addNode(monoToMultiChannelNode)

        audioGraph.connect(audioGraph.inputNode, multiChannelToMonoNode)
        audioGraph.connect(multiChannelToMonoNode, agoraResampledSinkNode)

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
        multiChannelToMonoNode.close()
        agoraResampledSourceNode.close()
        agoraResampledSinkNode.close()
        monoToMultiChannelNode.close()
    }
}