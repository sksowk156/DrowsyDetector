package com.paradise.setting

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.model.musicItem
import com.paradise.common.helper.MusicHelper
import com.paradise.common.helper.ToastHelper
import com.paradise.common.helper.VolumeHelper
import com.paradise.common.network.BASICMUSICMODE
import com.paradise.common.network.GUIDEMODE
import com.paradise.common.network.MUSICVOLUME
import com.paradise.common.network.getPathFromFileUri
import com.paradise.common.utils.launchWithRepeatOnLifecycle
import com.paradise.common_ui.R.*
import com.paradise.common_ui.base.BaseFragment
import com.paradise.setting.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.WithFragmentBindings
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

@WithFragmentBindings
@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val settingsViewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var musicHelper: MusicHelper

    @Inject
    lateinit var volumeHelper: VolumeHelper

    @Inject
    lateinit var toastHelper: ToastHelper

    private var volume = 0
    private lateinit var volumeChangeObserver: VolumeChangeObserver
    private lateinit var musicAdapter: MusicAdapter

    // 사이드 키 볼륨 조절 옵저버
    inner class VolumeChangeObserver(context: Context?) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.sliderSettingSound.setValues(volumeHelper.getCurrentVolume()?.toFloat())
        }
    }

    override fun onViewCreated() {
        with(binding) {
            toolbarSettings.setToolbarMenu("설정", true)
            volumeHelper.initVolumeHelper()
            musicHelper.initMusicHelper()
            volumeHelper.initAudio()
            // 알림음 설정 상태
            subscribeMusicStyle()
            // 알림음 크기 설정 상태
            subscribeMusicVolume()
            // 안내 설정 상태
            subscribeGuideState()
            // 음악 리스트
            subscribeMusicList()

            val maxVolume = volumeHelper.getMaxVolume()
            val currentVolume = volumeHelper.getCurrentVolume()
            settingsViewModel.getAllMusic()
            settingsViewModel.getSettingModeBool(GUIDEMODE)
            settingsViewModel.getSettingModeInt(MUSICVOLUME)
            settingsViewModel.getSettingModeBool(BASICMUSICMODE)

            // RangeSlider의 최소값과 최대값을 0과 최대 음량으로 설정
            sliderSettingSound.valueFrom = 0f

            sliderSettingSound.valueTo = maxVolume!!.toFloat()

            // RangeSlider의 현재 값들을 현재 음량으로 설정
            sliderSettingSound.setValues(currentVolume!!.toFloat())

            // RangeSlider의 값이 변경될 때마다 AudioManager의 음량을 변경
            sliderSettingSound.addOnChangeListener { slider, value, fromUser ->
                // value는 Float 타입이므로 Int 타입으로 변환
                volume = value.toInt()
                // AudioManager의 음량을 변경
                volumeHelper.setVolume(volume)
                settingsViewModel.setSettingMode(MUSICVOLUME, volume)
            }

            ivSettingAddmusic.setOnAvoidDuplicateClick {
                // ACTION_GET_CONTENT - 문서나 사진 등의 파일을 선택하고 앱에 그 참조를 반환하기 위해 요청하는 액션
                val intent = Intent(android.content.Intent.ACTION_GET_CONTENT)
                intent.type = "audio/*" // 탐색할 파일 MIME 타입 설정
                launcher_audio.launch(intent) // 파일탐색 액션을 가진 인텐트 실행
            }
            initButton()

            // 안내 설정을 변경할 때마다 값을 갱신
            switchSettingGuide.setOnCheckedChangeListener { buttonView, isChecked ->
                settingsViewModel.setSettingMode(GUIDEMODE, isChecked)
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        volumeChangeObserver = VolumeChangeObserver(requireContext())
        requireContext().registerReceiver(
            volumeChangeObserver, IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        )
    }

    override fun onPause() {
        super.onPause()
        musicHelper.releaseMediaPlayer()
        // BroadcastReceiver를 해제
        requireContext().unregisterReceiver(volumeChangeObserver)
    }

    override fun onDestroyViewInFragMent() {
        volume = 0
    }

    private fun subscribeMusicStyle() =
        viewLifecycleOwner.launchWithRepeatOnLifecycle(Lifecycle.State.STARTED) {
            settingsViewModel.basicMusicMode.collect { mode ->
                with(binding) {
                    if (mode) {
                        radiogroupSetting.check(radiobtSettingBasicmusic.id)
                        setRadioButtonBackground(radiobtSettingBasicmusic, true)
                        setRadioButtonBackground(radiobtSettingUsermusic, false)
                        layoutSettingMusiclistbackground.visibility = android.view.View.GONE
                    } else {
                        radiogroupSetting.check(radiobtSettingUsermusic.id)
                        setRadioButtonBackground(radiobtSettingBasicmusic, false)
                        setRadioButtonBackground(radiobtSettingUsermusic, true)
                        layoutSettingMusiclistbackground.visibility = android.view.View.VISIBLE
                    }
                }
            }
        }

    private fun subscribeMusicVolume() =
        viewLifecycleOwner.launchWithRepeatOnLifecycle(Lifecycle.State.STARTED) {
            settingsViewModel.musicVolume.collect { myvolume ->
                with(binding) {
                    volume = myvolume
                    sliderSettingSound.setValues(myvolume.toFloat())
                    volumeHelper.setVolume(myvolume)
                }
            }
        }

    private fun subscribeGuideState() =
        viewLifecycleOwner.launchWithRepeatOnLifecycle(Lifecycle.State.STARTED) {
            settingsViewModel.guideMode.collect { mode ->
                with(binding) {
                    switchSettingGuide.isChecked = mode
                }
            }
        }

    private fun subscribeMusicList() =
        viewLifecycleOwner.launchWithRepeatOnLifecycle(Lifecycle.State.STARTED) {
            settingsViewModel.music.collect { musicList ->
                if (musicList != null) {
                    initRecycler(musicList.toMutableList())
                }
            }
        }

    private fun initButton() {
        with(binding) {
            radiobtSettingBasicmusic.setOnAvoidDuplicateClick {
                settingsViewModel.setSettingMode(BASICMUSICMODE, true)
                musicHelper.releaseMediaPlayer()

            }
            radiobtSettingUsermusic.setOnAvoidDuplicateClick {
                settingsViewModel.setSettingMode(BASICMUSICMODE, false)
                musicHelper.releaseMediaPlayer()
            }
        }
    }


    private fun setRadioButtonBackground(radioButton: RadioButton, isSelected: Boolean) {
        with(radioButton) {
            if (isSelected) {
                setBackgroundResource(R.drawable.button_background2)
                setTextColor(ContextCompat.getColor(requireContext(), color.white))
            } else {
                setBackgroundResource(R.drawable.button_background)
                setTextColor(ContextCompat.getColor(requireContext(), color.primary1))
            }
        }
    }

    private fun initRecycler(result: MutableList<musicItem>) {
        with(binding) {
            musicAdapter = MusicAdapter(result, { selectedMusic ->
                if (musicHelper.isPrepared.value) {
                    musicHelper.releaseMediaPlayer()
                } else {
                    musicHelper.releaseMediaPlayer()
                    musicHelper.startMusic(selectedMusic)
                }
            }, { selectedMusic ->
                musicHelper.releaseMediaPlayer()
                showDialog(selectedMusic)
            }, { selectedMusic ->
                musicHelper.releaseMediaPlayer()
                deleteMusic(selectedMusic)
            })

            rvSettingUsermusic.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    androidx.recyclerview.widget.LinearLayoutManager.VERTICAL,
                    false
                )
                adapter = musicAdapter
            }
        }
    }

    private fun showDialog(selectedMusic: musicItem) {
        val dialogFragment = MusicSettingsDialogFragment(selectedMusic) {
            settingsViewModel.updateMusic(it)
        }
        dialogFragment.show(childFragmentManager, "YourDialogTag")
    }

    private fun deleteMusic(selectedMusic: musicItem) {
        val deleteFile = File(selectedMusic.newPath!!)
        if (deleteFile.exists()) {
            val result = deleteFile.delete()
            if (result) toastHelper.showToast("파일 삭제")
        }
        settingsViewModel.deleteMusic(selectedMusic.id)
    }


    // 오디오 파일 탐색 후 선택했을 때 콜백메서드를 설정한 intent launcher
    var launcher_audio: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 사용자가 파일 탐색 화면에서 돌아왔을 때 호출되는 메소드
        if (result.resultCode == AppCompatActivity.RESULT_OK) { // 사용자가 파일 선택을 성공적으로 완료했을 때 내부 코드 실행
            val data: Intent = result.data!! // 콜백 메서드를 통해 전달 받은 ActivityResult 객체에서 Intent 객체 추출
            val audioUri = data.data // Intent 객체에서 선택한 오디오 파일의 위치를 가리키는 Uri 추출
            // 앱과 안드로이드 시스템 간의 데이터 통신을 하기위해 ContentResolver 객체 생성
            // ContentResolver를 통해 앱은 ContentProvider를 사용해 다른 앱의 데이터에 접근하거나 데이터를 읽거나 쓸 수 있다
            val contentResolver = requireContext().contentResolver
            // Uri를 사용해 파일 복사본 생성후 해당 파일 경로 반환
            val newFilePath: String? = getNewFilePathFromUri(
                requireContext(), contentResolver, audioUri
            )
            // 원본 파일의 Uri로부터 절대 경로 반환
            val audioPath = getPathFromFileUri(requireContext(), audioUri)
            // 원본 파일로부터 음원 제목 추출
            val title = getFileName(contentResolver, audioUri!!)
            // 복사본이 성공적으로 생성되면 해당 위치가 기록된 파일을 Room에 저장
            if (newFilePath != null) { // 파일이 정상적으로 생성되었을 때 내부 코드 실행
                val newMusic = musicItem(
                    title = title, newPath = newFilePath, originalPath = audioPath
                )
                settingsViewModel.insertMusic(newMusic)
            } else { // 파일이 정상적으로 생성되지 않았을 때 내부 코드 실행
                toastHelper.showToast("오디오 파일을 가져오는데 문제가 생겼습니다.") // 메시지 출력
            }
        } else if (result.resultCode == AppCompatActivity.RESULT_CANCELED) { // 사용자가 파일 탐색 중 선택을 하지 않았을 때 내부 코드 실행
            Log.d("launcher_audio Callback", "audio picking is canceled") // 로그 출력
        } else { // 그 외의 경우 예외 처리
            Log.e("launcher_audio Callback", "audio picking has failed") // 로그 출력
        }
    }


    // URI를 사용해 파일을 복사하고 복사한 경로를 제공하는 메소드
    fun getNewFilePathFromUri(
        context: Context,
        contentResolver: ContentResolver,
        uri: Uri?,
    ): String? {
        // Uri를 사용해 파일 이름 반환
        val fileTempData: String = getFileName(contentResolver, uri!!)
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileTempData
        )
        return try {
            inputStream =
                contentResolver.openInputStream(uri) // uri에 있는 데이터를 읽기 위해 InputStream을 열고 InputStream을 반환한다
            if (inputStream == null) {
                return null // InputStream을 여는데 실패할 경우 null 반환
            }
            outputStream = FileOutputStream(file) // 이전에 만든 File 객체에 데이터를 쓰기 위해 OutputStream 생성
            val buffer = ByteArray(1024) // 1 KB buffer 생성
            var bytesRead: Int
            // 구간 복사를 시도했지만 안됐다.... 왜 안되는지 모르겠다....
            // 작은 파일들은 되지만 큰 파일의 경우 파일 생성까지는 되지만 mediaPlayer에서 동작하지 않는다. "Error (1,-1004)"가 발생하면서 동작하지 않았다.
            // 어쩔 수 없이 그냥 전체 파일을 복사해 저장하고, 시작 구간을 저장해서 재생할 때 시간을 조절하는 방식으로 구현했다.
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead) // 읽은 byte 데이터를 사용해 File 객체에 데이터 쓰기
            }
            file.absolutePath  // 그 후 복사가 완료된 파일 객체 반환
        } catch (e: IOException) { // 입출력 문제 생기면 오류 출력
            e.printStackTrace()
            Log.e("whatisthis", e.toString())
            null
        } finally {
            // 마지막으로 사용한 inputStream, outputStream 종료
            try {
                inputStream?.close()
                outputStream?.close()
            } catch (e: IOException) { // 입출력 문제 생기면 오류 출력
                e.printStackTrace()
            }
        }
    }


    // uri를 사용해 파일 이름을 반환하는 메서드
    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        // ContentProvider 를 통해 기기 데이터베이스에서 데이터를 조회하기 위해 query() 메서드 사용
        val cursor = contentResolver.query(uri, null, null, null, null)
        var displayName: String = ""
        if (cursor != null && cursor.moveToFirst()) { // 조회된 데이터를 저장한 cursor가 null이 아니고 첫번째 레코드로 이동할 수 있으면(첫번째 레코드가 없다면 false 반환) 내부 코드 실행
            val nameIndex =
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) // 파일이름이 저장된 열의 위치 반환 후 저장
            if (nameIndex != -1) { // 해당 열이 존재할 경우 내부 코드 실행
                displayName = cursor.getString(nameIndex) // 열에 있는 데이터 반환
            }
            cursor.close() // 커서 종료
        }
        return displayName // 받은 파일이름 반환
    }

    private fun refresh() {
//            val regionArray = resources.getStringArray(R.array.refresh_period)
//            val arrayAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, regionArray)
//            autoCompleteTextViewSetting.setAdapter(arrayAdapter)
//            autoCompleteTextViewSetting.itemClickEvents()
//                .onEach {
//                    showToast(regionArray[it.position])
//                }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}