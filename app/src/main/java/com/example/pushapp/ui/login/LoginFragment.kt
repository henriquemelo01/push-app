package com.example.pushapp.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pushapp.databinding.FragmentLoginBinding
import com.example.pushapp.utils.checkErrorState
import com.example.pushapp.utils.flowObserver
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var fireStore: FirebaseFirestore

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

//        auth = FirebaseAuth.getInstance().also {
//            try {
//                it.useEmulator("10.0.2.2", 8080)
//            } catch (e: IllegalStateException) {
//            }
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentLoginBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupBind()
        setupEventListeners()
    }

    private fun setupBind() = with(binding) {

        itUserEmail.doOnTextChanged { text, _, _, _ ->
            viewModel.email.value = text.toString()
            viewModel.checkErrorsState()
        }

        itUserPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.password.value = text.toString()
            viewModel.checkErrorsState()
        }

        btSignIn.setOnClickListener {
            viewModel.triggerLoginEvent()
        }

        btRegister.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToRegisterUserFragment()
            )
        }
    }

    private fun setupEventListeners() = with(viewModel) {


        flowObserver(userLoginEvent) { loginEvent ->
            authenticateWithEmailAndPassword(
                email = loginEvent.email,
                password = loginEvent.password
            )
        }

        flowObserver(navigateToAuthenticatedScreen) {
            findNavController().navigate(
                LoginFragmentDirections.actionGlobalToTrainingConfigurationFragment()
            )
        }

        isSignInButtonEnable.observe(viewLifecycleOwner) {
            binding.btSignIn.isEnabled = it
        }

        showEmailErrorState.observe(viewLifecycleOwner) { isRequiredFieldNotFilled ->
            binding.ilUserEmail.checkErrorState(isRequiredFieldNotFilled)
        }

        showPasswordErrorState.observe(viewLifecycleOwner) { isRequiredFieldNotFilled ->
            binding.ilUserPassword.checkErrorState(isRequiredFieldNotFilled)
        }
    }

    private fun authenticateWithEmailAndPassword(email: String, password: String) {
        auth
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                println("Usuario Autentificado: " + it.user?.email)
                viewModel.navigateToAuthenticatedScreen()
            }.addOnFailureListener {
                println("Falha na autentificação:\n${it.cause}")
            }
    }
}