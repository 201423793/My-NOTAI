const api = require('../../utils/api')

Page({
  data: {
    historyId: '',
    originalFileID: '',
    resultFileID: '',
    originalUrl: '',
    resultUrl: '',
    platform: '',
    processedAt: '',
    isFavorited: false,
    sliderPosition: 50,
    canvasWidth: 0,
    canvasHeight: 0
  },

  onLoad(options) {
    if (options.historyId) {
      this.setData({ historyId: options.historyId })
      this.loadResult(options.historyId)
    }
  },

  async loadResult(historyId) {
    wx.showLoading({ title: '加载中...' })
    try {
      const db = wx.cloud.database()
      const res = await db.collection('history').doc(historyId).get()
      const data = res.data

      const [originalRes, resultRes] = await Promise.all([
        api.getTempFileURL(data.originalFileID),
        api.getTempFileURL(data.resultFileID)
      ])

      this.setData({
        originalFileID: data.originalFileID,
        resultFileID: data.resultFileID,
        originalUrl: originalRes,
        resultUrl: resultRes,
        platform: data.platform,
        processedAt: data.processedAt,
        isFavorited: data.isFavorited || false
      })
    } catch (err) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  onSliderChange(e) {
    this.setData({ sliderPosition: e.detail.value })
  },

  onTouchMove(e) {
    const touch = e.touches[0]
    const query = wx.createSelectorQuery()
    query.select('.comparison-container').boundingClientRect(rect => {
      if (rect) {
        const position = ((touch.clientX - rect.left) / rect.width) * 100
        this.setData({
          sliderPosition: Math.max(0, Math.min(100, position))
        })
      }
    }).exec()
  },

  async saveToAlbum() {
    if (!this.data.resultUrl) return
    try {
      wx.showLoading({ title: '保存中...' })
      const downloadRes = await wx.cloud.downloadFile({ fileID: this.data.resultFileID })
      await wx.saveImageToPhotosAlbum({ filePath: downloadRes.tempFilePath })
      wx.showToast({ title: '已保存到相册', icon: 'success' })
    } catch (err) {
      if (err.errMsg && err.errMsg.includes('auth deny')) {
        wx.showModal({
          title: '需要权限',
          content: '请在设置中允许保存图片到相册',
          success(res) {
            if (res.confirm) wx.openSetting()
          }
        })
      } else {
        wx.showToast({ title: '保存失败', icon: 'none' })
      }
    } finally {
      wx.hideLoading()
    }
  },

  async toggleFavorite() {
    try {
      const res = await api.toggleFavorite(this.data.historyId)
      this.setData({ isFavorited: res.result.favorited })
      wx.showToast({
        title: this.data.isFavorited ? '已收藏' : '已取消收藏',
        icon: 'success'
      })
    } catch (err) {
      wx.showToast({ title: '操作失败', icon: 'none' })
    }
  },

  onShareAppMessage() {
    return {
      title: 'AI去水印 - 一键去除AI生成图片水印',
      path: '/pages/index/index'
    }
  },

  processAnother() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  previewOriginal() {
    if (this.data.originalUrl) {
      wx.previewImage({ urls: [this.data.originalUrl] })
    }
  },

  previewResult() {
    if (this.data.resultUrl) {
      wx.previewImage({ urls: [this.data.resultUrl] })
    }
  }
})
