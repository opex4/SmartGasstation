package com.example.smartgasstation.presentation.multithreading

import android.util.Log
import com.example.smartgasstation.domain.entity.RefuelRecord
import com.example.smartgasstation.domain.repository.IRefuelRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CoroutineManager @Inject constructor(
    private val repository: IRefuelRepository
) {
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e("CoroutineManager", "Ошибка в корутине: ${exception.message}", exception)
    }

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
    private var job: Job? = null

    fun startSequentialExport(
        records: List<RefuelRecord>,
        onProgress: (Int) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        job = ioScope.launch {
            try {
                onProgress(10)
                repository.saveToTxt(records, "RefuelHistoryTxt")
                val result = repository.loadFromTxt("RefuelHistoryTxt")
                onProgress(50)

                withContext(Dispatchers.Default) {
                    repository.saveToXls(result, "RefuelHistoryXls")
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
