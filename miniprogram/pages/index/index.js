const api = require('../../utils/api')
const auth = require('../../utils/auth')
const imageUtil = require('../../utils/image')
const { getAllPlatforms } = require('../../utils/platform')

Page({
  data: {
    imageFilePath: '',
    imageFileID: '',
    imageInfo: null,
    detectedPlatform: null,
    platformConfidence: 0,
    platforms: [],
    selectedPlatform: '',
    isDetecting: false,
    isUploading: false,
    freeCredits: 0,
    canProcess: false
  },

  onLoad() {
    this.setData({ platforms: getAllPlatforms() })
  },

  async onShow() {
    try {
      await auth.ensureLogin()
      const userInfo = auth.getUserInfo()
      if (userInfo) {
        this.setData({ freeCredits: userInfo.freeCredits || 0 })
      }
    } catch (err) {
      console.error('初始化失败:', err)
    }
  },

  async chooseImage() {
    try {
      const res = await wx.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['album', 'camera'],
        sizeType: ['compressed']
      })

      const tempFilePath = res.tempFiles[0].tempFilePath
      this.setData({ isUploading: true })

      wx.showLoading({ title: '正在处理图片...' })

      const compressedPath = await imageUtil.compressImage(tempFilePath)
      const info = await imageUtil.validateImage(compressedPath)

      this.setData({
        imageFilePath: compressedPath,
        imageInfo: info
      })

      wx.hideLoading()

      const fileID = await api.uploadImage(compressedPath)
      this.setData({
        imageFileID: fileID,
        isUploading: false,
        canProcess: true
      })

      this.detectWatermark(fileID)
    } catch (err) {
      wx.hideLoading()
      this.setData({ isUploading: false })
      if (err.errMsg && err.errMsg.includes('chooseMedia:fail cancel')) return
      wx.showToast({ title: err.message || '选择图片失败', icon: 'none' })
    }
  },

  async detectWatermark(fileID) {
    this.setData({ isDetecting: true })
    try {
      const res = await api.detectWatermark(fileID)
      if (res.result) {
        this.setData({
          detectedPlatform: res.result.platform,
          platformConfidence: res.result.confidence,
          selectedPlatform: res.result.platform
        })
      }
    } catch (err) {
      console.error('检测水印失败:', err)
    } finally {
      this.setData({ isDetecting: false })
    }
  },

  onPlatformChange(e) {
    const platform = e.detail.value
    this.setData({ selectedPlatform: platform })
  },

  removeWatermark() {
    if (!this.data.imageFileID || !this.data.selectedPlatform) {
      wx.showToast({ title: '请先选择图片', icon: 'none' })
      return
    }
    if (this.data.freeCredits <= 0) {
      wx.showModal({
        title: '额度不足',
        content: '免费额度已用完，请升级会员',
        showCancel: false
      })
      return
    }

    wx.navigateTo({
      url: `/pages/processing/processing?fileID=${this.data.imageFileID}&platform=${this.data.selectedPlatform}`
    })
  },

  previewImage() {
    if (this.data.imageFilePath) {
      wx.previewImage({ urls: [this.data.imageFilePath] })
    }
  }
})
