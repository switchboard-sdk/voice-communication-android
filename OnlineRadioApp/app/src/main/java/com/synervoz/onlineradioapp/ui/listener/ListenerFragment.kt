package com.synervoz.onlineradioapp.ui.listener

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.core.widget.addTextChangedListener
import com.synervoz.onlineradioapp.databinding.FragmentListenerBinding
import com.synervoz.onlineradioapp.ui.common.BaseFragment
import com.synervoz.onlineradioapp.ui.common.SideEffect
import com.synervoz.onlineradioapp.ui.common.ViewState
import kotlin.random.Random

class ListenerFragment : BaseFragment<FragmentListenerBinding, ListenerViewState, ListenerViewModel>() {

    companion object {
        const val TAG = "ListenerFragment"
    }

    override val viewModel: ListenerViewModel by viewModels { ListenerViewModel.Factory }

    override var loading: Boolean = false
        set(value) {
            binding.loadingIndicator.root.visibility = if (value) View.VISIBLE else View.GONE
            field = value
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setView(FragmentListenerBinding.inflate(inflater, container, false))

        binding.activeLabel.visibility = View.INVISIBLE

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.channelName.addTextChangedListener {
            binding.startButton.isEnabled = checkIfCanJoin()
        }

        binding.startButton.setOnClickListener {
            viewModel.toggleJoin("listener-${Random.nextInt(0, 1000)}", binding.channelName.text.toString())
        }

        binding.startButton.isEnabled = false

        viewModel.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stop()
    }

    override fun renderViewState(viewState: ViewState) {
        when (viewState) {
            is ListenerViewState.Loading -> renderLoadingState(viewState)
            is ListenerViewState.Joined -> renderJoinedState(viewState)
            is ListenerViewState.Left -> renderLeftState(viewState)
        }
    }

    override fun handleSideEffect(sideEffect: SideEffect) {
        when (sideEffect) {
            is ListenerSideEffect.Error -> handleError(sideEffect)
        }
    }

    private fun renderLoadingState(viewState: ListenerViewState.Loading) {
        loading = true
    }

    private fun renderJoinedState(viewState: ListenerViewState.Joined) {
        setJoinedState()
    }

    private fun renderLeftState(viewState: ListenerViewState.Left) {
        setLeftState()
    }

    private fun handleError(sideEffect: ListenerSideEffect.Error) {
        Toast.makeText(requireContext(), sideEffect.error, Toast.LENGTH_LONG)
    }

    private fun checkIfCanJoin(): Boolean {
        return binding.channelName.text.isNotBlank()
    }

    fun setJoinedState() {
        loading = false
        binding.channelName.isEnabled = false
        binding.startButton.text = "Stop"

        binding.activeLabel.text = "Listening to channel ${binding.channelName.text}"
        binding.activeLabel.visibility = View.VISIBLE
    }

    fun setLeftState() {
        loading = false
        binding.channelName.isEnabled = true
        binding.startButton.text = "Listen"

        binding.activeLabel.visibility = View.INVISIBLE
    }

}