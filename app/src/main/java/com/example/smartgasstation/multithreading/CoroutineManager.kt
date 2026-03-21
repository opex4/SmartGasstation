package com.example.smartgasstation.multithreading

import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.filemanager.RefuelRecordsFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoroutineManager(
    private val fileManager: RefuelRecordsFileManager
) {
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun startSequentialExport(
        records: List<RefuelRecordEntity>,
        onProgress: (Int) -> Unit
    ) {
        job = ioScope.launch {
            onProgress(10)
            fileManager.saveToTxt(records, "RefuelHistoryTxt")
            val result = fileManager.loadFromTxt("RefuelHistoryTxt")
            onProgress(50)

            withContext(Dispatchers.Default) {
                fileManager.saveToXls(result, "RefuelHistoryXls")
                onProgress(100)
            }
        }
    }

    fun cancelTasks() {
        job?.cancel()
    }
}