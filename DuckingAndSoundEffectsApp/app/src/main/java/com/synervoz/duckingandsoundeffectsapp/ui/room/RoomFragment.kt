package com.synervoz.duckingandsoundeffectsapp.ui.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.synervoz.duckingandsoundeffectsapp.databinding.FragmentRoomBinding
import com.synervoz.duckingandsoundeffectsapp.ui.common.BaseFragment
import com.synervoz.duckingandsoundeffectsapp.ui.common.SideEffect
import com.synervoz.duckingandsoundeffectsapp.ui.common.ViewState

class RoomFragment : BaseFragment<FragmentRoomBinding, RoomViewState, RoomViewModel>() {

    companion object {
        const val TAG = "RoomFragment"
    }

    override val viewModel: RoomViewModel by viewModels { RoomViewModel.Factory }

    override var loading: Boolean = false
        set(value) {
            binding.loadingIndicator.root.visibility = if (value) View.VISIBLE else View.GONE
            field = value
        }

    private lateinit var adapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setView(FragmentRoomBinding.inflate(inflater, container, false))

        adapter = UserListAdapter(emptyList())
        binding.userList.adapter = adapter

        binding.activeLabel.visibility = View.INVISIBLE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.username.addTextChangedListener {
            binding.joinButton.isEnabled = checkIfCanJoin()
        }

        binding.roomName.addTextChangedListener {
            binding.joinButton.isEnabled = checkIfCanJoin()
        }

        binding.joinButton.setOnClickListener {
            viewModel.toggleJoin(binding.username.text.toString(), binding.roomName.text.toString())
        }

        binding.joinButton.isEnabled = false

        binding.musicButton.setOnClickListener {
            viewModel.toggleMusic()
        }

        binding.effectButton.setOnClickListener {
            viewModel.toggleSoundEffect()
        }

        viewModel.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stop()
    }

    override fun renderViewState(viewState: ViewState) {
        when (viewState) {
            is RoomViewState.Loading -> renderLoadingState(viewState)
            is RoomViewState.Joined -> renderJoinedState(viewState)
            is RoomViewState.Left -> renderLeftState(viewState)
        }
    }

    override fun handleSideEffect(sideEffect: SideEffect) {
        when (sideEffect) {
            is RoomSideEffect.UpdatedUsers -> handleUpdatedUsers(sideEffect)
            is RoomSideEffect.MusicPlaying -> handleMusicPlaying(sideEffect)
            is RoomSideEffect.Error -> handleError(sideEffect)
        }
    }

    private fun renderLoadingState(viewState: RoomViewState.Loading) {
        loading = true
    }

    private fun renderJoinedState(viewState: RoomViewState.Joined) {
        setJoinedState()
    }

    private fun renderLeftState(viewState: RoomViewState.Left) {
        setLeftState()
    }

    private fun handleUpdatedUsers(sideEffect: RoomSideEffect.UpdatedUsers) {
        adapter.userIds = sideEffect.users
    }

    private fun handleMusicPlaying(sideEffect: RoomSideEffect.MusicPlaying) {
        binding.musicButton.text = if (sideEffect.isPlaying) "Pause Music" else "Play Music"
    }

    private fun handleError(sideEffect: RoomSideEffect.Error) {
        Toast.makeText(requireContext(), sideEffect.error, Toast.LENGTH_LONG)
    }

    private fun checkIfCanJoin(): Boolean {
        return !binding.username.text.isBlank() && !binding.roomName.text.isBlank()
    }

    private fun setJoinedState() {
        loading = false
        binding.username.isEnabled = false
        binding.roomName.isEnabled = false
        binding.joinButton.text = "Leave"

        binding.activeLabel.text = "Room ${binding.roomName.text} active"
        binding.activeLabel.visibility = View.VISIBLE
    }

    private fun setLeftState() {
        loading = false
        binding.username.isEnabled = true
        binding.roomName.isEnabled = true
        binding.joinButton.text = "Join"

        binding.activeLabel.visibility = View.INVISIBLE
    }
}
