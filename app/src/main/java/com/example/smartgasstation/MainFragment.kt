package com.example.smartgasstation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgasstation.adapters.MainAdapter
import com.example.smartgasstation.adapters.SwipeToActionCallback
import com.example.smartgasstation.domain.entity.RefuelRecord
import com.example.smartgasstation.viewModels.MainVM
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var consumptionText: TextView
    private val mainVM: MainVM by viewModels()
    private lateinit var adapter: MainAdapter
    private lateinit var filesButton: ImageButton
    private lateinit var coroutineButton: Button
    private lateinit var threadButton: Button

    private var dialogProgressBar: ProgressBar? = null
    private var startButton: Button? = null
    private var cancelButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        initializeViews(view)
        initObserves()
    }

    override fun onStart() {
        super.onStart()
        mainVM.fetchRecords()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.go_to_add_refuel_activity -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, AddRefuelFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    R.id.clear_refuel_history -> {
                        clearRefuelHistory()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initObserves() {
        mainVM.refuelRecords.observe(viewLifecycleOwner) { records ->
            updateHistoryDisplay(records)
        }

        mainVM.avgConsumption.observe(viewLifecycleOwner) { avg ->
            if (avg == null) {
                consumptionText.text = "Недостаточно данных для расчёта среднего расхода"
            } else {
                consumptionText.text = "Средний расход: %.2f л/100км".format(avg)
            }
        }

        mainVM.progress.observe(viewLifecycleOwner) { value ->
            when (value) {
                100 -> {
                    dialogProgressBar?.progress = value
                    cancelButton?.isEnabled = false
                    Toast.makeText(requireContext(), "Файлы txt и xls успешно сохранены", Toast.LENGTH_SHORT).show()
                }
                -1 -> {
                    cancelButton?.isEnabled = false
                    Toast.makeText(requireContext(), "Ошибка при экспорте файлов", Toast.LENGTH_LONG).show()
                }
                else -> {
                    dialogProgressBar?.progress = value
                }
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
                AlertDialog.Builder(requireContext())
                    .setTitle("Подтверждение")
                    .setMessage("Вы действительно хотите удалить запись о заправке?")
                    .setPositiveButton("Да") { _, _ ->
                        try {
                            val record = adapter.getCurrentRefuelRecord(position)
                            mainVM.deleteRefuelRecord(record)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
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

        AlertDialog.Builder(requireContext())
            .setTitle("Редактирование записи")
            .setView(dialogLayout)
            .setPositiveButton("Сохранить") { dialog, _ ->
                val fuelStr = fuelInput.text.toString()
                val odometerStr = odometerInput.text.toString()
                when {
                    fuelStr.isEmpty() || odometerStr.isEmpty() -> {
                        Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                    }
                    else -> try {
                        val fuelDouble = fuelStr.toDouble()
                        val odometerDouble = odometerStr.toDouble()
                        val updatedRecord = adapter.getCurrentRefuelRecord(position)
                        mainVM.updateRefuelRecord(updatedRecord, fuelDouble, odometerDouble)
                        dialog.dismiss()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Введите корректные числа", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun clearRefuelHistory() {
        AlertDialog.Builder(requireContext())
            .setTitle("Подтверждение")
            .setMessage("Вы действительно хотите очистить всю историю заправок?")
            .setPositiveButton("Да") { _, _ -> mainVM.clearRefuelHistory() }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun initializeViews(view: View) {
        recyclerView = view.findViewById(R.id.main_recycler_view)
        consumptionText = view.findViewById(R.id.main_activity_consumption_tv)
        consumptionText.text = "Нет данных о заправках"

        filesButton = view.findViewById(R.id.main_activity_files_btn)
        filesButton.setOnClickListener { saveAndLoadFiles() }

        threadButton = view.findViewById(R.id.thread_btn)
        threadButton.setOnClickListener {
            val v = layoutInflater.inflate(R.layout.dialog_export_progress, null)
            dialogProgressBar = v.findViewById(R.id.exportProgress)
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Потоки")
                .setCancelable(false)
                .setView(v)
                .setMessage("Сохранить в txt и xls?")
                .setPositiveButton("Старт", null)
                .setNeutralButton("Выйти") { _, _ -> dialogProgressBar = null }
                .setNegativeButton("Отмена", null)
                .show()
            startButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton?.isEnabled = false
            startButton?.setOnClickListener {
                startButton?.isEnabled = false
                cancelButton?.isEnabled = true
                mainVM.startThreadExport()
            }
            cancelButton?.setOnClickListener {
                cancelButton?.isEnabled = false
                mainVM.cancelThreadExport()
            }
        }

        coroutineButton = view.findViewById(R.id.coroutine_btn)
        coroutineButton.setOnClickListener {
            val v = layoutInflater.inflate(R.layout.dialog_export_progress, null)
            dialogProgressBar = v.findViewById(R.id.exportProgress)
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Корутины")
                .setCancelable(false)
                .setView(v)
                .setMessage("Сохранить в txt и xls?")
                .setPositiveButton("Старт", null)
                .setNeutralButton("Выйти") { _, _ -> dialogProgressBar = null }
                .setNegativeButton("Отмена", null)
                .show()
            startButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelButton?.isEnabled = false
            startButton?.setOnClickListener {
                startButton?.isEnabled = false
                cancelButton?.isEnabled = true
                mainVM.startCoroutineExport()
            }
            cancelButton?.setOnClickListener {
                cancelButton?.isEnabled = false
                mainVM.cancelCoroutineExport()
            }
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun saveAndLoadFiles() {
        val layout = layoutInflater.inflate(R.layout.dialog_work_with_files, null)
        val saveToTxt = layout.findViewById<Button>(R.id.btnSaveTxt)
        val saveToXls = layout.findViewById<Button>(R.id.btnSaveXls)
        val saveToPdf = layout.findViewById<Button>(R.id.btnSavePdf)
        val loadFromTxt = layout.findViewById<Button>(R.id.btnLoadTxt)
        val loadFromXls = layout.findViewById<Button>(R.id.btnLoadXls)

        AlertDialog.Builder(requireContext())
            .setTitle("Работа с файлами")
            .setView(layout)
            .show()
            .apply {
                saveToTxt.setOnClickListener {
                    mainVM.saveToTxt()
                    Toast.makeText(requireContext(), "Файл txt сохранён", Toast.LENGTH_SHORT).show()
                }
                saveToXls.setOnClickListener {
                    mainVM.saveToXls()
                    Toast.makeText(requireContext(), "Файл xls сохранён", Toast.LENGTH_SHORT).show()
                }
                saveToPdf.setOnClickListener {
                    mainVM.saveToPdf()
                    Toast.makeText(requireContext(), "Файл pdf сохранён", Toast.LENGTH_SHORT).show()
                }
                loadFromTxt.setOnClickListener {
                    mainVM.loadFromTxt()
                    Toast.makeText(requireContext(), "Файл txt загружен", Toast.LENGTH_SHORT).show()
                }
                loadFromXls.setOnClickListener {
                    mainVM.loadFromXls()
                    Toast.makeText(requireContext(), "Файл xls загружен", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateHistoryDisplay(updatedList: List<RefuelRecord>) {
        if (!::adapter.isInitialized) {
            adapter = MainAdapter(updatedList)
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapter
            setupSwipeCallbacks()
        } else {
            adapter.updateData(updatedList)
        }
    }
}
