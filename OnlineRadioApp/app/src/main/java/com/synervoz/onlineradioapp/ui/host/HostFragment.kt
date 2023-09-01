package com.synervoz.onlineradioapp.ui.host

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.core.widget.addTextChangedListener
import com.synervoz.onlineradioapp.databinding.FragmentHostBinding
import com.synervoz.onlineradioapp.ui.common.BaseFragment
import com.synervoz.onlineradioapp.ui.common.SideEffect
import com.synervoz.onlineradioapp.ui.common.ViewState

class HostFragment : BaseFragment<FragmentHostBinding, HostViewState, HostViewModel>() {

    companion object {
        const val TAG = "HostFragment"
    }

    override val viewModel: HostViewModel by viewModels { HostViewModel.Factory }

    override var loading: Boolean = false
        set(value) {
            binding.loadingIndicator.root.visibility = if (value) View.VISIBLE else View.GONE
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setView(FragmentHostBinding.inflate(inflater, container, false))

        binding.activeLabel.visibility = View.INVISIBLE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.channelName.addTextChangedListener {
            binding.startButton.isEnabled = checkIfCanJoin()
        }

        binding.startButton.setOnClickListener {
            viewModel.toggleJoin("host", binding.channelName.text.toString())
        }

        binding.startButton.isEnabled = false

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
            is HostViewState.Loading -> renderLoadingState(viewState)
            is HostViewState.Joined -> renderJoinedState(viewState)
            is HostViewState.Left -> renderLeftState(viewState)
        }
    }

    override fun handleSideEffect(sideEffect: SideEffect) {
        when (sideEffect) {
            is HostSideEffect.MusicPlaying -> handleMusicPlaying(sideEffect)
            is HostSideEffect.Error -> handleError(sideEffect)
        }
    }

    private fun renderLoadingState(viewState: HostViewState.Loading) {
        loading = true
    }

    private fun renderJoinedState(viewState: HostViewState.Joined) {
        setJoinedState()
    }

    private fun renderLeftState(viewState: HostViewState.Left) {
        setLeftState()
    }

    private fun handleMusicPlaying(sideEffect: HostSideEffect.MusicPlaying) {
        binding.musicButton.text = if (sideEffect.isPlaying) "Pause Music" else "Play Music"
    }

    private fun handleError(sideEffect: HostSideEffect.Error) {
        Toast.makeText(requireContext(), sideEffect.error, Toast.LENGTH_LONG)
    }

    private fun checkIfCanJoin(): Boolean {
        return binding.channelName.text.isNotBlank()
    }

    private fun setJoinedState() {
        loading = false
        binding.channelName.isEnabled = false
        binding.startButton.text = "Stop Broadcast"

        binding.activeLabel.text = "Broadcasting in channel ${binding.channelName.text}"
        binding.activeLabel.visibility = View.VISIBLE
    }

    private fun setLeftState() {
        loading = false
        binding.channelName.isEnabled = true
        binding.startButton.text = "Start Broadcast"

        binding.activeLabel.visibility = View.INVISIBLE
    }
}