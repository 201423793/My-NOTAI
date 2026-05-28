const { getPlatformInfo } = require('../../utils/platform')

Component({
  properties: {
    platform: { type: String, value: '' },
    confidence: { type: Number, value: 0 }
  },

  data: {
    platformInfo: null,
    confidenceText: ''
  },

  observers: {
    'platform, confidence': function(platform, confidence) {
      const info = getPlatformInfo(platform)
      this.setData({
        platformInfo: info,
        confidenceText: confidence > 0 ? `${Math.round(confidence * 100)}%` : ''
      })
    }
  }
})
