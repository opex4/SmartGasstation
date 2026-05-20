package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.domain.repository.IRefuelRepository

class ClearHistoryUseCase(
    private val repository: IRefuelRepository
) {
    suspend operator fun invoke() = repository.clear()
}
