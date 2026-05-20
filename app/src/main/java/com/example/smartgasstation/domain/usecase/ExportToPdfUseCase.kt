package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.domain.repository.IRefuelRepository

class ExportToPdfUseCase(
    private val repository: IRefuelRepository
) {
    suspend operator fun invoke() {
        val records = repository.getAllList()
        repository.saveToPdf(records, "RefuelHistoryPdf")
    }
}
