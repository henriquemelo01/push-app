package com.example.pushapp.ui.login

import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    val email = MutableLiveData("")

    val password = MutableLiveData("")

    val isSignInButtonEnable = MediatorLiveData<Boolean>().apply {

        addSource(email) {
            value = checkIfRequiredFieldsWasFilled()
        }
        addSource(password) {
            value = checkIfRequiredFieldsWasFilled()
        }
    }

    private val _userLoginEvent = MutableSharedFlow<LoginEvent>()
    val userLoginEvent get() = _userLoginEvent.asSharedFlow()

    private val _navigateToAuthenticatedScreen = MutableSharedFlow<Unit>()
    val navigateToAuthenticatedScreen get() = _navigateToAuthenticatedScreen.asSharedFlow()

    private val _showEmailErrorState = MutableLiveData<Boolean>()
    val showEmailErrorState: LiveData<Boolean> get() = _showEmailErrorState

    private val _showPasswordErrorState = MutableLiveData<Boolean>()
    val showPasswordErrorState: LiveData<Boolean> get() = _showPasswordErrorState

    private fun checkIfRequiredFieldsWasFilled() =
        email.value.isNullOrEmpty().not() && password.value.isNullOrEmpty().not()

    fun checkErrorsState() {
        _showEmailErrorState.value = email.value.isNullOrEmpty()
        _showPasswordErrorState.value = password.value.isNullOrEmpty()
    }

    fun triggerLoginEvent() = viewModelScope.launch {
        //if (email.value != null && password.value != null)
        _userLoginEvent.emit(
            LoginEvent(
                email = email.value.orEmpty(),
                password = password.value.orEmpty()
            )
        )
    }

    fun navigateToAuthenticatedScreen() = viewModelScope.launch {
        _navigateToAuthenticatedScreen.emit(Unit)
    }

    data class LoginEvent(
        val email: String,
        val password: String
    )
}