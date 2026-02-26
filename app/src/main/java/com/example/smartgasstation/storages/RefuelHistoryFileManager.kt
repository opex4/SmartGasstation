package com.example.smartgasstation.storages

import android.content.Context
import com.example.smartgasstation.data.RefuelHistory
import com.example.smartgasstation.data.RefuelRecord
import com.itextpdf.kernel.pdf.*
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import java.io.*

class RefuelHistoryFileManager(private val context: Context) {

    // txt файл
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
        val history = RefuelHistory()

        if (!file.exists()) return history

        file.forEachLine { line ->
            val parts = line.split(";")
            if (parts.size == 3) {
                val fuel = parts[0].toDouble()
                val odo = parts[1].toDouble()
                val timestamp = parts[2].toLong()

                history.addRecordFromFile(
                    RefuelRecord(fuel, odo, timestamp)
                )
            }
        }

        return history
    }

    // xls файл
    fun saveToXls(history: RefuelHistory, fileName: String) {

        val workbook = HSSFWorkbook()
        val sheet = workbook.createSheet("RefuelHistory")

        history.getHistory().forEachIndexed { index, record ->
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

    fun loadFromXls(fileName: String): RefuelHistory {

        val file = File(context.filesDir, "$fileName.xls")
        val history = RefuelHistory()

        if (!file.exists()) return history

        FileInputStream(file).use { fis ->
            val workbook = HSSFWorkbook(fis)
            val sheet = workbook.getSheetAt(0)

            for (row in sheet) {
                val fuel = row.getCell(0).numericCellValue
                val odo = row.getCell(1).numericCellValue
                val timestamp = row.getCell(2).numericCellValue.toLong()

                history.addRecordFromFile(
                    RefuelRecord(fuel, odo, timestamp)
                )
            }

            workbook.close()
        }

        return history
    }

    // pdf файл
    fun saveToPdf(history: RefuelHistory, fileName: String) {

        val file = File(context.filesDir, "$fileName.pdf")
        val writer = PdfWriter(file)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        document.add(Paragraph("REFUEL_HISTORY_START"))

        history.getHistory().forEach {
            document.add(
                Paragraph("${it.fuelAmount};${it.odometer};${it.timestamp}")
            )
        }

        document.add(Paragraph("REFUEL_HISTORY_END"))

        document.close()
    }

    fun loadFromPdf(fileName: String): RefuelHistory {

        val file = File(context.filesDir, "$fileName.pdf")
        val history = RefuelHistory()

        if (!file.exists()) return history

        val pdf = PdfDocument(PdfReader(file))
        val text = StringBuilder()

        for (i in 1..pdf.numberOfPages) {
            text.append(
                PdfTextExtractor.getTextFromPage(pdf.getPage(i))
            )
        }

        pdf.close()

        val lines = text.toString().split("\n")
        var reading = false

        for (line in lines) {

            when {
                line.contains("REFUEL_HISTORY_START") -> {
                    reading = true
                    continue
                }
                line.contains("REFUEL_HISTORY_END") -> break
            }

            if (reading) {
                val parts = line.trim().split(";")
                if (parts.size == 3) {

                    val fuel = parts[0].toDoubleOrNull()
                    val odo = parts[1].toDoubleOrNull()
                    val timestamp = parts[2].toLongOrNull()

                    if (fuel != null && odo != null && timestamp != null) {
                        history.addRecordFromFile(
                            RefuelRecord(fuel, odo, timestamp)
                        )
                    }
                }
            }
        }

        return history
    }
}