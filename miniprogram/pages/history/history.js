const api = require('../../utils/api')
const { PAGINATION } = require('../../utils/constants')

Page({
  data: {
    items: [],
    skip: 0,
    hasMore: true,
    isLoading: false,
    isEmpty: false
  },

  onLoad() {
    this.loadHistory(true)
  },

  onShow() {
    this.loadHistory(true)
  },

  async loadHistory(refresh = false) {
    if (this.data.isLoading) return
    if (!refresh && !this.data.hasMore) return

    this.setData({ isLoading: true })

    try {
      const skip = refresh ? 0 : this.data.skip
      const res = await api.getHistory(skip, PAGINATION.PAGE_SIZE)

      if (res.result) {
        const newItems = res.result.items || []
        const items = refresh ? newItems : [...this.data.items, ...newItems]

        this.setData({
          items,
          skip: skip + newItems.length,
          hasMore: newItems.length >= PAGINATION.PAGE_SIZE,
          isEmpty: items.length === 0
        })
      }
    } catch (err) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      this.setData({ isLoading: false })
      if (refresh) wx.stopPullDownRefresh()
    }
  },

  onPullDownRefresh() {
    this.loadHistory(true)
  },

  onReachBottom() {
    this.loadHistory(false)
  },

  viewResult(e) {
    const historyId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/result/result?historyId=${historyId}`
    })
  },

  async deleteItem(e) {
    const historyId = e.currentTarget.dataset.id
    try {
      const db = wx.cloud.database()
      await db.collection('history').doc(historyId).remove()
      const items = this.data.items.filter(item => item._id !== historyId)
      this.setData({
        items,
        isEmpty: items.length === 0
      })
      wx.showToast({ title: '已删除', icon: 'success' })
    } catch (err) {
      wx.showToast({ title: '删除失败', icon: 'none' })
    }
  }
})
