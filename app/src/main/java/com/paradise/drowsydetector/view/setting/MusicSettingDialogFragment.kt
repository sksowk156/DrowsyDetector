package com.paradise.drowsydetector.view.setting

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.databinding.FragmentMusicSettingDialogBinding
import com.paradise.drowsydetector.utils.getUriFromFilePath
import com.paradise.drowsydetector.utils.setOnAvoidDuplicateClickFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MusicSettingDialogFragment(
    private val music: Music,
    private val onSaveClick: (music: Music) -> Unit,
) : DialogFragment() {

    private lateinit var binding: FragmentMusicSettingDialogBinding

    private var player: ExoPlayer? = null
    private var updateJob: Job? = null
    private var stopJob: Job? = null

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            this.binding =
                FragmentMusicSettingDialogBinding.inflate(LayoutInflater.from(it))
            val dialogLayout = this.binding.root

            binding.tvSettingdialogTitle.text = music.title
            initPlayer(requireContext())
            initPlayControlButtons()
            initSeekBar()

            val builder = AlertDialog.Builder(it)
            with(builder) {
                setTitle(resources.getString(R.string.settingdialog_title))
                setMessage(resources.getString(R.string.settingdialog_message))
                setView(dialogLayout)
                    .setPositiveButton(
                        resources.getString(R.string.save)
                    ) { dialog, id ->
                        onSaveClick(music)
                        dismiss()
                    }
                    .setNegativeButton(
                        resources.getString(R.string.close)
                    ) { dialog, id ->
                        dismiss()
                    }
                create()
            }
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        player?.release()
        player = null
        updateJob = null
        stopJob = null
    }

    private fun initPlayer(context: Context) {
        player = ExoPlayer.Builder(context)
            .setRenderersFactory(DefaultRenderersFactory(context))
            .build()

        player?.addListener(object : Player.Listener { // 리스너 등록
            override fun onIsPlayingChanged(isPlaying: Boolean) { // 플레이어의 재생 여부가 변경될 때 호출됩니다.
                super.onIsPlayingChanged(isPlaying)
                if (isPlaying) {
                    binding.ivSettingdialogController.setImageResource(R.drawable.icon_pause)
                    stopJob?.cancel()
                    stopJob = initStopJob()
                } else {
                    binding.ivSettingdialogController.setImageResource(R.drawable.icon_play)
                    stopJob?.cancel()
                }
            }

            override fun onPlaybackStateChanged(state: Int) { // 플레이어의 재생 상태가 변경될 때 호출됩니다.
                super.onPlaybackStateChanged(state)
//                Player.STATE_IDLE: 플레이어가 아무것도 하지 않는 상태
//                Player.STATE_BUFFERING: 버퍼링 중인 상태
//                Player.STATE_READY: 플레이어가 준비된 상태 (재생 가능한 상태)
//                Player.STATE_ENDED: 플레이어가 종료된 상태 (재생이 끝난 상태)
                updateSeek()
//                initDurationView()
            }
        })

        // DataSourceFactory 설정
        val dataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, "YourApplicationName")
        )

        // 음악 파일 설정
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(getUriFromFilePath(context, music.newPath!!)!!))

        player?.addMediaSource(mediaSource)
        player?.prepare()
        player?.seekTo(music.startTime)
    }

    private fun initStopJob() = lifecycleScope.launch() {
        var remainTime = music.durationTime
        if (player?.currentPosition!! > music.startTime) { // 재생 중간에 멈출 경우 delay 시간을 고쳐야 한다.
            val diffTime = player?.currentPosition!! - music.startTime // 들은 시간
            remainTime = music.durationTime - diffTime // 전체 동작 시간에서 들은 시간만큼 빼준다.
        }
        delay(remainTime) // 이 시간이 지나면 자동으로 멈춘다.
        player!!.pause()
        player?.seekTo(music.startTime) // 다시 처음으로 돌아간다.
    }

    private fun initPlayControlButtons() {
        // 재생 or 일시정지 버튼
        binding.ivSettingdialogController.setOnAvoidDuplicateClickFlow {
            val player = this.player ?: return@setOnAvoidDuplicateClickFlow
            if (player.isPlaying) {
                player.pause()
                updateJob?.cancel()
            } else {
                player.play()
                updateJob?.cancel()
                updateJob = initUpdateJob()
            }
        }
    }

    private fun initUpdateJob() =
        lifecycleScope.launch {
            while (true) {
                updateSeek()
                delay(1000L)
            }
        }

    private fun initSeekBar() {
        binding.seekbarSettingdialogMusic.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean,
            ) { // 사용자가 SeekBar를 조작하여 현재 프로그레스가 변경되었을 때 호출되는 메서드
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) { // 사용자가 SeekBar를 터치하여 조작을 시작할 때 호출되는 메서드
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) { // 사용자가 SeekBar 조작을 멈출 때 호출되는 메서드
                if (player!!.isPlaying) player!!.pause()
                music.startTime = seekBar.progress * 1000L
                player?.seekTo(music.startTime)
//                initDurationView()
            }
        })
        val duration = if (player!!.duration >= 0) player!!.duration else 0 // 전체 음악 길이
        val position = music.startTime

        updateSeekUi(duration!!, position)
    }

    private fun updateSeek() {
        val player = this.player ?: return

        val duration = if (player.duration >= 0) player.duration else 0 // 전체 음악 길이
        val position = player.currentPosition

        updateSeekUi(duration, position)

//        val state = player.playbackState
//        Player.STATE_IDLE: 플레이어가 아무것도 하지 않는 상태. 초기 상태이거나 release 메서드가 호출된 상태입니다.
//        Player.STATE_BUFFERING: 미디어가 버퍼링 중인 상태. 플레이어가 재생을 시작하거나 버퍼링이 필요한 경우 이 상태로 전환됩니다.
//        Player.STATE_READY: 미디어가 준비되어 있고, 플레이어가 재생 가능한 상태입니다. 버퍼링이 완료되면 이 상태로 전환됩니다.
//        Player.STATE_ENDED: 미디어의 재생이 완료된 상태입니다. 플레이어가 미디어의 끝에 도달하면 이 상태로 전환됩니다.
//        if (state != Player.STATE_IDLE && state != Player.STATE_ENDED) { // STATE_BUFFERING 이거나 STATE_READY 일 때
//            postDelayed(updateSeekRunnable, 1000) // 1초에 한번씩 실행
//        }
    }

    private fun updateSeekUi(duration: Long, position: Long) {
        binding.seekbarSettingdialogMusic.max = (duration / 1000).toInt()
        binding.seekbarSettingdialogMusic.progress = (position / 1000).toInt()

        binding.tvSettingdialogCurrent.text = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(position, TimeUnit.MILLISECONDS), // 현재 분
            (position / 1000) % 60 // 분 단위를 제외한 현재 초
        )
        binding.tvSettingdialogEnd.text = String.format(
            "%02d:%02d",
            TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS), // 전체 분
            (duration / 1000) % 60 // 분 단위를 제외한 초
        )
    }

//    fun initDurationView() {
//        // Drawable 설정
//        val layerDrawable =
//            requireContext().getDrawable(R.drawable.seekbar_background) as LayerDrawable
//
////        // 특정 구간의 시작과 끝을 나타내는 level 값을 계산하여 설정
//        val startPercent: Double = music.startTime / (player?.duration)!!.toDouble()
//        val musicPercent: Double = music.durationTime / (player?.duration)!!.toDouble()
//        val startLevel = (startPercent * 10000).toInt()  // 10%에서의 level 값
//        val endLevel = ((startPercent + musicPercent) * 10000).toInt()   // 30%에서의 level 값
//
//        layerDrawable.findDrawableByLayerId(R.id.specificRangeBackgroundEnd).level = endLevel
//        layerDrawable.findDrawableByLayerId(R.id.specificRangeBackgroundStart).level = startLevel
//    }


}