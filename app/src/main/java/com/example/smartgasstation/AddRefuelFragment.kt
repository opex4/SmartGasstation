package com.example.smartgasstation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.smartgasstation.viewModels.AddRefuelVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class AddRefuelFragment : Fragment() {

    private lateinit var fuelTypeInput: EditText
    private lateinit var fuelAmountInput: EditText
    private lateinit var fuelConsumptionInput: EditText
    private lateinit var odometerInput: EditText
    private lateinit var resultText: TextView
    private lateinit var calculateButton: Button
    private lateinit var refuelButton: Button
    private lateinit var returnButton: Button
    private lateinit var cancelSearchButton: Button

    private val addRefuelVM: AddRefuelVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_refuel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupClickListeners()
        observeUiState()
        observeSaveState()
        observeConsumption()
    }

    private fun initializeViews(view: View) {
        cancelSearchButton = view.findViewById(R.id.cancelSearchButton)
        cancelSearchButton.isEnabled = false
        fuelTypeInput = view.findViewById(R.id.fuelTypeInput)
        fuelAmountInput = view.findViewById(R.id.fuelAmountInput)
        fuelConsumptionInput = view.findViewById(R.id.fuelConsumptionInput)
        odometerInput = view.findViewById(R.id.odometerInput)
        resultText = view.findViewById(R.id.resultText)
        calculateButton = view.findViewById(R.id.calculateButton)
        refuelButton = view.findViewById(R.id.refuelButton)
        returnButton = view.findViewById(R.id.returnButton)
        fuelConsumptionInput.setText("10.0")
    }

    private fun setupClickListeners() {
        calculateButton.setOnClickListener { calculateBestGasStation() }
        refuelButton.setOnClickListener { recordRefuel() }
        returnButton.setOnClickListener { parentFragmentManager.popBackStack() }
        cancelSearchButton.setOnClickListener {
            addRefuelVM.cancelSearch()
            resultText.text = "Запрос отменён"
            calculateButton.isEnabled = true
            cancelSearchButton.isEnabled = false
        }
    }

    private fun observeConsumption() {
        addRefuelVM.avgConsumption.observe(viewLifecycleOwner) { avg ->
            updateFuelConsumptionField(avg ?: 0.0)
        }
    }

    private fun calculateBestGasStation() {
        val fuelType = fuelTypeInput.text.toString().trim()
        val fuelAmountText = fuelAmountInput.text.toString().trim()
        val consumptionText = fuelConsumptionInput.text.toString().trim()

        if (fuelType.isEmpty() || fuelAmountText.isEmpty() || consumptionText.isEmpty()) {
            resultText.text = "Заполните все поля"
            return
        }

        try {
            val fuelAmount = fuelAmountText.toDouble()
            val consumption = consumptionText.toDouble()
            addRefuelVM.searchBestStation(fuelType, fuelAmount, consumption)
            calculateButton.isEnabled = false
            cancelSearchButton.isEnabled = true
        } catch (e: NumberFormatException) {
            resultText.text = "Некорректные числовые значения"
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            addRefuelVM.uiState.collectLatest { state ->
                when (state) {
                    is AddRefuelVM.UiState.Loading -> {
                        resultText.text = "Статус: обработка запроса"
                        calculateButton.isEnabled = false
                        cancelSearchButton.isEnabled = true
                    }
                    is AddRefuelVM.UiState.Success -> {
                        val r = state.response
                        resultText.text = "Лучшая заправка: ${r.name}\n" +
                                "Цена за литр: ${r.pricePerLiter} руб.\n" +
                                "Расстояние: ${r.distance} км\n" +
                                "Стоимость поездки: ${String.format("%.2f", r.tripCost)} руб.\n" +
                                "Общая стоимость: ${String.format("%.2f", r.totalCost)} руб."
                        calculateButton.isEnabled = true
                        cancelSearchButton.isEnabled = false
                    }
                    is AddRefuelVM.UiState.Error -> {
                        resultText.text = "Ошибка: ${state.message}\nЗаправки не найдены"
                        calculateButton.isEnabled = true
                        cancelSearchButton.isEnabled = false
                    }
                    is AddRefuelVM.UiState.Idle -> {
                        calculateButton.isEnabled = true
                        cancelSearchButton.isEnabled = false
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
                        resultText.text = state.message
                        addRefuelVM.resetSaveState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun recordRefuel() {
        val fuelAmountText = fuelAmountInput.text.toString().trim()
        val odometerText = odometerInput.text.toString().trim()

        if (fuelAmountText.isEmpty() || odometerText.isEmpty()) {
            resultText.text = "Введите количество топлива и пробег"
            return
        }

        try {
            val fuelAmount = fuelAmountText.toDouble()
            val odometer = odometerText.toDouble()
            fuelAmountInput.text.clear()
            odometerInput.text.clear()
            addRefuelVM.saveRefuel(fuelAmount, odometer)
        } catch (e: NumberFormatException) {
            resultText.text = "Некорректные числовые значения"
        }
    }

    private fun updateFuelConsumptionField(averageConsumption: Double) {
        if (averageConsumption > 0) {
            fuelConsumptionInput.setText(String.format(Locale.US, "%.2f", averageConsumption))
            fuelConsumptionInput.isEnabled = false
            fuelConsumptionInput.setBackgroundColor(0xFFE8E8E8.toInt())
        } else {
            fuelConsumptionInput.isEnabled = true
            fuelConsumptionInput.setBackgroundColor(0xFFFFFFFF.toInt())
        }
    }

    override fun onStop() {
        super.onStop()
        addRefuelVM.cancelSearch()
        calculateButton.isEnabled = true
        cancelSearchButton.isEnabled = false
    }
}
