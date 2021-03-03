package org.nunocky.coroutinestudy01

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.nunocky.coroutinestudy01.databinding.ActivityMainBinding
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    companion object {
        const val REQUEST_CODE = 999
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val klass = when (position) {
                1 -> {
                    Sample2Activity::class.java
                }
                2 -> {
                    Sample3Activity::class.java
                }
                3 -> {
                    Sample4Activity::class.java
                }
                else -> {
                    Sample1Activity::class.java
                }
            }

            startActivity(Intent(this, klass))
        }

        val permissions = arrayOf(
            Manifest.permission.CAMERA
        )

        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            EasyPermissions.requestPermissions(this, "permission check", REQUEST_CODE, *permissions)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        recreate()
    }

    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        finish()
    }
}