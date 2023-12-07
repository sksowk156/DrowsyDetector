package com.paradise.domain.usecases

import com.core.model.winkResultItem
import com.paradise.common.result.UiState
import com.paradise.common.network.ioDispatcher
import com.paradise.domain.repository.AnalyzerResultRepository
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetWinkItemListUseCase @Inject constructor(private val analyzerResultRepository: AnalyzerResultRepository) {
    operator fun invoke() = flow<UiState<List<winkResultItem>>> {
        emit(UiState.Loading)
        emit(UiState.Success(analyzerResultRepository.getAllWinkCount()))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()

    operator fun invoke(recordId: Int) = flow<UiState<List<winkResultItem>>> {
        emit(UiState.Loading)
        emit(UiState.Success(analyzerResultRepository.getWinkCount(recordId)))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()
}