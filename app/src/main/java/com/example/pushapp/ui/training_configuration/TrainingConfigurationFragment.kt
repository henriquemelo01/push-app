package com.example.pushapp.ui.training_configuration

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.pushapp.NavigationDirections
import com.example.pushapp.R
import com.example.pushapp.utils.BlePermissionsHandler
import com.example.pushapp.databinding.FragmentTrainingConfigurationBinding
import com.example.pushapp.utils.flowObserver
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrainingConfigurationFragment : Fragment() {

    private lateinit var binding: FragmentTrainingConfigurationBinding

    private val viewModel: TrainingConfigurationViewModel by viewModel()

    private lateinit var blePermissionsHandler: BlePermissionsHandler

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            requireActivity().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentTrainingConfigurationBinding.inflate(inflater, container, false).apply {

        blePermissionsHandler = BlePermissionsHandler(
            activityResultRegistry = requireActivity().activityResultRegistry,
            context = requireContext()
        )

        lifecycle.addObserver(blePermissionsHandler)
        lifecycle.addObserver(viewModel)
        binding = this

    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupBind()
        setupBlePermissionsHandlerCallbacks()
        setupLiveData()
    }

    private fun setupBind() = with(binding) {

        ctTrainingConfig.setRightIconClickListener {
            findNavController().navigate(
                TrainingConfigurationFragmentDirections.actionToMenu()
            )
        }

        spExercise.apply {

            setOnItemClickListener { _, _, position, _ ->
                viewModel.onExerciseSpinnerItemSelected(position)
            }

//            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//
//                override fun onItemSelected(
//                    parent: AdapterView<*>?,
//                    view: View?,
//                    position: Int,
//                    id: Long
//                ) {
//                    viewModel.onExerciseSpinnerItemSelected(position)
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {}
//            }
        }

        spTrainingMethodology.apply {

            setOnItemClickListener { _, _, position, _ ->
                viewModel.onTrainingMethodologySpinnerItemSelected(position)
            }
//            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//
//                override fun onItemSelected(
//                    parent: AdapterView<*>?,
//                    view: View?,
//                    position: Int,
//                    id: Long
//                ) {
//                    viewModel.onTrainingMethodologySpinnerItemSelected(position)
//                }
//
//                override fun onNothingSelected(p0: AdapterView<*>?) {}
//            }
        }

        etWeight.doOnTextChanged { text, _, _, _ ->
            viewModel.weight = text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0
            viewModel.checkIfButtonStartIsEnable()
        }

        etNumberOfSets.doOnTextChanged { text, _, _, _ ->
            viewModel.numberOfSets =
                text.toString().takeIf { it.isNotEmpty() }?.toInt() ?: 0

            viewModel.checkIfButtonStartIsEnable()
        }

        btStartTraining.setOnClickListener {
            blePermissionsHandler.checkPermissions()

            if (viewModel.bluetoothEnabled)
                viewModel.triggerNavigateToWorkoutScreenEvent()
        }
    }

    private fun setupLiveData() = with(viewModel) {

        showUserFirstName.observe(viewLifecycleOwner) {
            binding.tvHelloTrainingConfig.visibility = if (it) View.VISIBLE else View.GONE
            binding.tvUserFirstName.visibility = if (it) View.VISIBLE else View.GONE
        }

        userFirstName.observe(viewLifecycleOwner) {
            binding.tvUserFirstName.text = it
        }

        flowObserver(onGetUserDataFailureEvent) { failure ->
            Toast
                .makeText(requireContext(), "Xiii falhou: ${failure.message}", Toast.LENGTH_SHORT)
                .show()
        }

        enableStartButton.observe(viewLifecycleOwner) { btStartIsEnable ->
            binding.btStartTraining.isEnabled = btStartIsEnable
        }

        flowObserver(navigateToWorkoutScreenEvent) { workoutConfigModel ->
            findNavController().navigate(
                NavigationDirections.actionGlobalToWorkoutFragment(workoutConfigModel)
            )
        }

        availableExercises.observe(viewLifecycleOwner) {
            binding.spExercise.apply {

                val arrayAdapter = ArrayAdapter<Any?>(
                    requireContext(),
                    R.layout.item_drop_down_list,
                    it
                )

                setAdapter(arrayAdapter)
            }
        }

        trainingMethodologies.observe(viewLifecycleOwner) {
            binding.spTrainingMethodology.apply {

                val arrayAdapter = ArrayAdapter<Any?>(
                    requireContext(),
                    R.layout.item_drop_down_list,
                    it
                )

                setAdapter(arrayAdapter)
            }
        }
    }

    private fun setupBlePermissionsHandlerCallbacks() = blePermissionsHandler.apply {

        setOnPermissionsAccepted {
            if (!bluetoothAdapter.isEnabled)
                turnOnBluetooth()
            else
                viewModel.bluetoothEnabled = true
        }

        setOnPermissionsDenied {
            setOnEnableBleDenied {
                Toast.makeText(
                    requireContext(),
                    "Toadas as permissões não foram aceitas",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setOnEnableBleAccepted {
            viewModel.bluetoothEnabled = true
        }

        setOnEnableBleDenied {
            Toast.makeText(
                requireContext(),
                "Solicitação de habilitar o Bluetooth foi negada",
                Toast.LENGTH_SHORT
            ).show()

            viewModel.bluetoothEnabled = false
        }
    }
}
