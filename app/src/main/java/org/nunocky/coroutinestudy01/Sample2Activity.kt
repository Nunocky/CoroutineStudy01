package org.nunocky.coroutinestudy01

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.nunocky.coroutinestudy01.databinding.ActivitySample2Binding


class Sample2ViewModel : ViewModel() {
    companion object {
        private const val TAG = "Sample2ViewModel"
    }

    private val _image = MutableLiveData<Bitmap?>(null)
    val image: LiveData<Bitmap?> = _image

    private var job: Job? = null

    @MainThread
    fun processCoroutine(bitmap: Bitmap) {
        if (job != null && job!!.isActive) {
            Log.d(TAG, "frame skipped")
            return
        }

        job = viewModelScope.launch {
            val newBitmap = bitmap.flipHorizontal()
            _image.value = newBitmap
            //delay(100)
            job = null
        }
    }
}

class Sample2Activity : AppCompatActivity(), CameraFragment.Callback {
    companion object {
        private const val TAG = "Sample2Activity"
    }

    private val viewModel: Sample2ViewModel by viewModels()
    private lateinit var binding: ActivitySample2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample2)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        supportFragmentManager.beginTransaction()
            .replace(R.id.content, CameraFragment.newInstance(), "camera").commit()

        viewModel.image.observe(this) {
            it?.let { binding.imageView.setImageBitmap(it) }
        }
    }

    override fun onCameraPreview(bitmap: Bitmap) {
        viewModel.processCoroutine(bitmap)
    }
}