package com.paradise.domain.usecases

import com.core.model.analyzeResultItem
import com.paradise.common.UiState
import com.paradise.common.network.ioDispatcher
import com.paradise.domain.repository.AnalyzerResultRepository
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetAnalyzeResultItemListUseCase @Inject constructor(private val analyzerResultRepository: AnalyzerResultRepository) {
    operator fun invoke() = flow<UiState<List<analyzeResultItem>>> {
        emit(UiState.Loading)
        emit(UiState.Success(analyzerResultRepository.getAllRecord()))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()

    operator fun invoke(id: Int) = flow<UiState<analyzeResultItem>> {
        emit(UiState.Loading)
        emit(UiState.Success(analyzerResultRepository.getRecord(id)))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()

    operator fun invoke(time: String) = flow<UiState<analyzeResultItem>> {
        emit(UiState.Loading)
        emit(UiState.Success(analyzerResultRepository.getRecord(time)))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()
}