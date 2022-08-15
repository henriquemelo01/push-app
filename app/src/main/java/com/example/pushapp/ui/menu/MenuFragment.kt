package com.example.pushapp.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pushapp.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private lateinit var binding: FragmentMenuBinding

    private val viewModel: MenuViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentMenuBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupBind()
    }

    private fun setupBind() = with(binding) {

        ctPushAppMenu.setLeftIconClickListener { requireActivity().onBackPressed() }

        clReportHistory.setOnClickListener {
            findNavController().navigate(
                MenuFragmentDirections.actionMenuFragmentToReportsHistoryFragment()
            )
        }
    }
}