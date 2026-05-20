package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.domain.entity.RefuelRecord
import com.example.smartgasstation.domain.repository.IRefuelRepository

class GetRefuelRecordsUseCase(
    private val repository: IRefuelRepository
) {
    suspend operator fun invoke(): List<RefuelRecord> = repository.getAllList()
}
