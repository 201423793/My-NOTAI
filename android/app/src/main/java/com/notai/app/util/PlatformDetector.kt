package com.notai.app.util

import com.notai.app.domain.model.DetectionResult
import com.notai.app.domain.model.Platform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformDetector @Inject constructor() {

    private data class Hit(val platform: Platform, val confidence: Float, val method: String)

    fun detect(width: Int, height: Int, metadata: String): DetectionResult {
        val sizeHits = detectBySize(width, height)
        val metaHits = detectByMetadata(metadata)
        return combineResults(sizeHits, metaHits)
    }

    private fun detectBySize(width: Int, height: Int): List<Hit> =
        Platform.entries.filter { it != Platform.UNKNOWN }.flatMap { platform ->
            platform.sizes.filter { (sw, sh) -> sw == width && sh == height }
                .map { Hit(platform, 0.3f, "dimensions") }
        }

    private fun detectByMetadata(metadata: String): List<Hit> {
        val lower = metadata.lowercase()
        return Platform.entries.filter { it != Platform.UNKNOWN }.flatMap { platform ->
            platform.metadataKeywords.filter { lower.contains(it) }
                .map { Hit(platform, 0.95f, "metadata") }
        }
    }

    private fun combineResults(sizeHits: List<Hit>, metaHits: List<Hit>): DetectionResult {
        val scores = mutableMapOf<Platform, Float>()
        val methods = mutableMapOf<Platform, String>()

        (sizeHits + metaHits).forEach { hit ->
            scores[hit.platform] = (scores[hit.platform] ?: 0f) + hit.confidence
            methods[hit.platform] = hit.method
        }

        val best = scores.maxByOrNull { it.value }
        val platform = best?.key ?: Platform.UNKNOWN
        val confidence = best?.value?.coerceAtMost(1f) ?: 0f

        return DetectionResult(
            platform = platform,
            confidence = confidence,
            detectionMethod = methods[platform] ?: "fallback",
            watermarkRegion = platform.defaultRegion
        )
    }
}
