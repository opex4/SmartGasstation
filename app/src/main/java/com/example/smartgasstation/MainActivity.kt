package com.example.smartgasstation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartgasstation.data.RefuelHistory

class MainActivity : AppCompatActivity() {

    private lateinit var historyText: TextView

    private val refuelHistory = RefuelHistory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(intent.hasExtra("fuel_amount") && intent.hasExtra("odometer")){
            val fuelAmount = intent.getDoubleExtra("fuel_amount", 0.0)
            val odometer = intent.getDoubleExtra("odometer", -1.0)
            if(fuelAmount != 0.0 || odometer != -1.0){
                refuelHistory.addRefuelRecord(fuelAmount, odometer)
                Toast.makeText(this, "Заправка записана в историю", Toast.LENGTH_SHORT).show()
            }
        }

        initializeViews()
        updateHistoryDisplay()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.go_to_add_refuel_activity -> {
                goToAddRefuelActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun goToAddRefuelActivity() {
        val intent = Intent(this, AddRefuelActivity::class.java)
        intent.putExtra("average_consumption", refuelHistory.calculateAverageConsumption())
        intent.putExtra("last_odometer", refuelHistory.getLastOdometer())
        startActivity(intent)
    }

    private fun initializeViews() {
        historyText = findViewById(R.id.historyText)
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
}