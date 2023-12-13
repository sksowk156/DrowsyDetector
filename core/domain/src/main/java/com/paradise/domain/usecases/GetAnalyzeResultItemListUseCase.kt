package com.paradise.domain.usecases

import com.paradise.data.repository.AnalyzerResultRepository
import javax.inject.Inject
import javax.inject.Singleton

class GetAnalyzeResultItemListUseCase @Inject constructor(private val analyzerResultRepository: AnalyzerResultRepository) {
    operator fun invoke(id: Int) = analyzerResultRepository.getRecord(id)
    operator fun invoke(time: String) = analyzerResultRepository.getRecord(time)
}