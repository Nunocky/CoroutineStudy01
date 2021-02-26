package org.nunocky.coroutinestudy01

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.nunocky.coroutinestudy01.databinding.ActivitySample3Binding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class Sample3ViewModel : ViewModel() {
    companion object {
        private const val TAG = "Sample3ViewModel"
    }

    private val _image = MutableLiveData<Bitmap?>(null)
    val image: LiveData<Bitmap?> = _image

    private var job: Job? = null

    fun processCoroutine(bitmap: Bitmap) = viewModelScope.launch {
        if (job != null) {
            Log.d(TAG, "frame skipped")
            return@launch
        }

        job = viewModelScope.launch {
            val newBitmap = bitmap.flipHorizontal()
            _image.postValue(newBitmap)
            //delay(100)
            job = null
        }
    }
}

class Sample3Activity : AppCompatActivity() {
    companion object {
        private const val TAG = "Sample3Activity"
    }

    private val viewModel: Sample3ViewModel by viewModels()
    private lateinit var binding: ActivitySample3Binding
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample3)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.image.observe(this) {
            it?.let { binding.imageView.setImageBitmap(it) }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onResume() {
        super.onResume()
        binding.camera.post { startCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun startCamera() {
        val viewFinder = binding.camera

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->

                        runOnUiThread {
                            val bitmap = viewFinder.bitmap
                            if (bitmap != null) {
                                viewModel.processCoroutine(bitmap)
                            }
                        }
                        imageProxy.close()
                    }
                }

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed")
            }

        }, ContextCompat.getMainExecutor(this))
    }

}