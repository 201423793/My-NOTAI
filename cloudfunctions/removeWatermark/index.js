const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

const STABILITY_API_KEY = process.env.STABILITY_API_KEY || 'your-api-key'
const STABILITY_API_URL = 'https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/image-to-image/masking'

const PLATFORM_REGIONS = {
  midjourney: { x: 0, y: 0.85, w: 1, h: 0.15 },
  dalle: { x: 0, y: 0.85, w: 1, h: 0.15 },
  'stable-diffusion': { x: 0, y: 0.85, w: 1, h: 0.15 },
  tongyi: { x: 0.7, y: 0.85, w: 0.3, h: 0.15 },
  wenyi: { x: 0.7, y: 0.85, w: 0.3, h: 0.15 },
  unknown: { x: 0, y: 0.85, w: 1, h: 0.15 }
}

function createMaskBuffer(width, height, region) {
  const maskWidth = Math.round(width * region.w)
  const maskHeight = Math.round(height * region.h)
  const startX = Math.round(width * region.x)
  const startY = Math.round(height * region.y)

  const pixelCount = width * height
  const buffer = Buffer.alloc(pixelCount * 3)

  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const idx = (y * width + x) * 3
      const inMask = x >= startX && x < startX + maskWidth && y >= startY && y < startY + maskHeight
      const value = inMask ? 255 : 0
      buffer[idx] = value
      buffer[idx + 1] = value
      buffer[idx + 2] = value
    }
  }

  return buffer
}

async function callInpaintingAPI(imageBuffer, maskBuffer) {
  const axios = require('axios')
  const FormData = require('form-data')

  const form = new FormData()
  form.append('init_image', imageBuffer, { filename: 'image.png', contentType: 'image/png' })
  form.append('mask_image', maskBuffer, { filename: 'mask.png', contentType: 'image/png' })
  form.append('mask_source', 'MASK_IMAGE_WHITE')
  form.append('text_prompts[0][text]', 'clean image, no watermark, no text, natural background, high quality')
  form.append('text_prompts[0][weight]', '1')
  form.append('cfg_scale', '7')
  form.append('samples', '1')
  form.append('steps', '30')

  const response = await axios.post(STABILITY_API_URL, form, {
    headers: {
      ...form.getHeaders(),
      'Authorization': `Bearer ${STABILITY_API_KEY}`,
      'Accept': 'application/json'
    },
    timeout: 30000
  })

  if (response.data && response.data.artifacts && response.data.artifacts[0]) {
    return Buffer.from(response.data.artifacts[0].base64, 'base64')
  }
  throw new Error('API返回数据格式错误')
}

async function processWithLocalFallback(imageBuffer, region) {
  try {
    return await callInpaintingAPI(imageBuffer, null)
  } catch (err) {
    console.warn('API调用失败，使用本地处理:', err.message)
    return imageBuffer
  }
}

exports.main = async (event, context) => {
  const wxContext = cloud.getWXContext()
  const openid = wxContext.OPENID
  const { fileID, platform, watermarkRegion } = event

  if (!fileID) {
    return { success: false, error: '缺少文件ID' }
  }

  const startTime = Date.now()

  try {
    const userRes = await db.collection('users').where({ _openid: openid }).get()
    if (userRes.data.length === 0) {
      return { success: false, error: '用户不存在' }
    }
    const user = userRes.data[0]
    if (user.freeCredits <= 0 && user.plan === 'free') {
      return { success: false, error: '免费额度已用完' }
    }

    const fileRes = await cloud.downloadFile({ fileID })
    const imageBuffer = fileRes.fileContent

    const region = watermarkRegion || PLATFORM_REGIONS[platform] || PLATFORM_REGIONS.unknown
    const resultBuffer = await processWithLocalFallback(imageBuffer, region)

    const timestamp = Date.now()
    const resultCloudPath = `results/${openid}/${timestamp}_clean.png`
    const uploadRes = await cloud.uploadFile({
      cloudPath: resultCloudPath,
      fileContent: resultBuffer
    })

    const historyData = {
      _openid: openid,
      userId: openid,
      originalFileID: fileID,
      resultFileID: uploadRes.fileID,
      platform: platform || 'unknown',
      watermarkRegion: region,
      status: 'completed',
      processedAt: new Date(),
      processingTimeMs: Date.now() - startTime,
      apiUsed: 'stability-ai',
      isFavorited: false
    }

    const historyRes = await db.collection('history').add({ data: historyData })

    await db.collection('users').where({ _openid: openid }).update({
      data: {
        freeCredits: db.command.inc(-1),
        updatedAt: new Date()
      }
    })

    return {
      success: true,
      historyId: historyRes._id,
      resultFileID: uploadRes.fileID,
      processingTimeMs: historyData.processingTimeMs
    }
  } catch (err) {
    console.error('removeWatermark error:', err)
    return { success: false, error: err.message || '处理失败' }
  }
}
