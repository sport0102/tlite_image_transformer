package com.aiden.tflite.tliteimagetransformer

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aiden.tflite.tliteimagetransformer.databinding.ActivityMainBinding
import com.aiden.tflite.tliteimagetransformer.util.ImageHelper
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater, null, false) }
    private val transferViewModel: TransferViewModel by viewModels()
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { url: Uri? ->
        val imageUri = url ?: return@registerForActivityResult
        var bitmap: Bitmap? = null
        try {
            bitmap = if (Build.VERSION.SDK_INT >= 28) {
                val src = ImageDecoder.createSource(contentResolver, imageUri)
                ImageDecoder.decodeBitmap(src).copy(Bitmap.Config.RGBA_F16, true)
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            }
        } catch (exception: IOException) {
            Toast.makeText(this, "Can not load image!!", Toast.LENGTH_SHORT).show()
        }
        bitmap?.let {
            val centerCropBitmap = ImageHelper.createCenterCropBitmap(it)
            transferViewModel.setOriginalImageBitmap(centerCropBitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        bind()
        initViewModel()
    }

    private fun bind() {
        binding.run {
            btnSelectPhoto.setOnClickListener {
                getContent.launch("image/*")
            }
            btnTransfer.setOnClickListener {
                transferViewModel.transferImage()
            }
        }
    }

    private fun initViewModel() {
        transferViewModel.run {
            originalImageBitmap.observe(this@MainActivity, {
                binding.imageOriginal.setImageBitmap(it)
            })
            transferResult.observe(this@MainActivity, {
                binding.imageTransform.setImageBitmap(it.styledImage)
                Log.d("transferResult", "?????? ?????? : ${it.executionLog}")
                Log.d("transferResult", "??????????????? : ${it.errorMessage}")
                Log.d("transferResult", "??? ?????? ?????? : ${it.postProcessTime}")
                Log.d("transferResult", "??? ?????? ?????? : ${it.preProcessTime}")
                Log.d("transferResult", "????????? ?????? ?????? : ${it.stylePredictTime}")
                Log.d("transferResult", "????????? ?????? ?????? : ${it.styleTransferTime}")
                Log.d("transferResult", "??? ?????? ?????? : ${it.totalExecutionTime}")
            })
        }
    }
}