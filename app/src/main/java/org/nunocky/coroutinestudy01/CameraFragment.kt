package org.nunocky.coroutinestudy01

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.nunocky.coroutinestudy01.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    interface Callback {
        fun onCameraPreview(bitmap: Bitmap)
    }

    companion object {
        private const val TAG = "CameraFragment"

        @JvmStatic
        fun newInstance(): CameraFragment {
            val args = Bundle()

            val fragment = CameraFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: FragmentCameraBinding
    private lateinit var cameraExecutor: ExecutorService

    private var callback: Callback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            callback = activity as Callback
        } catch (ex: ClassCastException) {
            throw RuntimeException("activity must implement CameraFragment.Callback")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callback = null
        cameraExecutor.shutdown()
    }


    override fun onResume() {
        super.onResume()
        binding.camera.post { startCamera() }
    }

//    override fun onPause() {
//        super.onPause()
//    }

    private fun startCamera() {
        val viewFinder = binding.camera

        activity?.let { context ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

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

                            activity?.runOnUiThread {
                                viewFinder.bitmap?.let { bitmap -> callback?.onCameraPreview(bitmap) }
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

            }, ContextCompat.getMainExecutor(context))
        }
    }

}