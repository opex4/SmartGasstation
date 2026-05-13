package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.data.RefuelRepository
import com.example.smartgasstation.filemanager.RefuelRecordsFileManager
import javax.inject.Inject

class ImportFromTxtUseCase @Inject constructor(
    private val repository: RefuelRepository,
    private val fileManager: RefuelRecordsFileManager
) {
    suspend operator fun invoke() {
        val records = fileManager.loadFromTxt("RefuelHistoryTxt")
        repository.clear()
        records.forEach { repository.insert(it) }
    }
}
