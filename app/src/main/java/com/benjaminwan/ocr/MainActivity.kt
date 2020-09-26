package com.benjaminwan.ocr

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var selectedImg: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        resultTV.movementMethod = ScrollingMovementMethod.getInstance()
        selectBtn.setOnClickListener(this)
        detectBtn.setOnClickListener(this)
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
            R.id.detectBtn -> {

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
        }
    }

    companion object {
        const val REQUEST_SELECT_IMAGE = 666
    }

}