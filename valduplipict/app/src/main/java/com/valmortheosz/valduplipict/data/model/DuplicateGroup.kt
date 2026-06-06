package com.valmortheosz.valduplipict.data.model

data class DuplicateGroup(
    val groupId: String,
    val files: List<ImageFile>,
    val similarityScore: Float, // 0.0 to 1.0 (e.g. 0.95 for 95%)
    val type: DuplicateType
) {
    val totalWastedSpace: Long
        get() {
            if (files.size <= 1) return 0L
            // Wasted space = sum of all file sizes minus the size of the largest file (or first file as reference)
            val sortedFiles = files.sortedByDescending { it.fileSize }
            var wasted = 0L
            for (i in 1 until sortedFiles.size) {
                wasted += sortedFiles[i].fileSize
            }
            return wasted
        }
}

enum class DuplicateType {
    EXACT_MATCH, // 100% same hash
    SIMILAR_VISUAL, // High similarity using pHash/SSIM
    SAME_NAME_DIFFERENT_LOCATION
}
