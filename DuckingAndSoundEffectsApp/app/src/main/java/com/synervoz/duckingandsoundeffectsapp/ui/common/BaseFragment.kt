package com.synervoz.duckingandsoundeffectsapp.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class BaseFragment<T: ViewBinding, VS: ViewState, VM: BaseViewModel<VS>> : Fragment() {

    companion object {
        const val TAG = "BaseFragment"
    }

    private var _binding: T? = null
    protected val binding get() = _binding!!

    abstract val viewModel: VM

    protected open var loading: Boolean = false

    fun setView(binding: T?) {
        _binding = binding
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.state
//                .onEach { Logger.debug("ViewState: $it") }
                .observe(viewLifecycleOwner) {
                    renderViewState(it)
                }
        }

        lifecycleScope.launch {
            viewModel.sideEffect
//                .onEach { Logger.debug("SideEffect: $it") }
                .observe(viewLifecycleOwner) { sideEffect ->
                    viewModel.receiveSideEffect(sideEffect) {
                        handleSideEffect(it)
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setView(null)
    }

    abstract fun renderViewState(viewState: ViewState)
    open fun handleSideEffect(sideEffect: SideEffect) {}
}
