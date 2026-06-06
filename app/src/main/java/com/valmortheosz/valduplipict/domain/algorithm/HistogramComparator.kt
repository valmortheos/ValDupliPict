package com.valmortheosz.valduplipict.domain.algorithm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HistogramComparator @Inject constructor() {

    suspend fun compareHistograms(path1: String, path2: String): Double = withContext(Dispatchers.IO) {
        try {
            val size = 64
            val bmp1 = decodeAndScale(path1, size) ?: return@withContext 0.0
            val bmp2 = decodeAndScale(path2, size) ?: return@withContext 0.0

            val hist1 = calculateNormalizedHistogram(bmp1)
            val hist2 = calculateNormalizedHistogram(bmp2)

            bmp1.recycle()
            bmp2.recycle()

            return@withContext calculateCorrelation(hist1, hist2)
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }

    private fun calculateNormalizedHistogram(bmp: Bitmap): DoubleArray {
        val histogram = DoubleArray(256 * 3) // RGB separated
        val width = bmp.width
        val height = bmp.height
        val pixels = IntArray(width * height)
        bmp.getPixels(pixels, 0, width, 0, 0, width, height)

        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            histogram[r]++
            histogram[256 + g]++
            histogram[512 + b]++
        }

        val totalPixels = (width * height).toDouble()
        for (i in histogram.indices) {
            histogram[i] /= totalPixels
        }
        return histogram
    }

    private fun calculateCorrelation(h1: DoubleArray, h2: DoubleArray): Double {
        var mean1 = 0.0
        var mean2 = 0.0
        for (i in h1.indices) {
            mean1 += h1[i]
            mean2 += h2[i]
        }
        mean1 /= h1.size
        mean2 /= h2.size

        var num = 0.0
        var den1 = 0.0
        var den2 = 0.0

        for (i in h1.indices) {
            val d1 = h1[i] - mean1
            val d2 = h2[i] - mean2
            num += d1 * d2
            den1 += d1 * d1
            den2 += d2 * d2
        }

        val den = Math.sqrt(den1 * den2)
        return if (den == 0.0) 0.0 else num / den
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
