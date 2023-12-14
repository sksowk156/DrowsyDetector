package com.paradise.domain.usecases

import com.paradise.data.repository.AnalyzerResultRepository
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

class GetAnalyzeResultItemUseCase @Inject constructor(private val analyzerResultRepository: AnalyzerResultRepository) {
    operator fun invoke(recordId: Int) = analyzerResultRepository.getWinkCount(recordId)
        .zip(analyzerResultRepository.getDrowsyCount(recordId)) { value1, value2 -> value1 to value2 }
}