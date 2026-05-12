package com.example.smartgasstation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartgasstation.viewModels.AddRefuelVM
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class AddRefuelActivity : AppCompatActivity() {

    private var lastOdometer: Double = -1.0
    private var averageConsumption: Double = 0.0

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_refuel)
        lastOdometer = intent.getDoubleExtra("last_odometer", -1.0)
        averageConsumption = intent.getDoubleExtra("average_consumption", 0.0)
        initializeViews()
        setupClickListeners()
        updateFuelConsumptionField(averageConsumption)
        observeState()
    }

    private fun initializeViews() {
        cancelSearchButton = findViewById(R.id.cancelSearchButton)
        cancelSearchButton.isEnabled = false
        fuelTypeInput = findViewById(R.id.fuelTypeInput)
        fuelAmountInput = findViewById(R.id.fuelAmountInput)
        fuelConsumptionInput = findViewById(R.id.fuelConsumptionInput)
        odometerInput = findViewById(R.id.odometerInput)
        resultText = findViewById(R.id.resultText)
        calculateButton = findViewById(R.id.calculateButton)
        refuelButton = findViewById(R.id.refuelButton)
        returnButton = findViewById(R.id.returnButton)
        fuelConsumptionInput.setText("10.0")
    }

    private fun setupClickListeners() {
        calculateButton.setOnClickListener { calculateBestGasStation() }
        refuelButton.setOnClickListener { recordRefuel() }
        returnButton.setOnClickListener { goToMainActivity() }
        cancelSearchButton.setOnClickListener {
            addRefuelVM.cancelSearch()
            resultText.text = "Запрос отменен"
            calculateButton.isEnabled = true
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

    private fun observeState() {
        lifecycleScope.launch {
            addRefuelVM.uiState.collectLatest { state ->
                when (state) {
                    is AddRefuelVM.UiState.Loading -> {
                        resultText.text = "Статус: обработка запроса"
                        calculateButton.isEnabled = false
                        cancelSearchButton.isEnabled = true
                    }
                    is AddRefuelVM.UiState.ServerResult -> {
                        val r = state.response
                        resultText.text = "Лучшая заправка: ${r.name}\n" +
                                "Цена за литр: ${r.pricePerLiter} руб.\n" +
                                "Расстояние: ${r.distance} км\n" +
                                "Стоимость поездки: ${String.format("%.2f", r.tripCost)} руб.\n" +
                                "Общая стоимость: ${String.format("%.2f", r.totalCost)} руб."
                        calculateButton.isEnabled = true
                        cancelSearchButton.isEnabled = false
                    }
                    is AddRefuelVM.UiState.LocalResult -> {
                        val r = state.result
                        resultText.text = "Сетевой запрос не выполнен. Используются локальные данные:\n" +
                                "Лучшая заправка: ${r.station.name}\n" +
                                "Цена за литр: ${r.station.fuelPrice} руб.\n" +
                                "Расстояние: ${r.station.distance} км\n" +
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

            if (odometer <= lastOdometer) {
                resultText.text = "Ошибка: текущий пробег ($odometer км) должен быть больше предыдущего ($lastOdometer км)"
                return
            }

            fuelAmountInput.text.clear()
            odometerInput.text.clear()
            goToMainActivity(fuelAmount, odometer)

        } catch (e: NumberFormatException) {
            resultText.text = "Некорректные числовые значения"
        }
    }

    private fun goToMainActivity(fuelAmount: Double, odometer: Double) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("fuel_amount", fuelAmount)
        intent.putExtra("odometer", odometer)
        startActivity(intent)
        finish()
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun updateFuelConsumptionField(averageConsumption: Double) {
        if (averageConsumption > 0) {
            fuelConsumptionInput.setText(String.format(Locale.US,"%.2f", averageConsumption))
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