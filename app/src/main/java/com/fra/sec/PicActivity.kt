package com.fra.sec

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.fra.sec.databinding.ActivityPicBinding
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class PicActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPicBinding
    private var picUri: Uri? = null
    private var bitmap: Bitmap? = null
    private var mType = Type.SAVE

    enum class Type {
        SAVE, SHARE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListener()
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 210)
        setAdapter()
    }

    private val launch =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { grant ->
            if (grant) {
                save2()
            }
        }

    private fun setListener() {
        binding.back.setOnClickListener {
            backHandle()
        }
        binding.tvSave.setOnClickListener {
            if (mType == Type.SAVE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    save1()
                } else {
                    if (ActivityCompat.checkSelfPermission(
                            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        save2()
                    } else {
                        launch.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            } else {
                runCatching {
                    val file = File(externalCacheDir, "frame_pic_share")
                    val out = FileOutputStream(file)
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "image/png"
                    intent.putExtra(
                        Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                            this@PicActivity,
                            applicationInfo.packageName + ".provider",
                            file
                        )
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(Intent.createChooser(intent, "share"))
                }
            }
        }
        onBackPressedDispatcher.addCallback { backHandle() }
    }

    private fun backHandle() {
        if (mType == Type.SHARE) {
            mType = Type.SAVE
            setText()
            binding.tvSuccess.visibility = View.GONE
            binding.bottomLayout.visibility = View.VISIBLE
        } else {
            finish()
        }
    }

    private fun save1() {
        bitmap = binding.viewGroupFrame.drawToBitmap()
        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME, "${System.currentTimeMillis()}.png"
                )
                put(
                    MediaStore.Images.Media.DATE_MODIFIED, SimpleDateFormat(
                        "yyyy-MM-dd hh:mm:ss", Locale.getDefault()
                    ).format(System.currentTimeMillis())
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES
                )
            }
        )
        if (uri != null) {
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
                binding.tvSuccess.visibility = View.VISIBLE
                binding.bottomLayout.visibility = View.INVISIBLE
                mType = Type.SHARE
                setText()
            }
        }
    }

    private fun save2() {
        runCatching {
            val dirFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (dirFolder.exists() || dirFolder.mkdir()) {
                val file = File(dirFolder, "${System.currentTimeMillis()}.png")
                val fileOutputStream = FileOutputStream(file)
                bitmap = binding.viewGroupFrame.drawToBitmap()
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
                sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)
                    )
                )
                binding.tvSuccess.visibility = View.VISIBLE
                binding.bottomLayout.visibility = View.INVISIBLE
                mType = Type.SHARE
                setText()
            }
        }
    }

    private fun setText() {
        if (mType == Type.SAVE) {
            binding.tvSave.text = "Save"
        } else {
            binding.tvSave.text = "Share"
        }
    }

    private fun setAdapter() {
        val data = arrayListOf<Bean>()
        runCatching {
            data.add(Bean().apply {
                frameId = R.mipmap.ic_empty_frame
            })
            for (i in 1..9) {
                data.add(Bean().apply {
                    resId = resources.getIdentifier("ic_pic_frame$i", "mipmap", packageName)
                    frameId = resources.getIdentifier("ic_frame$i", "mipmap", packageName)
                })
            }
        }
        binding.rec.adapter = FrameAdapter(data) { pos ->
            runCatching {
                if (pos == 0) {
                    binding.ivPhoto.visibility = View.VISIBLE
                    binding.viewGroupFrame.visibility = View.GONE
                    binding.tvSave.visibility = View.GONE
                } else {
                    binding.ivPhoto.visibility = View.GONE
                    binding.viewGroupFrame.visibility = View.VISIBLE
                    binding.tvSave.visibility = View.VISIBLE
                    binding.ivFrame.setBackgroundResource(data[pos].resId)
                }
            }
        }
        binding.rec.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.rec.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(12, 0, 12, 0)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 210 && resultCode == RESULT_OK) {
            if (data != null) {
                picUri = data.data
                binding.ivPhoto.setImageURI(picUri)
                binding.ivPic.setImageURI(picUri)
            }
        } else finish()
    }

}