package com.example.smartgasstation.presentation.multithreading

import com.example.smartgasstation.domain.entity.RefuelRecord
import com.example.smartgasstation.domain.repository.IRefuelRepository
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ThreadManager @Inject constructor(
    private val repository: IRefuelRepository
) {
    private var threadTxt: Thread? = null
    private var threadXls: Thread? = null
    private val isCancelled = AtomicBoolean(false)

    fun startSequentialExport(
        records: List<RefuelRecord>,
        onProgress: (Int) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) {
        isCancelled.set(false)

        threadTxt = Thread {
            try {
                onProgress(10)
                if (isCancelled.get()) return@Thread

                repository.saveToTxt(records, "RefuelHistoryTxt")
                if (isCancelled.get()) return@Thread

                val loaded = repository.loadFromTxt("RefuelHistoryTxt")
                onProgress(50)

                threadXls = Thread {
                    try {
                        if (isCancelled.get()) return@Thread
                        repository.saveToXls(loaded, "RefuelHistoryXls")
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
