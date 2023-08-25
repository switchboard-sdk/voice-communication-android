package com.synervoz.voicecommunicationapp.ui.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.synervoz.voicecommunicationapp.databinding.FragmentRoomBinding
import com.synervoz.voicecommunicationapp.ui.common.BaseFragment
import com.synervoz.voicecommunicationapp.ui.common.SideEffect
import com.synervoz.voicecommunicationapp.ui.common.ViewState

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setView(FragmentRoomBinding.inflate(inflater, container, false))

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel.startAudioSystem()
    }

    override fun onDestroy() {
        super.onDestroy()
//        viewModel.stopAudioSystem()
    }

    override fun renderViewState(viewState: ViewState) {
        when (viewState) {
            is RoomViewState.Loading -> renderLoadingState(viewState)
            is RoomViewState.Ready -> renderReadyState(viewState)
        }
    }

    override fun handleSideEffect(sideEffect: SideEffect) {

    }

    private fun renderLoadingState(viewState: RoomViewState.Loading) {
        loading = true
    }

    private fun renderReadyState(viewState: RoomViewState.Ready) {
        loading = false
    }

}