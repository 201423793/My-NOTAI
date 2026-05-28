const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })

const PLATFORM_PROFILES = {
  midjourney: {
    sizes: [[1024, 1024], [1456, 816], [816, 1456], [1344, 768], [768, 1344]],
    keywords: ['midjourney'],
    defaultRegion: { x: 0, y: 0.85, w: 1, h: 0.15 }
  },
  dalle: {
    sizes: [[1024, 1024], [1024, 1792], [1792, 1024]],
    keywords: ['openai', 'dall-e', 'dalle', 'c2pa'],
    defaultRegion: { x: 0, y: 0.85, w: 1, h: 0.15 }
  },
  'stable-diffusion': {
    sizes: [[512, 512], [768, 768], [512, 768], [768, 512]],
    keywords: ['stable-diffusion', 'stable_diffusion', 'comfyui', 'automatic1111'],
    defaultRegion: { x: 0, y: 0.85, w: 1, h: 0.15 }
  },
  tongyi: {
    sizes: [[1024, 1024], [768, 1024], [1024, 768]],
    keywords: ['alibaba', 'tongyi', 'wanx'],
    defaultRegion: { x: 0.7, y: 0.85, w: 0.3, h: 0.15 }
  },
  wenyi: {
    sizes: [[1024, 1024], [512, 512], [768, 768]],
    keywords: ['baidu', 'wenxin', 'wenyi'],
    defaultRegion: { x: 0.7, y: 0.85, w: 0.3, h: 0.15 }
  }
}

function detectBySize(width, height) {
  const results = []
  for (const [platform, profile] of Object.entries(PLATFORM_PROFILES)) {
    for (const [sw, sh] of profile.sizes) {
      if (width === sw && height === sh) {
        results.push({ platform, confidence: 0.3, method: 'dimensions' })
      }
    }
  }
  return results
}

function detectByMetadata(metadata) {
  if (!metadata) return []
  const lower = metadata.toLowerCase()
  const results = []
  for (const [platform, profile] of Object.entries(PLATFORM_PROFILES)) {
    for (const keyword of profile.keywords) {
      if (lower.includes(keyword)) {
        results.push({ platform, confidence: 0.95, method: 'metadata' })
        break
      }
    }
  }
  return results
}

function combineResults(sizeResults, metadataResults) {
  const scores = {}
  const methods = {}

  for (const r of sizeResults) {
    scores[r.platform] = (scores[r.platform] || 0) + r.confidence
    methods[r.platform] = r.method
  }

  for (const r of metadataResults) {
    scores[r.platform] = (scores[r.platform] || 0) + r.confidence
    methods[r.platform] = r.method
  }

  let best = null
  let bestScore = 0
  for (const [platform, score] of Object.entries(scores)) {
    if (score > bestScore) {
      best = platform
      bestScore = score
    }
  }

  return {
    platform: best || 'unknown',
    confidence: Math.min(bestScore, 1),
    detectionMethod: methods[best] || 'fallback',
    watermarkRegion: PLATFORM_PROFILES[best]?.defaultRegion || { x: 0, y: 0.85, w: 1, h: 0.15 }
  }
}

exports.main = async (event, context) => {
  const { fileID } = event

  if (!fileID) {
    return { success: false, error: '缺少文件ID' }
  }

  try {
    const fileRes = await cloud.getTempFileURL({ fileList: [fileID] })
    const tempURL = fileRes.fileList[0].tempFileURL

    const sizeResults = detectBySize(event.width || 0, event.height || 0)
    const metadataResults = detectByMetadata(event.metadata || '')

    const result = combineResults(sizeResults, metadataResults)

    return {
      success: true,
      platform: result.platform,
      confidence: result.confidence,
      detectionMethod: result.detectionMethod,
      watermarkRegion: result.watermarkRegion
    }
  } catch (err) {
    console.error('detectWatermark error:', err)
    return {
      success: true,
      platform: 'unknown',
      confidence: 0,
      detectionMethod: 'fallback',
      watermarkRegion: { x: 0, y: 0.85, w: 1, h: 0.15 }
    }
  }
}
