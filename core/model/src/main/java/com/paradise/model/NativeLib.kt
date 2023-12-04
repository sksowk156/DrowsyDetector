package com.paradise.model

class NativeLib {

    /**
     * A native method that is implemented by the 'model' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'model' library on application startup.
        init {
            System.loadLibrary("model")
        }
    }
}