package com.example.smartgasstation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.smartgasstation.databinding.FragmentAddRefuelBinding
import com.example.smartgasstation.viewModels.AddRefuelVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddRefuelFragment : Fragment() {

    private var _binding: FragmentAddRefuelBinding? = null
    private val binding get() = _binding!!

    private val addRefuelVM: AddRefuelVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRefuelBinding.inflate(inflater, container, false)
        binding.viewModel = addRefuelVM
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews()
        setupClickListeners()
        observeUiState()
        observeSaveState()
        observeConsumption()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeViews() {
        binding.cancelSearchButton.isEnabled = false
    }

    private fun setupClickListeners() {
        binding.calculateButton.setOnClickListener { calculateBestGasStation() }
        binding.refuelButton.setOnClickListener { recordRefuel() }
        binding.returnButton.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.cancelSearchButton.setOnClickListener {
            addRefuelVM.cancelSearch()
            binding.resultText.text = "Запрос отменён"
            binding.calculateButton.isEnabled = true
            binding.cancelSearchButton.isEnabled = false
        }
    }

    private fun observeConsumption() {
        addRefuelVM.avgConsumption.observe(viewLifecycleOwner) { avg ->
            updateConsumptionFieldState(avg ?: 0.0)
        }
    }

    private fun calculateBestGasStation() {
        val fuelType = binding.fuelTypeInput.text.toString().trim()
        val fuelAmountText = binding.fuelAmountInput.text.toString().trim()
        val consumptionText = binding.fuelConsumptionInput.text.toString().trim()

        if (fuelType.isEmpty() || fuelAmountText.isEmpty() || consumptionText.isEmpty()) {
            binding.resultText.text = "Заполните все поля"
            return
        }

        try {
            val fuelAmount = fuelAmountText.toDouble()
            val consumption = consumptionText.toDouble()
            addRefuelVM.searchBestStation(fuelType, fuelAmount, consumption)
            binding.calculateButton.isEnabled = false
            binding.cancelSearchButton.isEnabled = true
        } catch (e: NumberFormatException) {
            binding.resultText.text = "Некорректные числовые значения"
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            addRefuelVM.uiState.collectLatest { state ->
                when (state) {
                    is AddRefuelVM.UiState.Loading -> {
                        binding.resultText.text = "Статус: обработка запроса"
                        binding.calculateButton.isEnabled = false
                        binding.cancelSearchButton.isEnabled = true
                    }
                    is AddRefuelVM.UiState.Success -> {
                        val r = state.response
                        binding.resultText.text = "Лучшая заправка: ${r.name}\n" +
                                "Цена за литр: ${r.pricePerLiter} руб.\n" +
                                "Расстояние: ${r.distance} км\n" +
                                "Стоимость поездки: ${String.format("%.2f", r.tripCost)} руб.\n" +
                                "Общая стоимость: ${String.format("%.2f", r.totalCost)} руб."
                        binding.calculateButton.isEnabled = true
                        binding.cancelSearchButton.isEnabled = false
                    }
                    is AddRefuelVM.UiState.Error -> {
                        binding.resultText.text = "Ошибка: ${state.message}\nЗаправки не найдены"
                        binding.calculateButton.isEnabled = true
                        binding.cancelSearchButton.isEnabled = false
                    }
                    is AddRefuelVM.UiState.Idle -> {
                        binding.calculateButton.isEnabled = true
                        binding.cancelSearchButton.isEnabled = false
                    }
                }
            }
        }
    }

    private fun observeSaveState() {
        viewLifecycleOwner.lifecycleScope.launch {
            addRefuelVM.saveState.collectLatest { state ->
                when (state) {
                    is AddRefuelVM.SaveState.Success -> {
                        Toast.makeText(requireContext(), "Заправка записана в историю", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    is AddRefuelVM.SaveState.Error -> {
                        binding.resultText.text = state.message
                        addRefuelVM.resetSaveState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun recordRefuel() {
        val fuelAmountText = binding.fuelAmountInput.text.toString().trim()
        val odometerText = binding.odometerInput.text.toString().trim()

        if (fuelAmountText.isEmpty() || odometerText.isEmpty()) {
            binding.resultText.text = "Введите количество топлива и пробег"
            return
        }

        try {
            val fuelAmount = fuelAmountText.toDouble()
            val odometer = odometerText.toDouble()
            binding.fuelAmountInput.text?.clear()
            binding.odometerInput.text?.clear()
            addRefuelVM.saveRefuel(fuelAmount, odometer)
        } catch (e: NumberFormatException) {
            binding.resultText.text = "Некорректные числовые значения"
        }
    }

    private fun updateConsumptionFieldState(averageConsumption: Double) {
        if (averageConsumption > 0) {
            binding.fuelConsumptionInput.isEnabled = false
            binding.fuelConsumptionInput.setBackgroundColor(0xFFE8E8E8.toInt())
        } else {
            binding.fuelConsumptionInput.isEnabled = true
            binding.fuelConsumptionInput.setBackgroundColor(0xFFFFFFFF.toInt())
        }
    }

    override fun onStop() {
        super.onStop()
        addRefuelVM.cancelSearch()
        binding.calculateButton.isEnabled = true
        binding.cancelSearchButton.isEnabled = false
    }
}
