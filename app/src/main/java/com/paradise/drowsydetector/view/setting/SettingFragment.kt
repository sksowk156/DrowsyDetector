package com.paradise.drowsydetector.view.setting

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.paradise.drowsydetector.R
import com.paradise.drowsydetector.base.BaseViewbindingFragment
import com.paradise.drowsydetector.data.local.room.music.Music
import com.paradise.drowsydetector.databinding.FragmentSettingBinding
import com.paradise.drowsydetector.utils.GUIDEMODE
import com.paradise.drowsydetector.utils.MusicHelper
import com.paradise.drowsydetector.utils.getPathFromFileUri
import com.paradise.drowsydetector.utils.getUriFromFilePath
import com.paradise.drowsydetector.utils.mainScope
import com.paradise.drowsydetector.utils.showToast
import com.paradise.drowsydetector.viewmodel.MusicViewModel
import com.paradise.drowsydetector.viewmodel.SettingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import reactivecircus.flowbinding.android.widget.itemClickEvents
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class SettingFragment :
    BaseViewbindingFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    private lateinit var musicAdapter: MusicAdapter

    //    private var mediaPlayer: MediaPlayer? = null
    private var musicHelper: MusicHelper? = null
    private val musicViewModel: MusicViewModel by activityViewModels()
    private val settingViewModel: SettingViewModel by activityViewModels()

    override fun onViewCreated() {
        with(binding) {
            toolbarSetting.setToolbarMenu("설정", true)
            musicHelper = MusicHelper()
            val regionArray = resources.getStringArray(R.array.refresh_period)
            val arrayAdapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, regionArray)
            autoCompleteTextViewSetting.setAdapter(arrayAdapter)
            autoCompleteTextViewSetting.itemClickEvents()
                .onEach {
                    showToast(regionArray[it.position])
                }.launchIn(mainScope)

            btSettingUsermusic.setOnAvoidDuplicateClick {
                musicHelper?.checkMediaPlayer()
                binding.layoutSettingMusiclistbackground.visibility = View.VISIBLE
            }

            ivSettingAddmusic.setOnAvoidDuplicateClick {
                musicHelper?.checkMediaPlayer()
                // ACTION_GET_CONTENT - 문서나 사진 등의 파일을 선택하고 앱에 그 참조를 반환하기 위해 요청하는 액션
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "audio/*" // 탐색할 파일 MIME 타입 설정
                launcher_audio.launch(intent) // 파일탐색 액션을 가진 인텐트 실행
            }

            // switch 상태
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    settingViewModel.mode.collect { mode ->
                        if (mode != null) {
                            switchSettingGuide.isChecked = mode
                        }
                    }
                }
            }

            // switch를 변경할 때마다 값을 갱신
            switchSettingGuide.setOnCheckedChangeListener { buttonView, isChecked ->
                settingViewModel.setSettingMode(GUIDEMODE, isChecked)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    musicViewModel.music.collect { musicList ->
                        if (musicList != null) {
                            Log.d("whatisthis", musicList.toString())
                            initRecycler(musicList.toMutableList())
                        }
                    }
                }
            }
        }
    }

//    fun queryAudioFiles(context: Context) {
//        // 오디오 파일을 조회하기 위한 URI
//        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//
//        // ContentResolver를 사용하여 쿼리 수행
//        val contentResolver: ContentResolver = context.contentResolver
//        val cursor = contentResolver.query(uri, null, null, null, null)
//
//        cursor?.use {
//            while (it.moveToNext()) {
//                // 오디오 파일의 정보를 추출
//                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
//                val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
//                val album = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
//                val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
//
//                // 추출한 정보를 사용하거나 출력
//                // 여기에서는 간단히 로그로 출력
//                // 실제로는 UI나 다른 작업에 활용 가능
//                val audioInfo = "Title: $title, Artist: $artist, Album: $album, Path: $filePath"
//                android.util.Log.d("AudioFiles", audioInfo)
//            }
//        }
//    }

    override fun onPause() {
        super.onPause()
        musicHelper?.checkMediaPlayer()
//        mediaPlayer?.release()
//        mediaPlayer = null
    }

    override fun onDestroyViewInFragMent() {
        musicHelper?.checkMediaPlayer()
        musicHelper = null
//        mediaPlayer?.release()
//        mediaPlayer = null
    }

    private fun initRecycler(result: MutableList<Music>) {
        with(binding) {
            musicAdapter = MusicAdapter(result, { selectedMusic ->
                musicHelper?.checkMediaPlayer()
                musicHelper?.playMusic(requireContext(), selectedMusic, viewLifecycleOwner)
            }, { selectedMusic ->
                musicHelper?.checkMediaPlayer()
                showDialog(selectedMusic)
            }, { selectedMusic ->
                musicHelper?.checkMediaPlayer()
                deleteMusic(selectedMusic)
            })

            rvSettingUsermusic.apply {
                layoutManager = LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.VERTICAL,
                    false
                )
                adapter = musicAdapter
            }
        }
    }

//    fun checkMediaPlayer() {
//        if (mediaPlayer != null) {
//            mediaPlayer!!.stop()
//            mediaPlayer!!.release()
//            mediaPlayer = null
//        }
//    }

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

    private fun showDialog(selectedMusic: Music) {
        val dialogFragment = MusicSettingDialogFragment(selectedMusic) {
            musicViewModel.updateMusic(it)
        }
        dialogFragment.show(childFragmentManager, "YourDialogTag")
    }

    private fun deleteMusic(selectedMusic: Music) {
        val deleteFile = File(selectedMusic.newPath!!)
        if (deleteFile.exists()) {
            val result = deleteFile.delete()
            if (result) showToast("파일 삭제")
        }
        musicViewModel.deleteMusic(selectedMusic.id)
    }


    // 오디오 파일 탐색 후 선택했을 때 콜백메서드를 설정한 intent launcher
    var launcher_audio: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 사용자가 파일 탐색 화면에서 돌아왔을 때 호출되는 메소드
        if (result.resultCode == AppCompatActivity.RESULT_OK) { // 사용자가 파일 선택을 성공적으로 완료했을 때 내부 코드 실행
            val data: Intent =
                result.data!! // 콜백 메서드를 통해 전달 받은 ActivityResult 객체에서 Intent 객체 추출

            val audioUri = data.data // Intent 객체에서 선택한 오디오 파일의 위치를 가리키는 Uri 추출

            // 앱과 안드로이드 시스템 간의 데이터 통신을 하기위해 ContentResolver 객체 생성
            // ContentResolver를 통해 앱은 ContentProvider를 사용해 다른 앱의 데이터에 접근하거나 데이터를 읽거나 쓸 수 있다
            val contentResolver = requireContext().contentResolver

            // Uri를 사용해 파일 복사본 생성후 해당 파일 경로 반환
            val newFilePath: String? = getNewFilePathFromUri(
                requireContext(),
                contentResolver,
                audioUri
            )

            // 원본 파일의 Uri로부터 절대 경로 반환
            val audioPath = getPathFromFileUri(requireContext(), audioUri)
            // 원본 파일로부터 음원 제목 추출
            val title = getFileName(contentResolver, audioUri!!)
            // 복사본이 성공적으로 생성되면 해당 위치가 기록된 파일을 Room에 저장
            if (newFilePath != null) { // 파일이 정상적으로 생성되었을 때 내부 코드 실행
                val newMusic = Music(
                    title = title,
                    newPath = newFilePath,
                    originalPath = audioPath
                )
                musicViewModel.insertMusic(newMusic)
            } else { // 파일이 정상적으로 생성되지 않았을 때 내부 코드 실행
                showToast("오디오 파일을 가져오는데 문제가 생겼습니다.") // 메시지 출력
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
            val buffer = ByteArray(1024 * 4) // 1 KB buffer 생성
            var bytesRead: Int

            // 구간 복사를 시도했지만 안됐다.... 왜 안되는지 모르겠다....
            // 작은 파일들은 되지만 큰 파일의 경우 파일 생성까지는 되지만 mediaPlayer에서 동작하지 않는다. "Error (1,-1004)"가 발생하면서 동작하지 않았다.
            // 어쩔 수 없이 그냥 전체 파일을 복사해 저장하고, 시작 구간을 저장해서 재생할 때 시간을 조절하는 방식으로 구현했다.
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead) // 읽은 byte 데이터를 사용해 File 객체에 데이터 쓰기
            }
            file.absolutePath  // 그 후 복사가 완료된 파일 객체 반환
//            // 구간 복사를 시도했지만 안됐다.... 왜 안되는지 모르겠다....
//            // 작은 파일들은 되지만 큰 파일의 경우 파일 생성까지는 되지만 mediaPlayer에서 동작하지 않는다. "Error (1,-1004)"가 발생하면서 동작하지 않았다.
//            // 어쩔 수 없이 그냥 전체 파일을 복사해 저장하고, 시작 구간을 저장해서 재생할 때 시간을 조절하는 방식으로 구현했다.
//            val MAX_COPY_BYTES = 50000 // 복사할 최대 바이트 수
//            val buffer = ByteArray(1024 * 4) // 1 KB buffer 생성
//            var bytesRead: Int
//            var totalBytesRead: Long = 0
//            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
//                // 복사한 데이터가 MAX_COPY_BYTES를 초과하면 종료
//                if (totalBytesRead + bytesRead > MAX_COPY_BYTES) {
//                    val remainingBytes = MAX_COPY_BYTES - totalBytesRead.toInt()
//                    outputStream.write(buffer, 0, remainingBytes) // 남은 만큼만 복사
//                    break
//                }
//                outputStream.write(buffer, 0, bytesRead) // 읽은 byte 데이터를 사용해 File 객체에 데이터 쓰기
//                totalBytesRead += bytesRead.toLong()
//
//                // 파일이 MAX_COPY_BYTES까지 읽혔으면 더 이상 복사하지 않고 종료
//                if (totalBytesRead >= MAX_COPY_BYTES) {
//                    break
//                }
//            }
//            file.absolutePath  // 그 후 복사가 완료된 파일 객체 반환
        } catch (e: IOException) { // 입출력 문제 생기면 오류 출력
            e.printStackTrace()
            Log.d("whatisthis", e.toString())
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
                Log.d("whatisthis", displayName.toString())
            }
            cursor.close() // 커서 종료
        }
        return displayName // 받은 파일이름 반환
    }

//    //Uri -> Path(파일경로)
//    fun getPathFromFileUri(context: Context, contentUri: Uri?): String? {
//        val projection = arrayOf(MediaStore.Audio.Media.DATA)
//        context.contentResolver.query(contentUri!!, projection, null, null, null)?.use { cursor ->
//            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
//            if (cursor.moveToFirst()) {
//                return cursor.getString(columnIndex)
//            }
//        }
//        return null
//    }
//
//    // Path -> Uri
//    fun getUriFromFilePath(context: Context, filePath: String): Uri? {
//        val file = File(filePath)
//        return if (file.exists()) {
//            try {
//                // FileProvider로 파일 검색후 Uri 생성
//                val authority = "${context.packageName}.provider"
//                FileProvider.getUriForFile(context, authority, file)
//            } catch (e: IllegalArgumentException) {
//                Log.d("whatisthis", "${context.packageName}ㅇㅔ러" + e.toString())
//                e.printStackTrace()
//                null
//            }
//        } else {
//            null
//        }
//    }

}