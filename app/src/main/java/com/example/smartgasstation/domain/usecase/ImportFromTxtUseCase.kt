package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.domain.repository.IRefuelRepository

class ImportFromTxtUseCase(
    private val repository: IRefuelRepository
) {
    suspend operator fun invoke() {
        val records = repository.loadFromTxt("RefuelHistoryTxt")
        repository.clear()
        records.forEach { repository.insert(it) }
    }
}
