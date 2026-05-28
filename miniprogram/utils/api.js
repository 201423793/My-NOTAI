function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

async function callWithRetry(fnName, data, maxRetries = 2) {
  for (let i = 0; i <= maxRetries; i++) {
    try {
      return await wx.cloud.callFunction({ name: fnName, data })
    } catch (err) {
      if (i === maxRetries) throw err
      await sleep(1000 * (i + 1))
    }
  }
}

function login() {
  return callWithRetry('login')
}

function detectWatermark(fileID) {
  return callWithRetry('detectWatermark', { fileID })
}

function removeWatermark(fileID, platform, watermarkRegion) {
  return callWithRetry('removeWatermark', { fileID, platform, watermarkRegion })
}

function getHistory(skip = 0, limit = 20) {
  return callWithRetry('getHistory', { skip, limit })
}

function toggleFavorite(historyId) {
  return callWithRetry('toggleFavorite', { historyId })
}

function getFavorites(skip = 0, limit = 20) {
  return callWithRetry('getFavorites', { skip, limit })
}

async function uploadImage(filePath) {
  const cloudPath = `uploads/${Date.now()}_${Math.random().toString(36).slice(2)}.png`
  const res = await wx.cloud.uploadFile({
    cloudPath,
    filePath
  })
  return res.fileID
}

async function getTempFileURL(fileID) {
  const res = await wx.cloud.getTempFileURL({ fileList: [fileID] })
  if (res.fileList && res.fileList[0]) {
    return res.fileList[0].tempFileURL
  }
  throw new Error('获取文件URL失败')
}

module.exports = {
  callWithRetry,
  login,
  detectWatermark,
  removeWatermark,
  getHistory,
  toggleFavorite,
  getFavorites,
  uploadImage,
  getTempFileURL
}
