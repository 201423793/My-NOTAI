const PLATFORMS = {
  midjourney: {
    name: 'Midjourney',
    icon: '/images/platforms/midjourney.png',
    sizes: [[1024, 1024], [1456, 816], [816, 1456], [1344, 768], [768, 1344]],
    watermark: {
      type: 'pattern',
      location: 'full',
      description: '隐式周期性纹理水印'
    },
    metadata: ['midjourney']
  },
  dalle: {
    name: 'DALL-E',
    icon: '/images/platforms/dalle.png',
    sizes: [[1024, 1024], [1024, 1792], [1792, 1024]],
    watermark: {
      type: 'metadata',
      location: 'c2pa',
      description: 'C2PA 元数据水印'
    },
    metadata: ['openai', 'dall-e', 'dalle', 'c2pa']
  },
  'stable-diffusion': {
    name: 'Stable Diffusion',
    icon: '/images/platforms/stable-diffusion.png',
    sizes: [[512, 512], [768, 768], [512, 768], [768, 512]],
    watermark: {
      type: 'none',
      location: null,
      description: '无内置水印（检查叠加水印）'
    },
    metadata: ['stable-diffusion', 'stable_diffusion', 'comfyui', 'automatic1111']
  },
  tongyi: {
    name: '通义万相',
    icon: '/images/platforms/tongyi.png',
    sizes: [[1024, 1024], [768, 1024], [1024, 768]],
    watermark: {
      type: 'text',
      location: 'bottom-right',
      description: '右下角文字/logo水印'
    },
    metadata: ['alibaba', 'tongyi', 'wanx']
  },
  wenyi: {
    name: '文心一格',
    icon: '/images/platforms/wenyi.png',
    sizes: [[1024, 1024], [512, 512], [768, 768]],
    watermark: {
      type: 'text',
      location: 'bottom-right',
      description: '"文心一格"文字水印'
    },
    metadata: ['baidu', 'wenxin', 'wenyi']
  }
}

function getPlatformInfo(platform) {
  return PLATFORMS[platform] || null
}

function getAllPlatforms() {
  return Object.keys(PLATFORMS).map(key => ({
    id: key,
    ...PLATFORMS[key]
  }))
}

function matchPlatformBySize(width, height) {
  const results = []
  for (const [id, platform] of Object.entries(PLATFORMS)) {
    for (const [sw, sh] of platform.sizes) {
      if (width === sw && height === sh) {
        results.push({ id, confidence: 0.3 })
      }
    }
  }
  return results
}

function matchPlatformByMetadata(metadataStr) {
  const lower = metadataStr.toLowerCase()
  const results = []
  for (const [id, platform] of Object.entries(PLATFORMS)) {
    for (const keyword of platform.metadata) {
      if (lower.includes(keyword)) {
        results.push({ id, confidence: 0.95, keyword })
      }
    }
  }
  return results
}

module.exports = {
  PLATFORMS,
  getPlatformInfo,
  getAllPlatforms,
  matchPlatformBySize,
  matchPlatformByMetadata
}
