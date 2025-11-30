package com.example.smartgasstation

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgasstation.data.GasStation
import com.example.smartgasstation.data.RefuelHistory
import com.example.smartgasstation.data.BestStationResult

class MainActivity : AppCompatActivity() {

    private lateinit var fuelTypeInput: EditText
    private lateinit var fuelAmountInput: EditText
    private lateinit var fuelConsumptionInput: EditText
    private lateinit var odometerInput: EditText
    private lateinit var resultText: TextView
    private lateinit var calculateButton: Button
    private lateinit var refuelButton: Button
    private lateinit var historyText: TextView

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

    private val refuelHistory = RefuelHistory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupClickListeners()
        updateHistoryDisplay()
        updateFuelConsumptionField()
    }

    private fun initializeViews() {
        fuelTypeInput = findViewById(R.id.fuelTypeInput)
        fuelAmountInput = findViewById(R.id.fuelAmountInput)
        fuelConsumptionInput = findViewById(R.id.fuelConsumptionInput)
        odometerInput = findViewById(R.id.odometerInput)
        resultText = findViewById(R.id.resultText)
        calculateButton = findViewById(R.id.calculateButton)
        refuelButton = findViewById(R.id.refuelButton)
        historyText = findViewById(R.id.historyText)

        fuelConsumptionInput.setText("10.0")
    }

    private fun setupClickListeners() {
        calculateButton.setOnClickListener {
            calculateBestGasStation()
        }

        refuelButton.setOnClickListener {
            recordRefuel()
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
            val lastOdometer = refuelHistory.getLastOdometer()
            if (lastOdometer != null && odometer <= lastOdometer) {
                resultText.text = "Ошибка: текущий пробег ($odometer км) должен быть больше предыдущего ($lastOdometer км)"
                return
            }

            refuelHistory.addRefuelRecord(fuelAmount, odometer)
            updateHistoryDisplay()
            updateFuelConsumptionField()
            resultText.text = "Заправка записана в историю"

            // Очищаем поля после успешной записи
            fuelAmountInput.text.clear()
            odometerInput.text.clear()

        } catch (e: NumberFormatException) {
            resultText.text = "Некорректные числовые значения"
        }
    }

    private fun updateHistoryDisplay() {
        val history = refuelHistory.getHistory()
        val averageConsumption = refuelHistory.calculateAverageConsumption()

        val historyTextBuilder = StringBuilder()
        historyTextBuilder.append("История заправок:\n")

        if (history.isEmpty()) {
            historyTextBuilder.append("Нет данных о заправках")
        } else {
            history.forEachIndexed { index, record ->
                historyTextBuilder.append("${index + 1}. Пробег: ${record.odometer} км, Топливо: ${record.fuelAmount} л\n")
            }

            historyTextBuilder.append("\nСредний расход: ")
            if (averageConsumption > 0) {
                historyTextBuilder.append(String.format("%.2f", averageConsumption))
                historyTextBuilder.append(" л/100км")
            } else {
                historyTextBuilder.append("Недостаточно данных")
            }
        }

        historyText.text = historyTextBuilder.toString()
    }

    private fun updateFuelConsumptionField() {
        val averageConsumption = refuelHistory.calculateAverageConsumption()

        if (averageConsumption > 0) {
            fuelConsumptionInput.setText(String.format("%.2f", averageConsumption))
            fuelConsumptionInput.isEnabled = false
            fuelConsumptionInput.setBackgroundColor(0xFFE8E8E8.toInt())
        } else {
            fuelConsumptionInput.isEnabled = true
            fuelConsumptionInput.setBackgroundColor(0xFFFFFFFF.toInt())
        }
    }
}