const api = require('../../utils/api')

Page({
  data: {
    fileID: '',
    platform: '',
    statusText: '正在上传图片...',
    progress: 0,
    isCancelling: false
  },

  onLoad(options) {
    this.setData({
      fileID: options.fileID || '',
      platform: options.platform || ''
    })
    this.startProcessing()
  },

  async startProcessing() {
    const statusMessages = [
      '正在上传图片...',
      '正在分析水印...',
      '正在去除水印...',
      '即将完成...'
    ]

    try {
      this.setData({ statusText: statusMessages[0], progress: 10 })

      await new Promise(resolve => setTimeout(resolve, 500))
      this.setData({ statusText: statusMessages[1], progress: 30 })

      const res = await api.removeWatermark(
        this.data.fileID,
        this.data.platform,
        null
      )

      this.setData({ statusText: statusMessages[2], progress: 60 })

      await new Promise(resolve => setTimeout(resolve, 500))
      this.setData({ statusText: statusMessages[3], progress: 90 })

      if (res.result && res.result.historyId) {
        this.setData({ progress: 100 })
        wx.redirectTo({
          url: `/pages/result/result?historyId=${res.result.historyId}`
        })
      } else {
        throw new Error('处理失败，请重试')
      }
    } catch (err) {
      wx.showToast({
        title: err.message || '处理失败',
        icon: 'none',
        duration: 2000
      })
      setTimeout(() => {
        wx.navigateBack()
      }, 2000)
    }
  },

  onCancel() {
    this.setData({ isCancelling: true })
    wx.navigateBack()
  }
})
