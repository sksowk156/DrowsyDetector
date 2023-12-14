//package com.paradise.drowsydetector.utils.helper
//
//import android.content.Context
//import android.media.AudioManager
//import java.lang.ref.WeakReference
//
//class VolumeHelper(private var contextRef: WeakReference<Context>) { // 서비스에서 사용하면 계속 메모리 릭이 뜬다.
//    companion object {
//        @Volatile
//        private var instance: VolumeHelper? = null
//        fun getInstance(context: Context) = instance ?: synchronized(this) {
//            // LocationSercie 객체를 생성할 때 같이 한번만 객체를 생성한다.
//            instance ?: VolumeHelper(
//                WeakReference(context),
//            ).also {
//                instance = it
//            }
//        }
//    }
//
//    fun releaseVolumeHelper() {
//        audioManager = null
//    }
//
//    fun clearContext() {
//        releaseVolumeHelper()
//        contextRef.clear()
//        instance = null
//    }
//
//    var audioManager: AudioManager? = null
//
//    fun initAudio() {
//        contextRef.get()?.let { context ->
//            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
//        }
//    }
//
//    fun getMaxVolume() = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
//
//    fun getCurrentVolume() = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
//
//    fun setVolume(volume: Int) {
//        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
//    }
//}