package org.nunocky.coroutinestudy01

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.nunocky.coroutinestudy01.databinding.ActivitySample4Binding


class Sample4ViewModel : ViewModel() {
    companion object {
        private const val TAG = "Sample4ViewModel"
    }

    private val _image = MutableLiveData<Bitmap?>(null)
    val image: LiveData<Bitmap?> = _image

    private var job: Job = Job().apply { cancel() }

    fun processCoroutine(bitmap: Bitmap) {
        viewModelScope.launch {
            if (!job.isActive) {
                job = viewModelScope.launch {
                    val newBitmap = bitmap.flipHorizontal()
                    _image.postValue(newBitmap)
                    //delay(100)
                }
            } else {
                Log.d(TAG, "frame skipped")
            }
        }
    }
}

class Sample4Activity : AppCompatActivity(), CameraFragment.Callback {
    companion object {
        private const val TAG = "Sample4Activity"
    }

    private val viewModel: Sample4ViewModel by viewModels()
    private lateinit var binding: ActivitySample4Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample4)
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