package com.example.smartgasstation.storages

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.smartgasstation.data.RefuelHistory
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class RefuelHistoryFileManager(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    // TXT
    fun saveToTxt(history: RefuelHistory, fileName: String) {
        val file = File(context.filesDir, "$fileName.txt")

        file.printWriter().use { writer ->
            history.getHistory().forEach {
                writer.println("${it.fuelAmount};${it.odometer};${it.timestamp}")
            }
        }
    }

    fun loadFromTxt(fileName: String): RefuelHistory {
        val file = File(context.filesDir, "$fileName.txt")
        val history = RefuelHistory
        RefuelHistory.clearHistory()

        if (!file.exists()) return history

        file.forEachLine { line ->
            val parts = line.split(";")
            if (parts.size == 3) {
                val fuel = parts[0].toDouble()
                val odo = parts[1].toDouble()
                history.addRefuelRecord(fuel, odo)
            }
        }

        return history
    }

    // XLS
    fun saveToXls(history: RefuelHistory, fileName: String) {
        val workbook: Workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Refuel History")

        val records = history.getHistory()

        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index)
            row.createCell(0).setCellValue(record.fuelAmount)
            row.createCell(1).setCellValue(record.odometer)
            row.createCell(2).setCellValue(dateFormat.format(Date(record.timestamp)))
        }

        val file = File(context.filesDir, "$fileName.xls")
        FileOutputStream(file).use {
            workbook.write(it)
        }
        workbook.close()
    }

    fun loadFromXls(fileName: String): RefuelHistory {
        val file = File(context.filesDir, "$fileName.xls")
        val history = RefuelHistory
        history.clearHistory()

        if (!file.exists()) return history

        FileInputStream(file).use { fis ->
            val workbook = HSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)

            for (row in sheet) {
                val fuel = row.getCell(0).numericCellValue
                val odo = row.getCell(1).numericCellValue
                history.addRefuelRecord(fuel, odo)
            }

            workbook.close()
        }

        return history
    }

    // PDF
    fun saveToPdf(history: RefuelHistory, fileName: String) {

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint()
        paint.textSize = 14f

        var yPosition = 40

        canvas.drawText("История заправок", 40f, yPosition.toFloat(), paint)
        yPosition += 30

        history.getHistory().forEach {
            val line = "Топливо: ${it.fuelAmount} л | Пробег: ${it.odometer} км | ${
                dateFormat.format(
                    Date(it.timestamp)
                )
            }"
            canvas.drawText(line, 40f, yPosition.toFloat(), paint)
            yPosition += 25
        }

        document.finishPage(page)

        val file = File(context.filesDir, "$fileName.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()
    }
}