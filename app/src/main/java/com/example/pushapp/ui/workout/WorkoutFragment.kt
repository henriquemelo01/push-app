package com.example.pushapp.ui.workout

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.pushapp.NavigationDirections
import com.example.pushapp.R
import com.example.pushapp.databinding.FragmentWorkoutBinding
import com.example.pushapp.models.BluetoothConnectionStatus
import com.example.pushapp.models.detailed_report.AccesedBy
import com.example.pushapp.ui.workout.BluetoothHandlerViewModel.Companion.BLE_VELOCITY_CHARACTERISTIC_UUID
import com.example.pushapp.utils.flowObserver
import com.example.pushapp.utils.setupStyle
import com.example.pushapp.utils.showOnce
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class WorkoutFragment : BluetoothHandlerFragment() {

    private lateinit var binding: FragmentWorkoutBinding

    private val args: WorkoutFragmentArgs by navArgs()

    override val viewModel: WorkoutViewModel by viewModel {
        parametersOf(args.workoutConfigModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentWorkoutBinding.inflate(inflater, container, false).apply {
        binding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBind()

        setupLiveData()

        scanESP32()
    }

    private fun setupBind() = with(binding) {

        ctWorkoutScreenTitle.setLeftIconClickListener {
            requireActivity().onBackPressed()
        }

        btFinishExercise.setOnClickListener {

            viewModel.triggerDisconnectToESP32Event()

            val bsFinishedWorkout = FinishedWorkoutBottomSheet().apply {

                setOnSeeLaterClickListener {
                    /*
                        Persistir dados no Firebase e em sua calback de sucesso da peristência dos dados,
                        fechar a BottomSheet e navegar até a tela de Configuração do Treino
                     */
                    dismiss()

                    Toast
                        .makeText(requireContext(), "Ver depois clicado", Toast.LENGTH_SHORT)
                        .show()

                    requireActivity().onBackPressed()
                }

                setOnShowReportClickListener {
                    dismiss()
                    Toast
                        .makeText(requireContext(), "Exiber relatório clicado", Toast.LENGTH_SHORT)
                        .show()

                    viewModel.navigateToDetailedReportFragment()
                }

                setOnDiscardMeasuresClickListener {
                    dismiss()
                    requireActivity().onBackPressed()
                }
            }

            bsFinishedWorkout.showOnce(childFragmentManager)
        }
    }

    private fun setupLiveData() = with(viewModel) {

        title.observe(viewLifecycleOwner) {
            binding.ctWorkoutScreenTitle.title = it
        }

        weightData.observe(viewLifecycleOwner) {
            binding.tvWorkoutWeight.text = getString(R.string.workout_weight_label, it)
        }

        showVelocityWheel.observe(viewLifecycleOwner) { shouldShowVelocityWheel ->
            binding.clVelocityWheel.visibility =
                if (shouldShowVelocityWheel) View.VISIBLE else View.GONE
        }

        velocityWheelColor.observe(viewLifecycleOwner) { color ->
            ContextCompat.getDrawable(
                requireContext(), R.drawable.shape_collored_wheel
            )?.let { drawable ->
                (drawable as? GradientDrawable)?.let {
                    val background = it.apply { setStroke(8, color) }
                    binding.clVelocityWheel.background = DrawableCompat.wrap(background)
                }
            }
        }

        velocityData.observe(viewLifecycleOwner) {

            println("VelocityData: $it")

            binding.tvVelocityWheelData.text = getString(R.string.velocity_wheel_label, it)

            binding.tvVelocityData.text = getString(R.string.velocity_data_label, it)

            viewModel.saveData(BLE_VELOCITY_CHARACTERISTIC_UUID, it)

            // esta relacionado ao offset characteristic
            binding.progressBar.apply {
                progress = (it * 100).toInt()
                max = 100
            }
        }

        showBarPositionContainer.observe(viewLifecycleOwner) { shouldShowBarPosition ->
            binding.clWorkoutProgressBar.visibility =
                if (shouldShowBarPosition) View.VISIBLE else View.GONE
        }

        velocityEntries.observe(viewLifecycleOwner) { entries ->

            binding.lcVelocityData.apply {
                setupStyle()

                visibility = if (entries.isNotEmpty()) View.VISIBLE else View.GONE

                val dataSet =
                    LineDataSet(entries, POSITION_CHART_SUBTITLES_LABEL).setupStyle(context)

                data = LineData(dataSet)

                invalidate()
            }

            binding.tvPositionGraphTitle.visibility =
                if (entries.isNotEmpty()) View.VISIBLE else View.GONE
        }

        weightData.observe(viewLifecycleOwner) {
            binding.tvForceData.text = getString(R.string.force_data_label, it)
        }

        offsetData.observe(viewLifecycleOwner) {
            println("OffsetData: $it")
        }

        forceData.observe(viewLifecycleOwner) {
            println("ForceData: $it")
        }

        accelerationData.observe(viewLifecycleOwner) {
            println("AccelerationData: $it")
        }

        powerData.observe(viewLifecycleOwner) {
            println("PowerData: $it")
        }

        flowObserver(viewModel.statusDevice) { connectionStatus ->
            if (connectionStatus == BluetoothConnectionStatus.CONNECTED)
                binding.tvDeviceConnectedStatus.apply {
                    visibility = View.VISIBLE

                    text = getString(
                        R.string.device_connected_status_text,
                        deviceConnected?.name.orEmpty(),
                        connectionStatus.value
                    )
                }
            else
                binding.tvDeviceConnectedStatus.text = getString(
                    R.string.device_connected_status_text,
                    previousDeviceConnected?.name ?: "ESP32",
                    connectionStatus.value
                )
        }

        flowObserver(viewModel.navigateToDetailedReportEvent) { report ->
            findNavController().navigate(
                NavigationDirections.actionGlobalToDetailedReportFragment(
                    report,
                    AccesedBy.WORKOUT_FRAGMENT
                )
            )
        }
    }

    private companion object {
        const val POSITION_CHART_SUBTITLES_LABEL = "POSIÇÃO"
    }
}