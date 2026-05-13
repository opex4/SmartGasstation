package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.data.RefuelRepository
import javax.inject.Inject

class ClearHistoryUseCase @Inject constructor(
    private val repository: RefuelRepository
) {
    suspend operator fun invoke() = repository.clear()
}
