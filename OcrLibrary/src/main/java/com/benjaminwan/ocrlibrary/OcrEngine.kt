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

    external fun init(assetManager: AssetManager): Boolean
    external fun detectResize(input: Bitmap, reSize: Int, output: Bitmap): String
    external fun detectScale(input: Bitmap, scale: Float, output: Bitmap): String
}