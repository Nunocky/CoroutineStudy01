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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.nunocky.coroutinestudy01.databinding.ActivitySample1Binding


class Sample1ViewModel : ViewModel() {
    companion object {
        private const val TAG = "Sample1ViewModel"
    }

    private val _image = MutableLiveData<Bitmap?>(null)
    val image: LiveData<Bitmap?> = _image

    private val mutex = Mutex()
    private var job: Job? = null

    fun processCoroutine(bitmap: Bitmap) {
        viewModelScope.launch {
            mutex.withLock {
                if (job == null) {
                    job = viewModelScope.launch {
                        val newBitmap = bitmap.flipHorizontal()
                        _image.postValue(newBitmap)

                        //delay(100)

                        mutex.withLock {
                            job = null
                        }
                    }
                } else {
                    Log.d(TAG, "frame skipped")
                }
            }
        }
    }
}

class Sample1Activity : AppCompatActivity(), CameraFragment.Callback {
//    companion object {
//        private const val TAG = "Sample1Activity"
//    }

    private val viewModel: Sample1ViewModel by viewModels()
    private lateinit var binding: ActivitySample1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample1)
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