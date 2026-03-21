package com.example.smartgasstation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgasstation.adapters.MainAdapter
import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.adapters.SwipeToActionCallback
import com.example.smartgasstation.viewModels.MainVM
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var consumptionText: TextView

    private val mainVM: MainVM by viewModels()
    private lateinit var adapter: MainAdapter
    private lateinit var filesButton: ImageButton
    private lateinit var coroutineButton: Button
    private lateinit var threadButton: Button

    private var lastOdometer: Double? = null
    private var avgConsumption: Double? = null

    private var dialogProgressBar: ProgressBar? = null
    private var startButton: Button? = null
    private var cancelButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initializeViews()
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
    }

    private fun initObserves() {
        mainVM.refuelRecords.observe(this) { records ->
            updateHistoryDisplay(records)
        }

        mainVM.lastOdometer.observe(this) {
            lastOdometer = it
        }

        mainVM.avgConsumption.observe(this) { avg ->
            // Средний расход
            avgConsumption = avg
            if (avg == null) {
                consumptionText.text = "Нет данных о заправках"
            } else {
                consumptionText.text =
                    "Средний расход: %.2f л/100км".format(avg)
            }
        }

        mainVM.progress.observe(this) { value ->
            dialogProgressBar?.progress = value

            if (value == 100) {
                cancelButton?.isEnabled = false
                Toast.makeText(this@MainActivity, "Файлы txt и xls успешно сохранены", Toast.LENGTH_SHORT).show()
            }
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
                            val record = adapter.getCurrentRefuelRecord(position)
                            mainVM.deleteRefuelRecord(record)
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

                        val record = adapter.getCurrentRefuelRecord(position)

                        mainVM.updateRefuelRecord(
                            record,
                            fuelDouble,
                            odometerDouble
                        )

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
        intent.putExtra(
            "average_consumption",
            mainVM.avgConsumption.value ?: 0.0
        )
        intent.putExtra(
            "last_odometer",
            mainVM.lastOdometer.value ?: -1.0
        )
        startActivity(intent)
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.main_recycler_view)
        consumptionText = findViewById(R.id.main_activity_consumption_tv)
        consumptionText.text = "Нет данных о заправках"
        filesButton = findViewById(R.id.main_activity_files_btn)
        filesButton.setOnClickListener {
            saveAndLoadFiles()
        }

        threadButton = findViewById(R.id.thread_btn)
        threadButton.setOnClickListener{
            val view = layoutInflater.inflate(R.layout.dialog_export_progress, null)
            dialogProgressBar = view.findViewById(R.id.exportProgress)

            val dialog = AlertDialog.Builder(this)
                .setTitle("Потоки")
                .setCancelable(false)
                .setView(view)
                .setMessage("Сохранить в txt и xls?")
                .setPositiveButton("Старт", null)
                .setNeutralButton("Выйти"){_, _ ->
                    dialogProgressBar = null
                }
                .setNegativeButton("Отмена", null)
                .show()

            startButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton?.isEnabled = false

            startButton?.setOnClickListener {
                startButton?.isEnabled = false
                cancelButton?.isEnabled = true
                try {
                    mainVM.startThreadExport()
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            cancelButton?.setOnClickListener {
                cancelButton?.isEnabled = false
                mainVM.cancelThreadExport()
            }
        }

        coroutineButton = findViewById(R.id.coroutine_btn)
        coroutineButton.setOnClickListener{
            val view = layoutInflater.inflate(R.layout.dialog_export_progress, null)
            dialogProgressBar = view.findViewById(R.id.exportProgress)

            val dialog = AlertDialog.Builder(this)
                .setTitle("Корутины")
                .setCancelable(false)
                .setView(view)
                .setMessage("Сохранить в txt и xls?")
                .setPositiveButton("Старт", null)
                .setNeutralButton("Выйти") { _, _ ->
                    dialogProgressBar = null
                }
                .setNegativeButton("Отмена", null)
                .show()

            startButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton?.isEnabled = false

            startButton?.setOnClickListener {
                startButton?.isEnabled = false
                cancelButton?.isEnabled = true
                try {
                    mainVM.startCoroutineExport()
                } catch (e: Exception) {
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
            }
            cancelButton?.setOnClickListener {
                cancelButton?.isEnabled = false
                mainVM.cancelCoroutineExport()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun saveAndLoadFiles() {
        val saveAndLoadFilesLayout = layoutInflater.inflate(R.layout.dialog_work_with_files, null)

        val saveToTxt = saveAndLoadFilesLayout.findViewById<Button>(R.id.btnSaveTxt)
        val saveToXls = saveAndLoadFilesLayout.findViewById<Button>(R.id.btnSaveXls)
        val saveToPdf = saveAndLoadFilesLayout.findViewById<Button>(R.id.btnSavePdf)

        val loadFromTxt = saveAndLoadFilesLayout.findViewById<Button>(R.id.btnLoadTxt)
        val loadFromXls = saveAndLoadFilesLayout.findViewById<Button>(R.id.btnLoadXls)

        AlertDialog.Builder(this)
            .setTitle("Работа с файлами")
            .setView(saveAndLoadFilesLayout)
            .show()
            .apply {
                saveToTxt.setOnClickListener {
                    try {
                        mainVM.saveToTxt()
                        Toast.makeText(this@MainActivity, "Файл txt сохранён", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                saveToXls.setOnClickListener {
                    try {
                        mainVM.saveToXls()
                        Toast.makeText(this@MainActivity, "Файл xls сохранён", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                saveToPdf.setOnClickListener {
                    try {
                        mainVM.saveToPdf()
                        Toast.makeText(this@MainActivity, "Файл pdf сохранён", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                loadFromTxt.setOnClickListener {
                    try {
                        mainVM.loadFromTxt()
                        Toast.makeText(this@MainActivity, "Файл txt загружен", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                loadFromXls.setOnClickListener {
                    try {
                        mainVM.loadFromXls()
                        Toast.makeText(this@MainActivity, "Файл xls загружен", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception){
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun updateHistoryDisplay(updatedList: List<RefuelRecordEntity>) {
        // Инициализирован ли адаптер
        if (!::adapter.isInitialized) {
            adapter = MainAdapter(updatedList)
            recyclerView.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapter
            setupSwipeCallbacks()
        } else {
            adapter.updateData(updatedList)
        }
    }
}