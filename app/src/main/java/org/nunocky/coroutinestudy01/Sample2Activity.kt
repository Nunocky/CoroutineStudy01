package org.nunocky.coroutinestudy01

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.annotation.MainThread
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
import org.nunocky.coroutinestudy01.databinding.ActivitySample2Binding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


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

class Sample2Activity : AppCompatActivity() {
    companion object {
        private const val TAG = "Sample2Activity"
    }

    private val viewModel: Sample2ViewModel by viewModels()
    private lateinit var binding: ActivitySample2Binding
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sample2)
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