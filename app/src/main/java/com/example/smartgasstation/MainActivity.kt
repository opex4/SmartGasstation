package com.example.smartgasstation

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgasstation.adapters.MainAdapter
import com.example.smartgasstation.data.RefuelRecord
import com.example.smartgasstation.adapters.SwipeToActionCallback
import com.example.smartgasstation.viewModels.MainVM
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var consumptionText: TextView

    private val mainVM: MainVM by viewModels()
    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initObserves()

        if(intent.hasExtra("fuel_amount") && intent.hasExtra("odometer")){
            val fuelAmount = intent.getDoubleExtra("fuel_amount", 0.0)
            val odometer = intent.getDoubleExtra("odometer", -1.0)
            if(fuelAmount != 0.0 || odometer != -1.0){
                try {
                    mainVM.addRefuelRecord(fuelAmount, odometer)
                    Toast.makeText(this, "Заправка записана в историю", Toast.LENGTH_SHORT).show()
                } catch (e: Exception){
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Ошибка сохранения записи", Toast.LENGTH_SHORT).show()
            }
        }

        initializeViews()
    }

    private fun initObserves() {
        mainVM.refuelRecords.observe(this) { records ->
            updateHistoryDisplay(records)
        }
    }

    private fun setupSwipeCallbacks() {
        val callback = SwipeToActionCallback(
            onSwipeUp = { position ->
                adapter.updateRecycler()
                showEditDialog(position)
            },
            onSwipeDown = { position ->
                adapter.updateRecycler()
                AlertDialog.Builder(this)
                    .setTitle("Подтверждение")
                    .setMessage("Вы действительно хотите удалить запись о заправке?")
                    .setPositiveButton("Да") { _, _ ->
                        try {
                            mainVM.deleteRefuelRecord(position)
                        } catch (e: Exception){
                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Нет", null)
                    .show()
            }
        )

        ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
    }

    private fun showEditDialog(position: Int) {
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_edit_refuel, null)
        val fuelInput = dialogLayout.findViewById<TextInputEditText>(R.id.et_fuel_amount)
        val odometerInput = dialogLayout.findViewById<TextInputEditText>(R.id.et_odometer)

        val record = adapter.getCurrentRefuelRecord(position)
        fuelInput.setText(record.fuelAmount.toString())
        odometerInput.setText(record.odometer.toString())

        AlertDialog.Builder(this)
            .setTitle("Редактирование записи")
            .setView(dialogLayout)
            .setPositiveButton("Сохранить") {dialog, _ ->
                val fuelStr = fuelInput.text.toString()
                val odometerStr = odometerInput.text.toString()

                when {
                    fuelStr.isEmpty() || odometerStr.isEmpty() -> {
                        Toast.makeText(this@MainActivity, "Заполните все поля", Toast.LENGTH_SHORT).show()
                    }
                    else -> try {
                        val fuelDouble = fuelStr.toDouble()
                        val odometerDouble = odometerStr.toDouble()

                        mainVM.updateRefuelRecord(position, fuelDouble, odometerDouble)
                        dialog.dismiss()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(this@MainActivity, "Введите корректные числа", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
            R.id.clear_refuel_history -> {
                clearRefuelHistory()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun clearRefuelHistory() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение")
            .setMessage("Вы действительно хотите очистить всю историю заправок?")
            .setPositiveButton("Да") { _, _ ->
                mainVM.clearRefuelHistory()
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun goToAddRefuelActivity() {
        val intent = Intent(this, AddRefuelActivity::class.java)

        try {
            val avgConsumption = mainVM.calculateAvgConsumption()
            intent.putExtra("average_consumption", avgConsumption)
        } catch (e: Exception){
            intent.putExtra("average_consumption", 0.0)
        }

        try {
            val lastOdometer = mainVM.getLastOdometer()
            intent.putExtra("last_odometer", lastOdometer)
        } catch (e: Exception){
            intent.putExtra("last_odometer", -1.0)
        }

        startActivity(intent)
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.main_recycler_view)
        consumptionText = findViewById(R.id.main_activity_consumption_tv)
        consumptionText.text = mainVM.calculateAverageConsumption()
    }

    private fun updateHistoryDisplay(updatedList: List<RefuelRecord>) {
        // Инициализирован ли адаптер
        if (!::adapter.isInitialized) {
            adapter = MainAdapter(updatedList)
            recyclerView.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapter
            setupSwipeCallbacks()
        } else {
            adapter.updateData(updatedList)
        }

        // Средний расход
        consumptionText.text = mainVM.calculateAverageConsumption()
    }
}