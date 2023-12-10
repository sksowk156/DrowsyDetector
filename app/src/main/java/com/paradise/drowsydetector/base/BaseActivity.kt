//package com.paradise.drowsydetector.base
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.annotation.LayoutRes
//import androidx.appcompat.app.AppCompatActivity
//import androidx.databinding.DataBindingUtil
//import androidx.databinding.ViewDataBinding
//
//abstract class BaseActivity<T : ViewDataBinding>(
//    @LayoutRes private val layoutResId: Int,
//) : AppCompatActivity() {
//    private var _binding: T? = null
//    val binding get() = _binding!!
//
//    protected open fun saveInstanceStateNull() {}
//    protected open fun saveInstanceStateNotNull(bundle: Bundle) {}
//
//    protected abstract fun onCreate()
//
//    protected fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        _binding = DataBindingUtil.setContentView(this, layoutResId)
//        binding.lifecycleOwner = this
//        onCreate()
//        if (savedInstanceState == null) saveInstanceStateNull()
//        else saveInstanceStateNotNull(savedInstanceState)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        _binding = null
//    }
//
//}
