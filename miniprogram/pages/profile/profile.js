const auth = require('../../utils/auth')

Page({
  data: {
    userInfo: null,
    processingCount: 0,
    freeCredits: 0,
    favoritesCount: 0,
    isLoading: true
  },

  async onShow() {
    await this.loadUserInfo()
  },

  async loadUserInfo() {
    this.setData({ isLoading: true })
    try {
      await auth.ensureLogin()
      const userInfo = auth.getUserInfo()
      if (userInfo) {
        this.setData({
          userInfo,
          processingCount: userInfo.processingCount || 0,
          freeCredits: userInfo.freeCredits || 0
        })
      }
      await this.loadFavoritesCount()
    } catch (err) {
      console.error('加载用户信息失败:', err)
    } finally {
      this.setData({ isLoading: false })
    }
  },

  async loadFavoritesCount() {
    try {
      const db = wx.cloud.database()
      const countRes = await db.collection('favorites').count()
      this.setData({ favoritesCount: countRes.total || 0 })
    } catch (err) {
      console.error('加载收藏数失败:', err)
    }
  },

  goToFavorites() {
    wx.navigateTo({ url: '/pages/history/history?filter=favorites' })
  },

  clearHistory() {
    wx.showModal({
      title: '确认清除',
      content: '确定要清除所有处理记录吗？此操作不可撤销。',
      success: async (res) => {
        if (res.confirm) {
          try {
            wx.showLoading({ title: '清除中...' })
            const db = wx.cloud.database()
            const { data } = await db.collection('history').where({
              _openid: '{openid}'
            }).get()
            for (const item of data) {
              await db.collection('history').doc(item._id).remove()
            }
            wx.showToast({ title: '已清除', icon: 'success' })
          } catch (err) {
            wx.showToast({ title: '清除失败', icon: 'none' })
          } finally {
            wx.hideLoading()
          }
        }
      }
    })
  },

  showAbout() {
    wx.showModal({
      title: '关于 AI去水印',
      content: '版本 1.0.0\n\n支持 Midjourney、DALL-E、Stable Diffusion、通义万相、文心一格 等主流AI平台的水印去除。',
      showCancel: false
    })
  }
})
