package com.paradise.domain.usecases

import com.paradise.common.network.BASICMUSICMODE
import com.paradise.common.network.GUIDEMODE
import com.paradise.common.network.MUSICVOLUME
import com.paradise.common.network.REFRESHTERM
import com.paradise.data.repository.SettingRepository
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

class GetSettingUseCase@Inject constructor(private val settingRepository: SettingRepository) {
    operator fun invoke() =
    with(settingRepository) {
        this.getBoolean(GUIDEMODE)
            .zip(this.getBoolean(BASICMUSICMODE)) { a, b -> mutableListOf(a, b) }
            .zip(this.getInt(MUSICVOLUME)) { list, c -> list to mutableListOf(c) }
            .zip(this.getInt(REFRESHTERM)) { pair, d ->
                pair.second.add(d)
                pair.first to pair.second
            }
    }
}