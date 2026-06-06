package com.valmortheosz.valduplipict.domain.usecase

import com.valmortheosz.valduplipict.data.model.DuplicateGroup
import com.valmortheosz.valduplipict.data.model.DuplicateType
import com.valmortheosz.valduplipict.data.model.ImageFile
import com.valmortheosz.valduplipict.data.repository.ScanRepository
import com.valmortheosz.valduplipict.domain.algorithm.HashEngine
import com.valmortheosz.valduplipict.domain.algorithm.HistogramComparator
import com.valmortheosz.valduplipict.domain.algorithm.SSIMCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ScanImagesUseCase @Inject constructor(
    private val scanRepository: ScanRepository,
    private val hashEngine: HashEngine,
    private val ssimCalculator: SSIMCalculator,
    private val histogramComparator: HistogramComparator
) {
    suspend operator fun invoke(
        similarityThreshold: Float = 0.90f,
        onProgress: suspend (state: String, processed: Int, total: Int, fileName: String, duplicates: Int, spaceSaved: Long) -> Unit = { _, _, _, _, _, _ -> }
    ): List<DuplicateGroup> = withContext(Dispatchers.Default) {
        val allImages = scanRepository.getAllImagesFromMediaStore()
        if (allImages.isEmpty()) return@withContext emptyList()

        val cachedImages = scanRepository.getCachedImages().associateBy { it.filePath }
        val processedImages = mutableListOf<ImageFile>()

        var progress = 0
        val total = allImages.size

        // Parallel processing of hashes if needed, but doing sequentially for now with progress
        for (img in allImages) {
            val cached = cachedImages[img.filePath]
            if (cached != null && cached.lastModified == img.lastModified && cached.fileSize == img.fileSize) {
                processedImages.add(cached)
            } else {
                val md5 = hashEngine.calculateMD5(img.filePath)
                val pHash = hashEngine.calculatePHash(img.filePath)
                val newImg = img.copy(md5Hash = md5, pHash = pHash)
                processedImages.add(newImg)
            }
            progress++
            if (progress % 10 == 0) onProgress("HASHING", progress, total, img.fileName, 0, 0L)
        }

        scanRepository.saveImagesToCache(processedImages)

        onProgress("FINALIZING", total, total, "Menganalisis duplikat...", 0, 0L)

        // Find duplicates
        val duplicateGroups = mutableListOf<DuplicateGroup>()
        val processedPaths = mutableSetOf<String>()

        for (i in processedImages.indices) {
            val img1 = processedImages[i]
            if (processedPaths.contains(img1.filePath)) continue

            val currentGroup = mutableListOf<ImageFile>()
            currentGroup.add(img1)
            var groupSimilarity = 1.0f
            var groupType = DuplicateType.EXACT_MATCH

            for (j in i + 1 until processedImages.size) {
                val img2 = processedImages[j]
                if (processedPaths.contains(img2.filePath)) continue

                // Stage 1: Exact match MD5
                if (img1.md5Hash != null && img1.md5Hash == img2.md5Hash) {
                    currentGroup.add(img2)
                    processedPaths.add(img2.filePath)
                    if (img1.fileName == img2.fileName && img1.folderName != img2.folderName) {
                        groupType = DuplicateType.SAME_NAME_DIFFERENT_LOCATION
                    }
                    continue
                }

                // Stage 2: Fast pre-filter (File size bucketing - ignore if difference > 15%)
                val sizeDiff = Math.abs(img1.fileSize - img2.fileSize).toDouble() / maxOf(img1.fileSize, img2.fileSize)
                if (sizeDiff > 0.15) continue

                // Stage 3: Perceptual Hash
                if (img1.pHash != null && img2.pHash != null) {
                    val hammingDistance = hashEngine.hammingDistance(img1.pHash, img2.pHash)
                    val pHashSimilarity = 1.0f - (hammingDistance / 64.0f)

                    if (pHashSimilarity >= similarityThreshold) {
                        // Stage 4: Structural Similarity (SSIM) Verification
                        val ssim = ssimCalculator.calculateSSIM(img1.filePath, img2.filePath)
                        if (ssim >= 0.85) {
                            // Optional Stage 5: Histogram Correlation
                            val histCorrelation = histogramComparator.compareHistograms(img1.filePath, img2.filePath)

                            val combinedScore = (pHashSimilarity * 0.4f) + (ssim.toFloat() * 0.4f) + (histCorrelation.toFloat() * 0.2f)

                            if (combinedScore >= similarityThreshold) {
                                currentGroup.add(img2)
                                processedPaths.add(img2.filePath)
                                groupSimilarity = minOf(groupSimilarity, combinedScore)
                                groupType = DuplicateType.SIMILAR_VISUAL
                            }
                        }
                    }
                }
            }

            if (currentGroup.size > 1) {
                processedPaths.add(img1.filePath)
                duplicateGroups.add(
                    DuplicateGroup(
                        groupId = UUID.randomUUID().toString(),
                        files = currentGroup,
                        similarityScore = groupSimilarity,
                        type = groupType
                    )
                )
            }
        }

        val duplicatesCount = duplicateGroups.size
        val spaceSaved = duplicateGroups.sumOf { it.totalWastedSpace }
        onProgress("COMPLETED", total, total, "Selesai", duplicatesCount, spaceSaved)
        return@withContext duplicateGroups
    }
}
