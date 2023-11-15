package com.paradise.drowsydetector.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.utils.getUriFromFilePath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Music helper
 *
 * 사용법 :
 *
 *       MusicHelper.Builder()
 *           .setMusic(context, Music or resId)
 *           .setTime(
 *               lifeCycleOwner,
 *               startTime,
 *               duration
 *           )
 *           .startMusic()
 * @property mediaPlayer
 * @property job
 * @constructor Create empty Music helper
 */
class MusicHelper private constructor(
    private val mediaPlayer: MediaPlayer?,
    private val job: Job?,
) {
    /**
     * Builder : 빌더 패턴
     *
     * context, resId, music, lifecycleowner, startTime, duration 등 음악을 한번 재생하는데 필요한 변수들이 너무 많았다.
     *
     * 그래서 빌더 패턴으로 구현하였다.
     * @property mediaPlayer
     * @property job
     * @constructor Create empty Builder
     */
    data class Builder(
        private var mediaPlayer: MediaPlayer? = null,
        private var job: Job? = null,
    ) {

        /**
         * Set music
         *
         * 앱 리소스에 있는 파일로 음악을 등록한다.
         * @param context
         * @param resId
         * @return
         */
        fun setMusic(context: Context, resId: Int): Builder {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer().apply {
                // 소리 파일의 데이터 소스 설정
                val rawDescriptor = context.resources.openRawResourceFd(resId)
                setDataSource(
                    rawDescriptor.fileDescriptor,
                    rawDescriptor.startOffset,
                    rawDescriptor.length
                )
                rawDescriptor.close()
                prepareAsync()
            }
            return this
        }

        /**
         * Set music
         *
         * Room에 저장된 Music 객체로 음악을 등록한다.
         * @param context
         * @param music
         * @return
         */
        fun setMusic(context: Context, music: Music): Builder {
            releaseMediaPlayer()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(
                    context,
                    getUriFromFilePath(context, music.newPath!!)!!
                )
                prepareAsync()
            }
            return this
        }

        /**
         * Set time
         *
         * 빌더에 mediaPlayer에 리스너를 등록하고, 종료 시간을 관리하는 job을 등록 해준다.
         * @param lifecycleOwner
         * @param startTime
         * @param duration
         * @return
         */
        fun setTime(
            lifecycleOwner: LifecycleOwner,
            startTime: Int = 0,
            duration: Long,
        ): Builder {
            mediaPlayer?.run {
                this.setListener(startTime)
                // mediaPlayer가 null이 아닐 때만 job이 생김
                job = setDuration(lifecycleOwner, duration)
            }
            return this
        }

        /**
         * Start music
         *
         * 최종 호출 메서드, 모든 설정이 완료되어야 실행됨
         * @return
         */
        fun startMusic(): MusicHelper {
            return MusicHelper(mediaPlayer, job)
        }

        /**
         * Release media player
         *
         * 음악을 실행하거나, 실행하고나서 내부 인자를 null처리 해준다.
         */
        private fun releaseMediaPlayer() {
            mediaPlayer?.run {
                setOnCompletionListener(null) // 리스너 해제
                stop() // 실행 중이라면 중단
                release() // 해제
            }
            mediaPlayer = null
            job?.cancel() // 실행 중이라면 중단
            job = null
        }

        /**
         * Set listener
         *
         * 음악이 준비되었을 때 동작 리스너, startTime을 따로 설정하지 않으면 처음부터 실행
         *
         * 음악이 종료되었을 때 동작 리스너, mediaPlayer와 job 객체를 null 처리 해준다.
         * @param startTime, 시작 시간(설정 안하면 0)
         */
        private fun MediaPlayer.setListener(startTime: Int) {
            this.setOnPreparedListener {
                if (startTime > 0) seekTo(startTime)
                start()
            }
            this.setOnCompletionListener {
                releaseMediaPlayer()
            }
        }

        /**
         * Set duration
         *
         * 코루틴으로 종료 시간을 설정한다.
         * @param lifecycleOwner, 생명주기를 주입받는다.
         * @param duration, 음악 시간을 설정한다.
         * @return
         */
        private fun setDuration(lifecycleOwner: LifecycleOwner, duration: Long) =
            lifecycleOwner.lifecycleScope.launch(defaultDispatcher) {
                delay(duration)
                mediaPlayer?.run {
                    if (isPlaying) {
                        pause()
                    }
                }
            }


    }

//    private fun playMusic(selectedMusic: Music) {
//        mediaPlayer = MediaPlayer().apply {
//            setDataSource(
//                requireContext(),
//                getUriFromFilePath(requireContext(), selectedMusic.newPath!!)!!
//            )
//
//            setOnPreparedListener {
//                seekTo(selectedMusic.startTime.toInt())
//                start()
//            }
//
//            setOnCompletionListener {
//                stop()
//                release()
//            }
//
//            prepareAsync()
//        }
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            delay(selectedMusic.durationTime)
//            mediaPlayer?.let {
//                if (it.isPlaying) {
//                    it.pause()
//                }
//            }
//        }
//    }
}