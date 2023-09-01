package com.synervoz.onlineradioapp.ui.role

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.synervoz.onlineradioapp.MainActivity
import com.synervoz.onlineradioapp.databinding.FragmentRoleBinding
import com.synervoz.onlineradioapp.ui.host.HostFragment
import com.synervoz.onlineradioapp.ui.listener.ListenerFragment

class RoleFragment : Fragment() {

    private var _binding: FragmentRoleBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.hostButton.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.pushFragment(HostFragment())
        }

        binding.listenerButton.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.pushFragment(ListenerFragment())
        }
    }
}