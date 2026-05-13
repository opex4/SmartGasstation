package com.example.smartgasstation.domain.usecase

import androidx.lifecycle.LiveData
import com.example.smartgasstation.data.RefuelRecordEntity
import com.example.smartgasstation.data.RefuelRepository
import javax.inject.Inject

class GetRefuelRecordsUseCase @Inject constructor(
    private val repository: RefuelRepository
) {
    operator fun invoke(): LiveData<List<RefuelRecordEntity>> = repository.allRecords
}
