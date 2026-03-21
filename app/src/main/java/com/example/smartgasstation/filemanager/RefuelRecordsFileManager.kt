package com.example.smartgasstation.filemanager

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.smartgasstation.data.RefuelRecordEntity
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class RefuelRecordsFileManager(private val context: Context) {
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    // TXT
    fun saveToTxt(records: List<RefuelRecordEntity>, fileName: String) {
        val file = File(context.filesDir, "$fileName.txt")

        file.printWriter().use { writer ->
            records.forEach {
                writer.println("${it.fuelAmount};${it.odometer};${it.timestamp}")
            }
        }
    }

    fun loadFromTxt(fileName: String): List<RefuelRecordEntity> {
        val file = File(context.filesDir, "$fileName.txt")
        val records = mutableListOf<RefuelRecordEntity>()

        if (!file.exists()) return records

        file.forEachLine { line ->
            val parts = line.split(";")
            if (parts.size == 3) {

                val fuel = parts[0].toDouble()
                val odo = parts[1].toDouble()
                val timestamp = parts[2].toLong()

                records.add(
                    RefuelRecordEntity(
                        fuelAmount = fuel,
                        odometer = odo,
                        timestamp = timestamp
                    )
                )
            }
        }
        return records
    }

    // XLS
    fun saveToXls(records: List<RefuelRecordEntity>, fileName: String) {
        val workbook: Workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("Refuel History")

        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index)

            row.createCell(0).setCellValue(record.fuelAmount)
            row.createCell(1).setCellValue(record.odometer)
            row.createCell(2).setCellValue(record.timestamp.toDouble())
        }

        val file = File(context.filesDir, "$fileName.xls")
        FileOutputStream(file).use {
            workbook.write(it)
        }

        workbook.close()
    }

    fun loadFromXls(fileName: String): List<RefuelRecordEntity> {
        val file = File(context.filesDir, "$fileName.xls")
        val records = mutableListOf<RefuelRecordEntity>()

        if (!file.exists()) return records

        FileInputStream(file).use { fis ->
            val workbook = HSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)

            for (row in sheet) {
                val fuel = row.getCell(0).numericCellValue
                val odo = row.getCell(1).numericCellValue
                val timestamp = row.getCell(2).numericCellValue.toLong()

                records.add(
                    RefuelRecordEntity(
                        fuelAmount = fuel,
                        odometer = odo,
                        timestamp = timestamp
                    )
                )
            }
            workbook.close()
        }
        return records
    }

    // PDF
    fun saveToPdf(records: List<RefuelRecordEntity>, fileName: String) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.textSize = 14f
        var yPosition = 40
        canvas.drawText("История заправок", 40f, yPosition.toFloat(), paint)
        yPosition += 30
        records.forEach {
            val line =
                "Топливо: ${it.fuelAmount} л | Пробег: ${it.odometer} км | ${
                    dateFormat.format(Date(it.timestamp))
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