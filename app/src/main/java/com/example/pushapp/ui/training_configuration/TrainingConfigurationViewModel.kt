package com.example.pushapp.ui.training_configuration

import androidx.lifecycle.*
import com.example.pushapp.models.UserModel
import com.example.pushapp.models.WorkoutConfigurationModel
import com.example.pushapp.models.training_configuration.Exercise
import com.example.pushapp.models.training_configuration.TrainingMethodology
import com.example.pushapp.services.PushAppAuthService
import com.example.pushapp.services.PushAppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class TrainingConfigurationViewModel(
    private val authService: PushAppAuthService,
    private val repository: PushAppRepository
) : ViewModel(), DefaultLifecycleObserver {

    private var selectedExercise = Exercise.SMITH_MACHINE.value

    private var selectedTrainingMethod = TrainingMethodology.VELOCITY_BASED_TRAINING.value

    var weight: Int = 0

    var numberOfSets: Int = 0

    val trainingMethodologies = liveData {
        emit(
            listOf(
                TrainingMethodology.FREE_TRAINING.value,
                TrainingMethodology.VELOCITY_BASED_TRAINING.value
            )
        )
    }

    val availableExercises = liveData {
        emit(listOf(Exercise.NONE.value, Exercise.SMITH_MACHINE.value))
    }

    private val _enableStartButton = MutableLiveData(false)
    val enableStartButton: LiveData<Boolean> get() = _enableStartButton

    var bluetoothEnabled = false

    override fun onCreate(owner: LifecycleOwner) {
        getUserData()
    }

    private val _user = MutableLiveData<UserModel>()

    val userFirstName = _user.map { it.firstName.split(" ").first() }
    val showUserFirstName = _user.map { it.firstName.isNotEmpty() }

    private val _onGetUserDataFailureEvent = MutableSharedFlow<Throwable>()
    val onGetUserDataFailureEvent get() = _onGetUserDataFailureEvent.asSharedFlow()

    private fun getUserData() = viewModelScope.launch {

        val userId = authService.getCurrentUserId()

        userId?.let {
            repository.getUser(it)
                .onSuccess { userModel ->
                    _user.value = userModel
                }
                .onFailure { failure ->
                    _onGetUserDataFailureEvent.emit(failure)
                }
        }
    }

    fun onExerciseSpinnerItemSelected(exercisePosition: Int) {
        // Solução temporaria - Usar Mediator
        selectedExercise = availableExercises.value?.get(exercisePosition).orEmpty()
        checkIfButtonStartIsEnable()
    }

    fun onTrainingMethodologySpinnerItemSelected(trainingMethodPosition: Int) {
        // Solução temporaria - Usar Mediator
        selectedTrainingMethod = trainingMethodologies.value?.get(trainingMethodPosition).orEmpty()
        checkIfButtonStartIsEnable()
    }

    fun checkIfButtonStartIsEnable() {
        val isButtonStartEnable =
            (!selectedExercise.isEmpty() && !selectedTrainingMethod.isEmpty()) && (weight != 0 && numberOfSets != 0)

        _enableStartButton.value = isButtonStartEnable
    }

    private val _navigateToWorkoutScreenEvent = MutableSharedFlow<WorkoutConfigurationModel>()
    val navigateToWorkoutScreenEvent get() = _navigateToWorkoutScreenEvent.asSharedFlow()

    fun triggerNavigateToWorkoutScreenEvent() = viewModelScope.launch {
        _navigateToWorkoutScreenEvent.emit(
            WorkoutConfigurationModel(
                exercise = Exercise.getByValue(selectedExercise),
                trainingMethodology = TrainingMethodology.getByValue(selectedTrainingMethod),
                weight = weight,
                numberOfSets = numberOfSets
            )
        )
    }
}