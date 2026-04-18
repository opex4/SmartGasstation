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
        onProgress: (Int) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        isCancelled.set(false)

        threadTxt = Thread {
            try {
                onProgress(10)
                if (isCancelled.get()) return@Thread

                fileManager.saveToTxt(records, "RefuelHistoryTxt")
                if (isCancelled.get()) return@Thread

                val loaded = fileManager.loadFromTxt("RefuelHistoryTxt")
                onProgress(50)

                threadXls = Thread {
                    try {
                        if (isCancelled.get()) return@Thread

                        fileManager.saveToXls(loaded, "RefuelHistoryXls")
                        onProgress(100)
                    } catch (e: Exception) {
                        onError(e)
                    }
                }
                threadXls?.start()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            } catch (e: Exception) {
                onError(e)
            }
        }
        threadTxt?.start()
    }

    fun cancelTasks() {
        isCancelled.set(true)
        threadTxt?.interrupt()
        threadXls?.interrupt()
    }
}