package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.domain.repository.IRefuelRepository

class ExportToTxtUseCase(
    private val repository: IRefuelRepository
) {
    suspend operator fun invoke() {
        val records = repository.getAllList()
        repository.saveToTxt(records, "RefuelHistoryTxt")
    }
}
