//package com.paradise.drowsydetector.view
//
//import android.R
//import android.content.Intent
//import android.database.Cursor
//import android.media.AudioAttributes
//import android.media.AudioAttributes.Builder
//import android.media.MediaPlayer
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.view.View
//import android.widget.Button
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import java.io.IOException
//import java.util.concurrent.Executors
//import java.util.concurrent.ScheduledExecutorService
//import java.util.concurrent.TimeUnit
//
//
////class TempActivity : AppCompatActivity() {
////    var file_name: TextView? = null
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(com.paradise.drowsydetector.R.layout.activity_temp)
//////        file_name =
//////            findViewById<View>(com.paradise.drowsydetector.R.id.file_name) as TextView // 오디오 파일의 이름을 표시할 TextView 변수에 참조
//////        val bring_audio =
//////            findViewById<View>(com.paradise.drowsydetector.R.id.bring_file) as Button // 오디오 파일을 탐색하기 위해 클릭할 Button 변수에 참조
//////
//////        bring_audio.setOnClickListener {
//////            file_name!!.text = "" // 오디오 파일을 탐색하면 기존 파일이름을 지운다
//////
//////            // ACTION_GET_CONTENT - 문서나 사진 등의 파일을 선택하고 앱에 그 참조를 반환하기 위해 요청하는 액션
//////            val intent = Intent(Intent.ACTION_GET_CONTENT)
//////            intent.type = "audio/*" // 탐색할 파일 MIME 타입 설정
//////            launcher_audio.launch(intent) // 파일탐색 액션을 가진 인텐트 실행
//////        }
////    }
////
////    // 오디오 파일 탐색 후 선택했을 때 콜백메서드를 설정한 intent launcher
////    var launcher_audio: ActivityResultLauncher<Intent> = registerForActivityResult(
////        ActivityResultContracts.StartActivityForResult()
////    ) { result ->
////        // 사용자가 파일 탐색 화면에서 돌아왔을 때 호출되는 메소드
////        if (result.getResultCode() === RESULT_OK) { // 사용자가 파일 선택을 성공적으로 완료했을 때 내부 코드 실행
////            Log.d("launcher_audio Callback", "audio picking has succeeded") // 로그 출력
////            val data: Intent =
////                result.getData()!! // 콜백 메서드를 통해 전달 받은 ActivityResult 객체에서 Intent 객체 추출
////            val audioUri = data.data // Intent 객체에서 선택한 오디오 파일의 위치를 가리키는 Uri 추출
////            val audioFile: File = getFileFromUri(
////                this@TempActivity,
////                audioUri
////            )!! // Uri를 사용해 파일 복사본 생성
////            if (audioFile != null) { // 파일이 정상적으로 생성되었을 때 내부 코드 실행
////                file_name!!.text = audioFile.name // 복사본 파일의 이름으로 TextView에 설정
////            } else { // 파일이 정상적으로 생성되지 않았을 때 내부 코드 실행
////                Toast.makeText(
////                    this@TempActivity,
////                    "오디오 파일을 가져오는데 문제가 생겼습니다.",
////                    Toast.LENGTH_SHORT
////                ).show() // 메시지 출력
////            }
////        } else if (result.getResultCode() === RESULT_CANCELED) { // 사용자가 파일 탐색 중 선택을 하지 않았을 때 내부 코드 실행
////            Log.d("launcher_audio Callback", "audio picking is canceled") // 로그 출력
////        } else { // 그 외의 경우 예외 처리
////            Log.e("launcher_audio Callback", "audio picking has failed") // 로그 출력
////        }
////    }
////    companion object {
////        // URI를 사용해 파일을 복사하고 복사한 경로를 제공하는 메소드
////        fun getFileFromUri(context: Context, uri: Uri?): File? {
////            // 앱과 안드로이드 시스템 간의 데이터 통신을 하기위해 ContentResolver 객체 생성
////            // ContentResolver를 통해 앱은 ContentProvider를 사용해 다른 앱의 데이터에 접근하거나 데이터를 읽거나 쓸 수 있다
////            val contentResolver = context.contentResolver
////            // Uri를 사용해 파일 이름 반환
////            val fileName: String = getFileName(contentResolver, uri!!)!!
////            var inputStream: InputStream? = null
////            var outputStream: FileOutputStream? = null
////
////            // 같은 이름으로 앱 내부저장소에 파일 생성
////            val file = File(context.cacheDir, fileName)
////            return try {
////                inputStream =
////                    contentResolver.openInputStream(uri!!) // uri에 있는 데이터를 읽기 위해 InputStream을 열고 InputStream을 반환한다
////                if (inputStream == null) {
////                    return null // InputStream을 여는데 실패할 경우 null 반환
////                }
////                outputStream = FileOutputStream(file) // 이전에 만든 File 객체에 데이터를 쓰기 위해 OutputStream 생성
////                val buffer = ByteArray(4 * 1024) // 4 KB buffer 생성
////                var bytesRead: Int
////                while (inputStream.read(buffer)
////                        .also { bytesRead = it } != -1
////                ) { // inputStream을 사용해 데이터를 읽고 읽은 데이터가 있다면 while문 내부 코드 실행
////                    outputStream.write(buffer, 0, bytesRead) // 읽은 byte 데이터를 사용해 File 객체에 데이터 쓰기
////                }
////                file // 그 후 복사가 완료된 파일 객체 반환
////            } catch (e: IOException) { // 입출력 문제 생기면 오류 출력
////                e.printStackTrace()
////                null
////            } finally {
////                // 마지막으로 사용한 inputStream, outputStream 종료
////                try {
////                    inputStream?.close()
////                    outputStream?.close()
////                } catch (e: IOException) { // 입출력 문제 생기면 오류 출력
////                    e.printStackTrace()
////                }
////            }
////        }
////
////        // uri를 사용해 파일 이름을 반환하는 메서드
////        private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
////            // ContentProvider 를 통해 기기 데이터베이스에서 데이터를 조회하기 위해 query() 메서드 사용
////            val cursor = contentResolver.query(uri, null, null, null, null)
////            var displayName: String? = null
////            if (cursor != null && cursor.moveToFirst()) { // 조회된 데이터를 저장한 cursor가 null이 아니고 첫번째 레코드로 이동할 수 있으면(첫번째 레코드가 없다면 false 반환) 내부 코드 실행
////                val nameIndex =
////                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) // 파일이름이 저장된 열의 위치 반환 후 저장
////                if (nameIndex != -1) { // 해당 열이 존재할 경우 내부 코드 실행
////                    displayName = cursor.getString(nameIndex) // 열에 있는 데이터 반환
////                }
////                cursor.close() // 커서 종료
////            }
////            return displayName // 받은 파일이름 반환
////        }
////    }
////
////
////}
//
//class MainActivity : AppCompatActivity() {
//    var textview2: TextView? = null
//    var textview3: TextView? = null
//    var button1: Button? = null
//    var button2: Button? = null
//    var seekbar1: SeekBar? = null
//    var duration: String? = null
//    var mediaPlayer: MediaPlayer? = null
//    var timer: ScheduledExecutorService? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        button1 = findViewById(R.id.button1)
//        button2 = findViewById(R.id.button2)
//        textview2 = findViewById<TextView>(R.id.textView2)
//        textview3 = findViewById<TextView>(R.id.textView3)
//        seekbar1 = findViewById(R.id.seekbar1)
//        button1.setOnClickListener(View.OnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type = "audio/*"
//            startActivityForResult(intent, MainActivity.Companion.PICK_FILE)
//        })
//        button2.setOnClickListener(View.OnClickListener {
//            if (mediaPlayer != null) {
//                if (mediaPlayer!!.isPlaying) {
//                    mediaPlayer!!.pause()
//                    button2.setText("PLAY")
//                    timer!!.shutdown()
//                } else {
//                    mediaPlayer!!.start()
//                    button2.setText("PAUSE")
//                    timer = Executors.newScheduledThreadPool(1)
//                    timer!!.scheduleAtFixedRate(Runnable {
//                        if (mediaPlayer != null) {
//                            if (!seekbar1.isPressed()) {
//                                seekbar1.setProgress(mediaPlayer!!.currentPosition)
//                            }
//                        }
//                    }, 10, 10, TimeUnit.MILLISECONDS)
//                }
//            }
//        })
//        seekbar1.setOnSeekBarChangeListener(object : OnSeekBarChangeListener() {
//            fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                if (mediaPlayer != null) {
//                    val millis = mediaPlayer!!.currentPosition
//                    val total_secs: Long = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS)
//                    val mins: Long = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS)
//                    val secs = total_secs - mins * 60
//                    textview3.setText("$mins:$secs / $duration")
//                }
//            }
//
//            fun onStartTrackingTouch(seekBar: SeekBar?) {}
//            fun onStopTrackingTouch(seekBar: SeekBar?) {
//                if (mediaPlayer != null) {
//                    mediaPlayer!!.seekTo(seekbar1.getProgress())
//                }
//            }
//        })
//        button2.setEnabled(false)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == MainActivity.Companion.PICK_FILE && resultCode == RESULT_OK) {
//            if (data != null) {
//                val uri = data.data
//                createMediaPlayer(uri)
//            }
//        }
//    }
//
//    fun createMediaPlayer(uri: Uri?) {
//        mediaPlayer = MediaPlayer()
//        mediaPlayer!!.setAudioAttributes(
//            Builder()
//                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
//                .setUsage(AudioAttributes.USAGE_MEDIA)
//                .build()
//        )
//        try {
//            mediaPlayer!!.setDataSource(applicationContext, uri!!)
//            mediaPlayer!!.prepare()
//            textview2!!.text = getNameFromUri(uri)
//            button2!!.isEnabled = true
//            val millis = mediaPlayer!!.duration
//            val total_secs: Long = TimeUnit.SECONDS.convert(millis, TimeUnit.MILLISECONDS)
//            val mins: Long = TimeUnit.MINUTES.convert(total_secs, TimeUnit.SECONDS)
//            val secs = total_secs - mins * 60
//            duration = "$mins:$secs"
//            textview3!!.text = "00:00 / $duration"
//            seekbar1.setMax(millis)
//            seekbar1.setProgress(0)
//            mediaPlayer!!.setOnCompletionListener { releaseMediaPlayer() }
//        } catch (e: IOException) {
//            textview2!!.text = e.toString()
//        }
//    }
//
//    fun getNameFromUri(uri: Uri?): String {
//        var fileName = ""
//        var cursor: Cursor? = null
//        cursor = contentResolver.query(
//            uri!!, arrayOf(
//                MediaStore.Images.ImageColumns.DISPLAY_NAME
//            ), null, null, null
//        )
//        if (cursor != null && cursor.moveToFirst()) {
//            fileName =
//                cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME))
//        }
//        cursor?.close()
//        return fileName
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        releaseMediaPlayer()
//    }
//
//    fun releaseMediaPlayer() {
//        if (timer != null) {
//            timer!!.shutdown()
//        }
//        if (mediaPlayer != null) {
//            mediaPlayer!!.release()
//            mediaPlayer = null
//        }
//        button2!!.isEnabled = false
//        textview2!!.text = "TITLE"
//        textview3!!.text = "00:00 / 00:00"
//        seekbar1.setMax(100)
//        seekbar1.setProgress(0)
//    }
//
//    companion object {
//        const val PICK_FILE = 99
//    }
//}