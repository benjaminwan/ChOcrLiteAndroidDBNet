package com.benjaminwan.ocr.dbnet

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.benjaminwan.ocrlibrary.OcrEngine
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uber.autodispose.android.lifecycle.autoDisposable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.max

class MainActivity : AppCompatActivity(), View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private lateinit var ocrEngine: OcrEngine
    private var selectedImg: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ocrEngine = OcrEngine(applicationContext)
        resultTV.movementMethod = ScrollingMovementMethod.getInstance()
        selectBtn.setOnClickListener(this)
        detectBtn.setOnClickListener(this)
        boxScoreThreshSeekBar.setOnSeekBarChangeListener(this)
        boxThreshSeekBar.setOnSeekBarChangeListener(this)
        minAreaSeekBar.setOnSeekBarChangeListener(this)
        scaleSeekBar.setOnSeekBarChangeListener(this)
        angleWidthSeekBar.setOnSeekBarChangeListener(this)
        angleHeightSeekBar.setOnSeekBarChangeListener(this)
        textWidthSeekBar.setOnSeekBarChangeListener(this)
        textHeightSeekBar.setOnSeekBarChangeListener(this)
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
                val img = selectedImg ?: return
                val scale = scaleSeekBar.progress.toFloat() / 100.toFloat()
                val maxSize = max(img.width, img.height)
                val reSize = (scale * maxSize).toInt()
                detect(img, reSize)
                /*val boxImg: Bitmap = Bitmap.createBitmap(
                    img.width, img.height, Bitmap.Config.ARGB_8888
                )
                Log.i(TAG, "selectedImg=${img.height},${img.width} ${img.config}")
                val start = System.currentTimeMillis()
                resultTV.text = ocrEngine.detect(img, boxImg, reSize)
                val end = System.currentTimeMillis()
                val time = "time=${end - start}ms"
                timeTV.text = time
                val options =
                    RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                Glide.with(this).load(boxImg).apply(options).into(imageView)*/
            }
            else -> {
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        seekBar ?: return
        when (seekBar.id) {
            R.id.scaleSeekBar -> {
                updateScale(progress)
            }
            R.id.boxScoreThreshSeekBar -> {
                updateBoxScoreThresh(progress)
            }
            R.id.boxThreshSeekBar -> {
                updateBoxThresh(progress)
            }
            R.id.minAreaSeekBar -> {
                updateMinArea(progress)
            }
            R.id.angleWidthSeekBar -> {
                updateAngleWidth(progress)
            }
            R.id.angleHeightSeekBar -> {
                updateAngleHeight(progress)
            }
            R.id.textWidthSeekBar -> {
                updateTextWidth(progress)
            }
            R.id.textHeightSeekBar -> {
                updateTextHeight(progress)
            }
            else -> {
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    private fun updateScale(progress: Int) {
        val scale = progress.toFloat() / 100.toFloat()
        if (selectedImg != null) {
            val img = selectedImg ?: return
            val maxSize = max(img.width, img.height)
            val reSize = (scale * maxSize).toInt()
            scaleTv.text = "Size:$reSize(${scale * 100}%)"
        } else {
            scaleTv.text = "Size:0(${scale * 100}%)"
        }
    }

    private fun updateBoxScoreThresh(progress: Int) {
        val thresh = progress.toFloat() / 100.toFloat()
        boxScoreThreshTv.text = "BoxScoreThresh:$thresh"
        ocrEngine.boxScoreThresh = thresh
    }

    private fun updateBoxThresh(progress: Int) {
        val thresh = progress.toFloat() / 100.toFloat()
        boxThreshTv.text = "BoxThresh:$thresh"
        ocrEngine.boxThresh = thresh
    }

    private fun updateMinArea(progress: Int) {
        minAreaTv.text = "MinArea:$progress"
        ocrEngine.miniArea = progress.toFloat()
    }

    private fun updateAngleWidth(progress: Int) {
        val scale = progress.toFloat() / 10.toFloat()
        angleWidthTv.text = "AngleWidth:$scale"
        ocrEngine.angleScaleWidth = scale
    }

    private fun updateAngleHeight(progress: Int) {
        val scale = progress.toFloat() / 10.toFloat()
        angleHeightTv.text = "AngleHeight:$scale"
        ocrEngine.angleScaleHeight = scale
    }

    private fun updateTextWidth(progress: Int) {
        val scale = progress.toFloat() / 10.toFloat()
        textWidthTv.text = "TextWidth:$scale"
        ocrEngine.textScaleWidth = scale
    }

    private fun updateTextHeight(progress: Int) {
        val scale = progress.toFloat() / 10.toFloat()
        textHeightTv.text = "TextHeight:$scale"
        ocrEngine.textScaleHeight = scale
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
            updateScale(scaleSeekBar.progress)
            clearLastResult()
        }
    }

    private fun showLoading() {
        Glide.with(this).load(R.drawable.loading_anim).into(imageView)
    }

    private fun clearLastResult() {
        resultTV.text = ""
        timeTV.text = ""
    }

    private fun detect(img: Bitmap, reSize: Int) {
        Single.fromCallable {
            val boxImg: Bitmap = Bitmap.createBitmap(
                img.width, img.height, Bitmap.Config.ARGB_8888
            )
            Log.i(TAG, "selectedImg=${img.height},${img.width} ${img.config}")
            val start = System.currentTimeMillis()
            val text = ocrEngine.detect(img, boxImg, reSize)
            val end = System.currentTimeMillis()
            val time = "time=${end - start}ms"
            OcrResult(boxImg, text, time)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { showLoading() }
            .doFinally { /*hideLoading()*/ }
            .autoDisposable(this)
            .subscribe { t1, t2 ->
                timeTV.text = t1.time
                resultTV.text = t1.text
                val options =
                    RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)
                Glide.with(this).load(t1.bitmap).apply(options).into(imageView)
            }
    }

    companion object {
        const val REQUEST_SELECT_IMAGE = 666
        const val TAG = "OcrLite"
    }


}