package com.valmortheosz.valduplipict.domain.algorithm

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

class HashEngine @Inject constructor() {

    suspend fun calculateMD5(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) return@withContext null
            val md = MessageDigest.getInstance("MD5")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }
            return@withContext md.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun calculatedHash(filePath: String): Long? = withContext(Dispatchers.IO) {
        try {
            val bitmap = decodeSampledBitmap(filePath, 9, 8) ?: return@withContext null
            var hash = 0L
            val pixels = IntArray(9 * 8)
            bitmap.getPixels(pixels, 0, 9, 0, 0, 9, 8)

            for (y in 0 until 8) {
                for (x in 0 until 8) {
                    val leftPixel = getLuminance(pixels[y * 9 + x])
                    val rightPixel = getLuminance(pixels[y * 9 + x + 1])
                    hash = hash shl 1
                    if (leftPixel > rightPixel) {
                        hash = hash or 1L
                    }
                }
            }
            bitmap.recycle()
            return@withContext hash
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun calculateAHash(filePath: String): Long? = withContext(Dispatchers.IO) {
        try {
            val bitmap = decodeSampledBitmap(filePath, 8, 8) ?: return@withContext null
            var hash = 0L
            val pixels = IntArray(8 * 8)
            bitmap.getPixels(pixels, 0, 8, 0, 0, 8, 8)

            var sum = 0
            val luminances = IntArray(64)
            for (i in 0 until 64) {
                luminances[i] = getLuminance(pixels[i])
                sum += luminances[i]
            }
            val average = sum / 64

            for (i in 0 until 64) {
                hash = hash shl 1
                if (luminances[i] >= average) {
                    hash = hash or 1L
                }
            }
            bitmap.recycle()
            return@withContext hash
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun calculatePHash(filePath: String): Long? = withContext(Dispatchers.IO) {
        // Simplified pHash for performance: typically involves DCT.
        // For a true offline native implementation without OpenCV, a lightweight DCT on 32x32 is complex but doable.
        // For this task, we will implement a basic version of DCT-based pHash.
        try {
            val size = 32
            val bitmap = decodeSampledBitmap(filePath, size, size) ?: return@withContext null
            val pixels = IntArray(size * size)
            bitmap.getPixels(pixels, 0, size, 0, 0, size, size)

            val vals = DoubleArray(size * size)
            for (i in 0 until size * size) {
                vals[i] = getLuminance(pixels[i]).toDouble()
            }
            bitmap.recycle()

            val dctVals = applyDCT(vals, size)

            // Take the top-left 8x8 block (low frequencies), excluding the very first term (0,0) which is DC
            var total = 0.0
            for (x in 0 until 8) {
                for (y in 0 until 8) {
                    if (x == 0 && y == 0) continue
                    total += dctVals[x][y]
                }
            }
            val avg = total / 63.0 // 64 - 1

            var hash = 0L
            for (x in 0 until 8) {
                for (y in 0 until 8) {
                    if (x == 0 && y == 0) continue
                    hash = hash shl 1
                    if (dctVals[x][y] > avg) {
                        hash = hash or 1L
                    }
                }
            }
            return@withContext hash

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun applyDCT(f: DoubleArray, N: Int): Array<DoubleArray> {
        val F = Array(N) { DoubleArray(N) }
        // We only need the 8x8 top-left part for pHash, so optimize
        for (u in 0 until 8) {
            for (v in 0 until 8) {
                var sum = 0.0
                for (i in 0 until N) {
                    for (j in 0 until N) {
                        sum += f[i * N + j] *
                               Math.cos((2 * i + 1) * u * Math.PI / (2.0 * N)) *
                               Math.cos((2 * j + 1) * v * Math.PI / (2.0 * N))
                    }
                }
                val cu = if (u == 0) 1.0 / Math.sqrt(2.0) else 1.0
                val cv = if (v == 0) 1.0 / Math.sqrt(2.0) else 1.0
                F[u][v] = 0.25 * cu * cv * sum
            }
        }
        return F
    }

    fun hammingDistance(hash1: Long, hash2: Long): Int {
        var x = hash1 xor hash2
        var setBits = 0
        while (x != 0L) {
            setBits += (x and 1L).toInt()
            x = x ushr 1
        }
        return setBits
    }

    private fun decodeSampledBitmap(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, this)
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }
        val bitmap = BitmapFactory.decodeFile(filePath, options) ?: return null
        return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getLuminance(pixel: Int): Int {
        val red = Color.red(pixel)
        val green = Color.green(pixel)
        val blue = Color.blue(pixel)
        // Standard perceptual weight
        return (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
    }
}
