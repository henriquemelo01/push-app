package com.example.pushapp.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pushapp.models.CreateUserRequest
import com.example.pushapp.models.UserModel
import com.example.pushapp.services.PushAppAuthService
import com.example.pushapp.services.PushAppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RegisterUserViewModel(
    private val pushAppRepository: PushAppRepository,
    private val authService: PushAppAuthService
) : ViewModel() {

    var email: String? = null

    var password: String? = null

    var secPassword: String? = null

    var firstName: String? = null

    private var userCreated: UserModel? = null

    private val _failedToSaveUserEvent = MutableSharedFlow<Throwable>()
    val failedToSaveUserEvent get() = _failedToSaveUserEvent.asSharedFlow()

    private val _navigateToTrainingConfig = MutableSharedFlow<UserModel>()
    val navigateToTrainingConfig get() = _navigateToTrainingConfig.asSharedFlow()

    private val _failedToCreateUserAccountEvent = MutableSharedFlow<Throwable>()
    val failedToCreateUserAccountEvent get() = _failedToCreateUserAccountEvent.asSharedFlow()

    private val _showFirstNameErrorState = MutableLiveData<Boolean>()
    val showFirstNameErrorState: LiveData<Boolean> get() = _showFirstNameErrorState

    private val _showEmailErrorState = MutableLiveData<Boolean>()
    val showEmailErrorState: LiveData<Boolean> get() = _showEmailErrorState

    private val _showPasswordErrorState = MutableLiveData<Boolean>()
    val showPasswordErrorState: LiveData<Boolean> get() = _showPasswordErrorState

    fun createUserAccount() = viewModelScope.launch {

        if (email != null && password != null && password == secPassword && firstName != null)
            authService
                .createAccount(email = email!!, password = password!!)
                .onSuccess { userId ->
                    addAccountToDatabase(
                        CreateUserRequest(
                            userId = userId,
                            email = email.orEmpty(),
                            firstName = firstName.orEmpty()
                        )
                    )
                }.onFailure {
                    _failedToCreateUserAccountEvent.emit(it)
                }
    }

    private fun addAccountToDatabase(
        user: CreateUserRequest
    ) = viewModelScope.launch {

        pushAppRepository.saveCreatedUserData(user)
            .onSuccess {
                userCreated = it

                _navigateToTrainingConfig.emit(it)

            }.onFailure {
                _failedToSaveUserEvent.emit(it)
            }
    }

    fun doOnFirstNameTextChanged(text: CharSequence?) {
        firstName = text.toString()
        _showFirstNameErrorState.value = firstName.isNullOrEmpty()
    }

    fun doOnEmailTextChanged(text: CharSequence?) {
        email = text.toString()
        _showEmailErrorState.value = email.isNullOrEmpty()
    }

    fun doOnPasswordTextChanged(text: CharSequence?) {
        password = text.toString()
        _showPasswordErrorState.value = password.isNullOrEmpty()
    }

    enum class InputError(val message: String) {
        REQUIRED_FIELD_NOT_FILLED("required *"),
        CONFIRM_PASSWORD_IS_DIFFERENT("passwords distintos *")
    }
}