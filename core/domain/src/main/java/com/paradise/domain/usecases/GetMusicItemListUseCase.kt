package com.paradise.domain.usecases

import com.core.model.musicItem
import com.paradise.common.result.UiState
import com.paradise.common.network.ioDispatcher
import com.paradise.domain.repository.MusicRepository
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetMusicItemListUseCase @Inject constructor(private val musicRepository: MusicRepository) {
    operator fun invoke() = flow<UiState<List<musicItem>>> {
        emit(UiState.Loading)
        emit(UiState.Success(musicRepository.getAllMusic()))
    }.catch {
        emit(UiState.Error(it))
    }.flowOn(ioDispatcher).cancellable()
}