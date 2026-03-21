package com.example.smartgasstation.multithreading

import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.filemanager.RefuelRecordsFileManager
import java.util.concurrent.atomic.AtomicBoolean

class ThreadManager(
    private val fileManager: RefuelRecordsFileManager
) {
    private var threadTxt: Thread? = null
    private var threadXls: Thread? = null

    private val isCancelled = AtomicBoolean(false)

    fun startSequentialExport(
        records: List<RefuelRecordEntity>,
        onProgress: (Int) -> Unit
    ) {
        isCancelled.set(false)

        threadTxt = Thread {
            onProgress(10)
            if (isCancelled.get()) return@Thread

            fileManager.saveToTxt(records, "RefuelHistoryTxt")
            if (isCancelled.get()) return@Thread

            val loaded = fileManager.loadFromTxt("RefuelHistoryTxt")
            onProgress(50)

            threadXls = Thread {
                if (isCancelled.get()) return@Thread

                fileManager.saveToXls(loaded, "RefuelHistoryXls")
                onProgress(100)
            }
            threadXls?.start()
        }
        threadTxt?.start()
    }

    fun cancelTasks() {
        isCancelled.set(true)
        threadTxt?.interrupt()
        threadXls?.interrupt()
    }
}