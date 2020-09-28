package com.benjaminwan.ocr

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.SeekBar
import com.benjaminwan.ocrlibrary.OcrEngine
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.max

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var ocrEngine: OcrEngine
    private var selectedImg: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ocrEngine = OcrEngine(applicationContext)
        resultTV.movementMethod = ScrollingMovementMethod.getInstance()
        selectBtn.setOnClickListener(this)
        detectScaleBtn.setOnClickListener(this)
        detectResizeBtn.setOnClickListener(this)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBar ?: return
                updateScaleTv(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    fun updateScaleTv(progress: Int) {
        if (selectedImg != null) {
            val img = selectedImg ?: return
            val scale = seekBar.progress.toFloat() / 10.toFloat()
            val maxSize = max(img.width, img.height)
            val reSize = (scale * maxSize).toInt()
            scaleTv.text = "Scale:${progress}/10, Size:$reSize"
        } else {
            scaleTv.text = "Scale:${progress}/10"
        }
    }

    override fun onClick(view: View?) {
        view ?: return
        when (view.id) {
            R.id.selectBtn -> {
                val intent = Intent(Intent.ACTION_PICK).apply {
                    type = "image/*"
                }
                startActivityForResult(
                    intent, REQUEST_SELECT_IMAGE
                )
            }
            R.id.detectScaleBtn -> {
                val img = selectedImg ?: return
                val scale = seekBar.progress.toFloat() / 10.toFloat()
                val boxImg: Bitmap = Bitmap.createBitmap(
                    img.width, img.height, Bitmap.Config.ARGB_8888
                )
                Log.i(TAG, "selectedImg=${img.height},${img.width} ${img.config}")
                val start = System.currentTimeMillis()
                resultTV.text = ocrEngine.detectScale(img, scale, boxImg)
                val end = System.currentTimeMillis()
                val time = "time=${end - start}ms"
                timeTV.text = time
                val options =
                    RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                Glide.with(this).load(boxImg).apply(options).into(imageView)
            }
            R.id.detectResizeBtn -> {
                val img = selectedImg ?: return
                val scale = seekBar.progress.toFloat() / 10.toFloat()
                val maxSize = max(img.width, img.height)
                val reSize = (scale * maxSize).toInt()
                val boxImg: Bitmap = Bitmap.createBitmap(
                    img.width, img.height, Bitmap.Config.ARGB_8888
                )
                Log.i(TAG, "selectedImg=${img.height},${img.width} ${img.config}")
                val start = System.currentTimeMillis()
                resultTV.text = ocrEngine.detectResize(img, reSize, boxImg)
                val end = System.currentTimeMillis()
                val time = "time=${end - start}ms"
                timeTV.text = time
                val options =
                    RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                Glide.with(this).load(boxImg).apply(options).into(imageView)
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SELECT_IMAGE) {
            val imgUri = data.data ?: return
            val options =
                RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
            Glide.with(this).load(imgUri).apply(options).into(imageView)
            selectedImg = decodeUri(imgUri)
            updateScaleTv(seekBar.progress)
        }
    }

    companion object {
        const val REQUEST_SELECT_IMAGE = 666
        const val TAG = "OcrLite"
    }

}