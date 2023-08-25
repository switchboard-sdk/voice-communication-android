package com.synervoz.voicecommunicationapp.domain

import android.content.Context
import android.view.SurfaceView
import com.synervoz.switchboardagora.rooms.Room
import com.synervoz.switchboardagora.rooms.RoomConnectionState
import com.synervoz.switchboardagora.rooms.RoomInterface
import com.synervoz.switchboardagora.rooms.RoomManager
import com.synervoz.switchboardagora.rooms.SubscriberVideoView
import com.synervoz.voicecommunicationapp.Config

enum class State {
    JOINING, JOINED, LEAVING, LEFT;
}

interface CommunicationDelegate {
    fun joined()
    fun left()
    fun updatedUsers()
    fun receivedError(error: Error)
}

class CommunicationSystem(context: Context) {
    lateinit var delegate: CommunicationDelegate

    val roomManager = RoomManager(context, Config.switchboardAPIBaseURL)
    var room: Room? = null

    val users = mutableListOf<String>()

    val isConnected: Boolean
        get() = room?.state?.isConnected == true

    val subscribeEnabled: Boolean
        get() = room?.state?.subscribeEnabled == true

    val publishEnabled: Boolean
        get() = room?.state?.publishEnabled == true

    val joined: Boolean
        get() = isConnected && subscribeEnabled && publishEnabled

    private var communicationSystemState: State = State.LEFT

    private fun connect(name: String, roomID: String) {
        room = roomManager.createRoom("VoiceCommunicationApp-$roomID")
        room?.roomInterface = roomDelegate
        room?.join(name)
    }

    private fun disconnect() {
        room?.leave()
    }

    private fun subscribe() {
        room?.subscribe()
    }

    private fun unsubscribe() {
        room?.unsubscribe()
    }

    private fun publish() {
        room?.publish()
    }

    private fun unpublish() {
        room?.unpublish()
    }

    fun join(name: String, roomID: String) {
        communicationSystemState = State.JOINING
        connect(name, roomID)
        subscribe()
        publish()
    }

    fun leave() {
        communicationSystemState = State.LEAVING
        unpublish()
        unsubscribe()
        disconnect()
    }

    val roomDelegate: RoomInterface = object : RoomInterface {
        override fun didUpdateConnectionState(state: RoomConnectionState) {
            if (communicationSystemState == State.JOINING) {
                if (state.isConnected && state.subscribeEnabled && state.publishEnabled) {
                    communicationSystemState = State.JOINED
                    delegate.joined()
                }
            } else if (communicationSystemState == State.LEAVING) {
                if (!state.isConnected && !state.subscribeEnabled && !state.publishEnabled) {
                    communicationSystemState = State.LEFT
                    delegate.left()
                }
            }
        }

        override fun didFailToJoinWithError(error: Error) {
            delegate.receivedError(error)
        }

        override fun didFailToPublishWithError(error: Error) {
            delegate.receivedError(error)
        }

        override fun didFailToSubscribeWithError(error: Error) {
            delegate.receivedError(error)
        }

        override fun didUpdatePublisherAudioLevel(audioLevel: Float) {}

        override fun didUpdateSubscriberAudioLevel(audioLevel: Float, userID: String?) {}

        override fun userDidJoin(userID: String) {
            if (!users.contains(userID)) {
                users.add(userID)
            }
            delegate.updatedUsers()
        }

        override fun userDidLeave(userID: String) {
            users.removeAll { it == userID }
            delegate.updatedUsers()
        }

        override fun userDidMute(userID: String) {}

        override fun userDidUnmute(userID: String) {}

        override fun didUpdatePublisherVideoView(view: SurfaceView?) {}

        override fun didUpdateSubscriberVideoViews(views: ArrayList<SubscriberVideoView>) {}

        override fun videoDidMute(userID: String) {}

        override fun videoDidUnmute(userID: String) {}

        override fun videoPublisherState(mute: Boolean) {}
    }
}