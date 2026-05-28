module.exports = {
  // 云存储路径前缀
  CLOUD_PATH: {
    UPLOADS: 'uploads/',
    RESULTS: 'results/',
    THUMBNAILS: 'thumbnails/'
  },

  // 图片限制
  IMAGE_LIMITS: {
    MAX_SIZE: 10 * 1024 * 1024,
    MAX_WIDTH: 4096,
    MAX_HEIGHT: 4096,
    MIN_WIDTH: 256,
    MIN_HEIGHT: 256,
    COMPRESS_QUALITY: 80
  },

  // 分页
  PAGINATION: {
    PAGE_SIZE: 20
  },

  // 用户计划
  PLANS: {
    FREE: 'free',
    PRO: 'pro'
  },

  // 免费额度
  FREE_CREDITS: 10,

  // 水印区域默认比例
  WATERMARK_REGION: {
    BOTTOM_RATIO: 0.15,
    PADDING: 10
  },

  // 支持的平台
  PLATFORMS: {
    MIDJOURNEY: 'midjourney',
    DALLE: 'dalle',
    STABLE_DIFFUSION: 'stable-diffusion',
    TONGYI: 'tongyi',
    WENYI: 'wenyi',
    UNKNOWN: 'unknown'
  }
}
