package com.paradise.drowsydetector.utils.helper

import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.utils.DEFAULT_MUSIC_DURATION
import com.paradise.drowsydetector.utils.defaultDispatcher
import com.paradise.drowsydetector.utils.getRandomElement
import com.paradise.drowsydetector.utils.getUriFromFilePath
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Music helper
 *
 * SettingFragment와 AnalyzeFragment에서 잠깐 사용되기에 조금 더 사용하기 쉽게 클래스로 만들었다.
 *
 * MediaPlayer에 Context가 필요해서, 약한 참조 클래스인 WeakReference<Context>로 Context가 가비지 컬렉션 대상이 될 수 있게끔 만들어 혹시 모를 메모리 누수를 방지했다.
 *
 * 또한 음악이 한 번에 여러개 재생되는 것을 막고 Context를 보유하는 클래스 개수를 최소화시키고자 싱글톤으로 구현했다.
 *
 * @property contextRef
 * @constructor Create empty Music helper
 */
class MusicHelper(
    private var contextRef: WeakReference<Context>,
    private var lifecycleOwner: WeakReference<LifecycleOwner>,
) {
    private var mediaPlayer: MediaPlayer? = null
    private var job: Job? = null
    private var isPrepared: Boolean = false

    /**
     * Companion
     * 싱글톤
     * @constructor Create empty Companion
     */
    companion object {
        @Volatile
        private var instance: MusicHelper? = null
        fun getInstance(context: Context, lifecycleOwner: LifecycleOwner) =
            instance ?: synchronized(this) {
                // LocationSercie 객체를 생성할 때 같이 한번만 객체를 생성한다.
                instance ?: MusicHelper(
                    WeakReference(context),
                    WeakReference(lifecycleOwner)
                ).also {
                    instance = it
                }
            }
    }

    /**
     * Clear context
     *
     * 제일 마지막에 호출하는 함수이다.
     *
     * Context를 주입해주는 Activity나 Fragment가 Destroy될 때 주입받은 context 정보를 clear()하고 null 처리했다.
     */
    fun clearContext() {
        releaseMediaPlayer()
        contextRef.clear()
        lifecycleOwner.clear()
        instance = null
    }

    /**
     * Release media player
     *
     * 내부 변수 mediaPlayer를 초기화 해주거나, 중간에 음악을 멈추거나 할 때 사용하는 함수이다.
     */
    fun releaseMediaPlayer() {
        this.mediaPlayer?.run {
            if (!isPrepared) { // mediaPlayer가 null은 아닌데 prepared가 되어있지 않다는 것은 아직 prepareAsync()호출 후 비동기적으로 prepare이 동작 중이라는 것이다.
                release() // 비동기로 prepareAsync()가 동작 중일 땐 isPlaying이나 stop을 호출하면 에러가 발생한다. 그래서 release()만 호출
            } else {
                if (this.isPlaying) { // prepare가 된 상태라는 건 음악이 실행되고 있다는 것이다.
                    stop()
                    release()
                }
                isPrepared = false // 초기화
            }
            this.setOnCompletionListener(null) // 리스너 해제
            this.setOnPreparedListener(null)
        }
        job?.cancel() // 음악이 duration 전에 끝날 경우 job을 cancle한다.
        job = null
    }

    /**
     * Start res music
     *
     * Res에 저장된 음악 리스트에서 음악을 랜덤으로 뽑아 MusicHelper.Builder()에 저장한다.
     */
    fun setStandardMusic() {
        startMusic(R.raw.setstandard)
    }

    /**
     * Start res music
     *
     * Res에 저장된 음악 리스트에서 음악을 랜덤으로 뽑아 MusicHelper.Builder()에 저장한다.
     */
    fun setResMusic() {
        val randomMusic = listOf<Int>(
            (R.raw.alert1), (R.raw.alert2), (R.raw.alert3), (R.raw.alert4), (R.raw.alert5)
        ).getRandomElement()
        if (randomMusic != null) startMusic(randomMusic)
    }

    fun setMyMusic(musicList: List<Music>) {
        val randomMusic = musicList.getRandomElement()
        if (randomMusic != null) startMusic(randomMusic)
    }

    /**
     * Start music
     *
     * 음악을 실행하는 메서드
     * @param resId, raw package에 있는 음악 파일을 재생
     */
    fun startMusic(resId: Int) = contextRef.get()?.let { context ->
        val rawDescriptor = context.resources.openRawResourceFd(resId)
        mediaPlayer = MediaPlayer().apply {
            setDataSource(
                rawDescriptor.fileDescriptor,
                rawDescriptor.startOffset,
                rawDescriptor.length
            )
            setMusic(duration = DEFAULT_MUSIC_DURATION)
        }
        rawDescriptor.close()
    }

    /**
     * Start music
     *
     * @param music, Room에 저장된 외부 저장소의 음악 파일 경로로 음악 재생
     */
    fun startMusic(music: Music) =
        contextRef.get()?.let { context ->
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, getUriFromFilePath(context, music.newPath!!)!!)
                setMusic(music.startTime.toInt(), music.durationTime)
            }
            this@MusicHelper
        }

    /**
     * Set music
     *
     * 주어진 음악 파일 정보로부터 시작 시간과 지속 시간을 설정한다.
     * @param startTime, 시작 시간(raw 파일 음악 : 기본 0 사용, Room 음악 : 커스텀 가능)
     * @param duration, (나중에 커스텀화)
     */
    private fun MediaPlayer.setMusic(
        startTime: Int = 0,
        duration: Long,
    ) = this.apply {
        // mediaPlayer가 null이 아닐 때만 job이 생김
        setListener(startTime)
        job = setDuration(duration) // 음악이 재생하는 도중에 정지할 경우 직접 cancle 해줘야하므로 객체를 받아둔다.
        if(job==null) releaseMediaPlayer()
    }

    /**
     * Set listener
     *
     * mediaPlayer에 리스너를 등록한다.
     * @param startTime
     */
    private fun MediaPlayer.setListener(startTime: Int) = this.apply {
        setOnPreparedListener {// 준비 완료 여부 리스너
            isPrepared = true // 준비가 완료됨!!
            if (startTime > 0) seekTo(startTime) // 시작 시간이 있다면 그 시간으로 시작 시간을 초기화 한다.
            start()
        }

        setOnCompletionListener {// 음악 완료 여부 리스너
            releaseMediaPlayer()
        }

        prepareAsync() // 리스너 등록이 끝나면 prepareAsync()를 호출해 비동기로 음악을 준비한다.
    }

    /**
     * Set duration
     *
     * 음악의 종료 시점을 정한다.
     * @param duration
     */
    private fun setDuration(duration: Long) =
        lifecycleOwner.get()?.let {
            it.lifecycleScope.launch(defaultDispatcher) {
                delay(duration)
                releaseMediaPlayer()
            }
        }
}
