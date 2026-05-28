App({
  onLaunch() {
    if (!wx.cloud) {
      console.error('请使用 2.2.3 或以上的基础库以使用云能力')
      return
    }
    wx.cloud.init({
      env: 'your-env-id',
      traceUser: true
    })
    this.globalData.db = wx.cloud.database()
    this.login()
  },

  async login() {
    try {
      const res = await wx.cloud.callFunction({ name: 'login' })
      if (res.result && res.result.user) {
        this.globalData.userInfo = res.result.user
        this.globalData.userId = res.result.user._openid
      }
    } catch (err) {
      console.error('登录失败:', err)
    }
  },

  globalData: {
    userInfo: null,
    userId: null,
    db: null
  }
})
