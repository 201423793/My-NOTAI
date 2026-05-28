package com.notai.app.domain.model

data class WatermarkRegion(
    val x: Float, val y: Float, val w: Float, val h: Float
)

data class DetectionResult(
    val platform: Platform,
    val confidence: Float,
    val detectionMethod: String,
    val watermarkRegion: WatermarkRegion
)

data class ProcessingResult(
    val historyId: Long,
    val resultFile: java.io.File,
    val processingTimeMs: Long
)

enum class Platform(
    val displayName: String,
    val sizes: List<Pair<Int, Int>>,
    val metadataKeywords: List<String>,
    val defaultRegion: WatermarkRegion,
    val watermarkType: String
) {
    MIDJOURNEY(
        "Midjourney",
        listOf(1024 to 1024, 1456 to 816, 816 to 1456, 1344 to 768, 768 to 1344),
        listOf("midjourney"),
        WatermarkRegion(0f, 0.85f, 1f, 0.15f),
        "pattern"
    ),
    DALLE(
        "DALL-E",
        listOf(1024 to 1024, 1024 to 1792, 1792 to 1024),
        listOf("openai", "dall-e", "dalle", "c2pa"),
        WatermarkRegion(0f, 0.85f, 1f, 0.15f),
        "metadata"
    ),
    STABLE_DIFFUSION(
        "Stable Diffusion",
        listOf(512 to 512, 768 to 768, 512 to 768, 768 to 512),
        listOf("stable-diffusion", "stable_diffusion", "comfyui", "automatic1111"),
        WatermarkRegion(0f, 0.85f, 1f, 0.15f),
        "none"
    ),
    TONGYI(
        "通义万相",
        listOf(1024 to 1024, 768 to 1024, 1024 to 768),
        listOf("alibaba", "tongyi", "wanx"),
        WatermarkRegion(0.7f, 0.85f, 0.3f, 0.15f),
        "text"
    ),
    WENYI(
        "文心一格",
        listOf(1024 to 1024, 512 to 512, 768 to 768),
        listOf("baidu", "wenxin", "wenyi"),
        WatermarkRegion(0.7f, 0.85f, 0.3f, 0.15f),
        "text"
    ),
    UNKNOWN(
        "未知平台",
        emptyList(),
        emptyList(),
        WatermarkRegion(0f, 0.85f, 1f, 0.15f),
        "unknown"
    );

    companion object {
        fun fromId(id: String): Platform =
            entries.find { it.name.equals(id, ignoreCase = true) } ?: UNKNOWN
    }
}
