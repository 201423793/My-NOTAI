const { getPlatformInfo } = require('../../utils/platform')

Component({
  properties: {
    item: { type: Object, value: {} }
  },

  data: {
    platformInfo: null,
    dateText: ''
  },

  observers: {
    'item': function(item) {
      if (item && item.platform) {
        this.setData({
          platformInfo: getPlatformInfo(item.platform)
        })
      }
      if (item && item.processedAt) {
        this.setData({ dateText: this.formatDate(item.processedAt) })
      }
    }
  },

  methods: {
    formatDate(dateStr) {
      const date = new Date(dateStr)
      const now = new Date()
      const diff = now - date
      if (diff < 60000) return '刚刚'
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
      const month = date.getMonth() + 1
      const day = date.getDate()
      return `${month}月${day}日`
    },

    onView() {
      this.triggerEvent('view', { id: this.data.item._id })
    },

    onDelete() {
      wx.showModal({
        title: '确认删除',
        content: '确定要删除这条记录吗？',
        success: (res) => {
          if (res.confirm) {
            this.triggerEvent('delete', { id: this.data.item._id })
          }
        }
      })
    }
  }
})
