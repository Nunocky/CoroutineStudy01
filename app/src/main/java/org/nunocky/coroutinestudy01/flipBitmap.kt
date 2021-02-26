package org.nunocky.coroutinestudy01

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.flipHorizontal(): Bitmap {
    val src = this
    val mat = Matrix().apply {
        postScale(-1f, 1f)
    }

    return Bitmap.createBitmap(
        src,
        0,
        0,
        src.width,
        src.height,
        mat,
        false
    )
}