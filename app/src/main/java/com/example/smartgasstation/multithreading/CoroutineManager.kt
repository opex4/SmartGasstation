package com.example.smartgasstation.multithreading

import android.util.Log
import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.filemanager.RefuelRecordsFileManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoroutineManager(
    private val fileManager: RefuelRecordsFileManager
) {
    private val exceptionHandler = CoroutineExceptionHandler { context, exception ->
        Log.e("CoroutineManager", "Ошибка в корутине: ${exception.message}", exception)
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
    private var job: Job? = null

    fun startSequentialExport(
        records: List<RefuelRecordEntity>,
        onProgress: (Int) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        job = ioScope.launch {
            try {
                onProgress(10)
                fileManager.saveToTxt(records, "RefuelHistoryTxt")
                val result = fileManager.loadFromTxt("RefuelHistoryTxt")
                onProgress(50)

                withContext(Dispatchers.Default) {
                    fileManager.saveToXls(result, "RefuelHistoryXls")
                    onProgress(100)
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun cancelTasks() {
        job?.cancel()
    }
}