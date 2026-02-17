package com.example.smartgasstation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartgasstation.data.BestStationResult
import com.example.smartgasstation.data.GasStation
import com.example.smartgasstation.data.RefuelHistory
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
    // Список заправок
    private val gasStations = listOf(
        GasStation(
            id = 1,
            name = "Лукойл №1",
            fuelPrice = 50.5,
            distance = 2.5,
            availableFuels = listOf("АИ-95", "АИ-92", "ДТ")
        ),
        GasStation(
            id = 2,
            name = "Роснефть №2",
            fuelPrice = 49.8,
            distance = 5.0,
            availableFuels = listOf("АИ-95", "АИ-92", "ДТ", "Газ")
        ),
        GasStation(
            id = 3,
            name = "Газпромнефть №3",
            fuelPrice = 51.2,
            distance = 1.0,
            availableFuels = listOf("АИ-95", "АИ-98", "ДТ")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_refuel)
        lastOdometer = intent.getDoubleExtra("last_odometer", -1.0)
        averageConsumption = intent.getDoubleExtra("average_consumption", 0.0)
        initializeViews()
        setupClickListeners()
        updateFuelConsumptionField(averageConsumption)
    }

    private fun initializeViews() {
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
        calculateButton.setOnClickListener {
            calculateBestGasStation()
        }

        refuelButton.setOnClickListener {
            recordRefuel()
        }

        returnButton.setOnClickListener {
            goToMainActivity()
        }
    }

    // Поиск лучшей заправки
    private fun findBestGasStation(fuelType: String, fuelAmount: Double, fuelConsumption: Double): BestStationResult? {
        // Фильтруем заправки по типу топлива
        val suitableStations = gasStations.filter { station ->
            station.availableFuels.any { it.contains(fuelType, ignoreCase = true) }
        }

        if (suitableStations.isEmpty()) return null

        // Рассчитываем стоимость для каждой подходящей заправки
        val stationsWithCost = suitableStations.map { station ->
            val fuelCost = station.fuelPrice * fuelAmount
            val tripFuel = (station.distance * fuelConsumption) / 100
            val tripCost = tripFuel * station.fuelPrice
            val totalCost = fuelCost + tripCost

            BestStationResult(station, tripCost, totalCost)
        }

        // Возвращаем заправку с минимальной общей стоимостью
        return stationsWithCost.minByOrNull { it.totalCost }
    }

    // Записать заправку
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

            // Проверяем, что пробег больше предыдущего
            if (odometer <= lastOdometer) {
                resultText.text = "Ошибка: текущий пробег ($odometer км) должен быть больше предыдущего ($lastOdometer км)"
                return
            }

            // Очищаем поля после успешной записи
            fuelAmountInput.text.clear()
            odometerInput.text.clear()

            // Передаём данные в главную активность и переходим в неё
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
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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

            // Поиск лучшей заправки
            val bestStation = findBestGasStation(fuelType, fuelAmount, consumption)

            if (bestStation != null) {
                val result = """
                    Лучшая заправка: ${bestStation.station.name}
                    Цена за литр: ${bestStation.station.fuelPrice} руб.
                    Расстояние: ${bestStation.station.distance} км
                    Стоимость поездки: ${String.format("%.2f", bestStation.tripCost)} руб.
                    Общая стоимость: ${String.format("%.2f", bestStation.totalCost)} руб.
                """.trimIndent()
                resultText.text = result
            } else {
                resultText.text = "Не найдено подходящих заправок"
            }

        } catch (e: NumberFormatException) {
            resultText.text = "Некорректные числовые значения"
        }
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
}