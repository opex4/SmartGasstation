package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.domain.repository.IRefuelRepository

class ImportFromXlsUseCase(
    private val repository: IRefuelRepository
) {
    suspend operator fun invoke() {
        val records = repository.loadFromXls("RefuelHistoryXls")
        repository.clear()
        records.forEach { repository.insert(it) }
    }
}
