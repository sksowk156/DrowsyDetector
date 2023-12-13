package com.paradise.domain.usecases

import com.paradise.data.repository.AnalyzerResultRepository
import javax.inject.Inject
import javax.inject.Singleton

class GetDrowsyItemListUseCase @Inject constructor(private val analyzerResultRepository: AnalyzerResultRepository) {
    operator fun invoke(recordId: Int) = analyzerResultRepository.getDrowsyCount(recordId)

}