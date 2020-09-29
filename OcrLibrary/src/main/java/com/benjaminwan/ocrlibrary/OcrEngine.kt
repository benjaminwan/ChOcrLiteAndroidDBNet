package com.benjaminwan.ocrlibrary

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap

class OcrEngine(context: Context) {
    init {
        System.loadLibrary("OcrLite")
        val ret = init(context.assets)
        if (!ret) throw IllegalArgumentException()
    }

    var boxScoreThresh: Float = 0.5f
    var boxThread: Float = 0.3f
    var miniArea: Float = 3f

    fun detect(input: Bitmap, output: Bitmap, reSize: Int) =
        detect(input, output, reSize, boxScoreThresh, boxThread, miniArea)

    external fun init(assetManager: AssetManager): Boolean
    external fun detect(
        input: Bitmap, output: Bitmap, reSize: Int,
        boxScoreThread: Float, boxThread: Float, miniArea: Float
    ): String

}