package com.example.smartgasstation.domain.usecase

import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.data.RefuelRepository
import javax.inject.Inject

class DeleteRefuelUseCase @Inject constructor(
    private val repository: RefuelRepository
) {
    suspend operator fun invoke(record: RefuelRecordEntity) {
        repository.delete(record)
    }
}
