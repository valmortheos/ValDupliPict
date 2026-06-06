package com.valmortheosz.valduplipict.domain.algorithm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SSIMCalculator @Inject constructor() {

    suspend fun calculateSSIM(path1: String, path2: String): Double = withContext(Dispatchers.IO) {
        try {
            val size = 64
            val bmp1 = decodeAndScale(path1, size) ?: return@withContext 0.0
            val bmp2 = decodeAndScale(path2, size) ?: return@withContext 0.0

            val ssim = computeSSIM(bmp1, bmp2)
            bmp1.recycle()
            bmp2.recycle()
            return@withContext ssim
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    private fun computeSSIM(bmp1: Bitmap, bmp2: Bitmap): Double {
        val width = bmp1.width
        val height = bmp1.height

        var ssimSum = 0.0
        var blockCount = 0

        val blockSize = 8
        for (y in 0 until height - blockSize + 1 step blockSize) {
            for (x in 0 until width - blockSize + 1 step blockSize) {
                ssimSum += computeBlockSSIM(bmp1, bmp2, x, y, blockSize)
                blockCount++
            }
        }

        return if (blockCount == 0) 0.0 else ssimSum / blockCount
    }

    private fun computeBlockSSIM(bmp1: Bitmap, bmp2: Bitmap, startX: Int, startY: Int, blockSize: Int): Double {
        var mean1 = 0.0
        var mean2 = 0.0

        val pixels1 = IntArray(blockSize * blockSize)
        val pixels2 = IntArray(blockSize * blockSize)
        bmp1.getPixels(pixels1, 0, blockSize, startX, startY, blockSize, blockSize)
        bmp2.getPixels(pixels2, 0, blockSize, startX, startY, blockSize, blockSize)

        val luma1 = DoubleArray(blockSize * blockSize)
        val luma2 = DoubleArray(blockSize * blockSize)

        for (i in 0 until blockSize * blockSize) {
            luma1[i] = getLuma(pixels1[i]).toDouble()
            luma2[i] = getLuma(pixels2[i]).toDouble()
            mean1 += luma1[i]
            mean2 += luma2[i]
        }

        mean1 /= (blockSize * blockSize)
        mean2 /= (blockSize * blockSize)

        var var1 = 0.0
        var var2 = 0.0
        var cov12 = 0.0

        for (i in 0 until blockSize * blockSize) {
            var1 += (luma1[i] - mean1) * (luma1[i] - mean1)
            var2 += (luma2[i] - mean2) * (luma2[i] - mean2)
            cov12 += (luma1[i] - mean1) * (luma2[i] - mean2)
        }

        val n = (blockSize * blockSize) - 1.0
        var1 /= n
        var2 /= n
        cov12 /= n

        val c1 = (0.01 * 255) * (0.01 * 255)
        val c2 = (0.03 * 255) * (0.03 * 255)

        val num = (2 * mean1 * mean2 + c1) * (2 * cov12 + c2)
        val den = (mean1 * mean1 + mean2 * mean2 + c1) * (var1 + var2 + c2)

        return num / den
    }

    private fun getLuma(pixel: Int): Int {
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        return (0.299 * r + 0.587 * g + 0.114 * b).toInt()
    }

    private fun decodeAndScale(filePath: String, size: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, this)
            var inSampleSize = 1
            if (outHeight > size || outWidth > size) {
                while (outHeight / inSampleSize >= size && outWidth / inSampleSize >= size) {
                    inSampleSize *= 2
                }
            }
            this.inSampleSize = inSampleSize
            inJustDecodeBounds = false
        }
        val bitmap = BitmapFactory.decodeFile(filePath, options) ?: return null
        return Bitmap.createScaledBitmap(bitmap, size, size, true)
    }
}
