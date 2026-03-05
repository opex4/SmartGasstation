package com.example.smartgasstation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartgasstation.R
import com.example.smartgasstation.data.RefuelRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainAdapter(
    private var refuelRecords: List<RefuelRecordEntity>
) : RecyclerView.Adapter<MainAdapter.MyViewHolder>() {

    fun updateData(newRecords: List<RefuelRecordEntity>) {
        refuelRecords = newRecords
        notifyDataSetChanged()
    }

    fun updateRecycler() {
        notifyDataSetChanged()
    }

    fun getCurrentRefuelRecord(position: Int): RefuelRecordEntity {
        return refuelRecords[position]
    }

    // Создание ViewHolder - вызывается когда нужно создать новый элемент списка
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.main_item_recycler_view, parent, false)
        return MyViewHolder(view)
    }

    // Привязка данных к ViewHolder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val record = refuelRecords[position]
        holder.bind(record)
    }

    override fun getItemCount() = refuelRecords.size

    // Внутренний класс ViewHolder
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fuelAmountTV: TextView = itemView.findViewById(R.id.tv_fuel_amount)
        private val odometerTV: TextView = itemView.findViewById(R.id.tv_odometer)
        private val dateTV: TextView = itemView.findViewById(R.id.tv_date)

        fun bind(record: RefuelRecordEntity) {
            fuelAmountTV.text = String.format("Залито: %.2f л", record.fuelAmount)
            odometerTV.text = String.format("Пробег: %.2f км", record.odometer)
            dateTV.text = "Дата: ${formatDate(record.timestamp)}"
        }

        private fun formatDate(timestamp: Long): String ? {
            val date = Date(timestamp)
            // Полная дата и время
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return format.format(date)
        }
    }
}