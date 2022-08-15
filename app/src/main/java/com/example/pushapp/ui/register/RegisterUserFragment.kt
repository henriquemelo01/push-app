package com.example.pushapp.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pushapp.databinding.FragmentRegisterUserBinding
import com.example.pushapp.utils.checkErrorState
import com.example.pushapp.utils.flowObserver
import com.google.android.material.textfield.TextInputLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterUserFragment : Fragment() {

    private lateinit var binding: FragmentRegisterUserBinding

    private val viewModel: RegisterUserViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentRegisterUserBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupBind()

        setupLiveData()
    }

    private fun setupBind() = with(binding) {

        ctRegisterAccountTitle.setLeftIconClickListener {
            requireActivity().onBackPressed()
        }

        itFirstName.doOnTextChanged { text, _, _, _ ->
            viewModel.doOnFirstNameTextChanged(text)
        }

        itUserEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.doOnEmailTextChanged(text)
        }

        itUserPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.doOnPasswordTextChanged(text)
        }

        itUserPasswordCheck.doOnTextChanged { text, _, _, _ ->

            viewModel.secPassword = text.toString()

            // Se password != secPassword -> Mostrar mensagem de erro
        }


        btCreateAccount.setOnClickListener {
            viewModel.createUserAccount()
        }
    }

    private fun setupLiveData() = with(viewModel) {

        flowObserver(navigateToTrainingConfig) { userModel ->

            Toast.makeText(
                requireContext(),
                "Navigate to Training Config - $userModel",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(
                RegisterUserFragmentDirections.actionRegisterUserFragmentToTrainingConfigurationFragment()
            )
        }

        flowObserver(failedToCreateUserAccountEvent) {
            Toast.makeText(requireContext(), "Xiii Falhou - ${it.message}", Toast.LENGTH_SHORT)
                .show()
        }

        flowObserver(failedToSaveUserEvent) { error ->
            Toast.makeText(
                requireContext(),
                "Falha Criação do Usuario - $error",
                Toast.LENGTH_SHORT
            ).show()
        }

        showFirstNameErrorState.observe(viewLifecycleOwner) { isRequiredFieldNotFilled ->
            binding.ilFirstName.checkErrorState(isRequiredFieldNotFilled)
        }

        showEmailErrorState.observe(viewLifecycleOwner) { isRequiredFieldNotFilled ->
            binding.ilUserEmail.checkErrorState(isRequiredFieldNotFilled)
        }

        showPasswordErrorState.observe(viewLifecycleOwner) { isRequiredFieldNotFilled ->
            binding.ilUserPassword.checkErrorState(isRequiredFieldNotFilled)
        }
    }
}