const { IMAGE_LIMITS } = require('./constants')

async function compressImage(filePath) {
  try {
    const res = await wx.compressImage({
      src: filePath,
      quality: IMAGE_LIMITS.COMPRESS_QUALITY
    })
    return res.tempFilePath
  } catch (err) {
    console.warn('压缩失败，使用原图:', err)
    return filePath
  }
}

async function getImageInfo(filePath) {
  return new Promise((resolve, reject) => {
    wx.getImageInfo({
      src: filePath,
      success: resolve,
      fail: reject
    })
  })
}

async function validateImage(filePath) {
  const info = await getImageInfo(filePath)

  if (info.width < IMAGE_LIMITS.MIN_WIDTH || info.height < IMAGE_LIMITS.MIN_HEIGHT) {
    throw new Error(`图片尺寸太小，最小 ${IMAGE_LIMITS.MIN_WIDTH}x${IMAGE_LIMITS.MIN_HEIGHT}`)
  }

  return {
    width: info.width,
    height: info.height,
    type: info.type,
    path: filePath
  }
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

module.exports = {
  compressImage,
  getImageInfo,
  validateImage,
  formatFileSize
}
