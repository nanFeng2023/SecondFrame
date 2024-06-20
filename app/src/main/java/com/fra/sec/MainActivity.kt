package com.fra.sec

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fra.sec.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBg.setOnClickListener {
            permission()
        }
        binding.setting.setOnClickListener {
            if (!binding.drawer.isOpen) binding.drawer.open()
        }
        binding.relativePolicy.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://www.baidu.com")
                ).addCategory(Intent.CATEGORY_BROWSABLE)
            )
        }
    }

    private fun permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkP(Manifest.permission.READ_MEDIA_IMAGES, {
                jump()
            }, {
                requestP(arrayOf(Manifest.permission.READ_MEDIA_IMAGES))
            })
        } else {
            checkP(Manifest.permission.READ_EXTERNAL_STORAGE, {
                jump()
            }, {
                requestP(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            })
        }
    }

    private fun checkP(permission: String, onGrant: () -> Unit, onDenied: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onGrant.invoke()
        } else {
            onDenied.invoke()
        }
    }

    private fun requestP(permissions: Array<String>) {
        ActivityCompat.requestPermissions(this, permissions, 66)
    }

    private fun jump() = startActivity(Intent(this, PicActivity::class.java))
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 66 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            jump()
        } else {
            Toast.makeText(this, "Please enable permissions", Toast.LENGTH_SHORT).show()
        }
    }
}