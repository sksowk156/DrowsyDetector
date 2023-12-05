package com.paradise.domain.usecases

import com.core.model.drowsyResultItem
import com.paradise.common.UiState
import com.paradise.common.network.ioDispatcher
import com.paradise.domain.repository.AnalyzerResultRepository
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetDrowsyItemListUseCase @Inject constructor(private val analyzerResultRepository: AnalyzerResultRepository) {
    operator fun invoke() = flow<UiState<List<drowsyResultItem>>> {
        emit(UiState.Loading)
        emit(UiState.Success(analyzerResultRepository.getAllDrowsyCount()))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()

    operator fun invoke(recordId: Int) = flow<UiState<List<drowsyResultItem>>> {
        emit(UiState.Loading)
        emit(UiState.Success(analyzerResultRepository.getDrowsyCount(recordId)))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()

}