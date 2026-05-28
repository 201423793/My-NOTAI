const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

exports.main = async (event, context) => {
  const wxContext = cloud.getWXContext()
  const openid = wxContext.OPENID
  const { skip = 0, limit = 20 } = event

  try {
    const countRes = await db.collection('favorites')
      .where({ _openid: openid })
      .count()

    const favRes = await db.collection('favorites')
      .where({ _openid: openid })
      .orderBy('createdAt', 'desc')
      .skip(skip)
      .limit(limit)
      .get()

    const historyIds = favRes.data.map(fav => fav.historyId)
    let items = []

    if (historyIds.length > 0) {
      const historyRes = await db.collection('history')
        .where({
          _id: db.command.in(historyIds)
        })
        .get()

      const historyMap = {}
      for (const h of historyRes.data) {
        historyMap[h._id] = h
      }

      items = favRes.data.map(fav => ({
        ...fav,
        history: historyMap[fav.historyId] || null
      }))
    }

    return {
      success: true,
      items,
      total: countRes.total,
      hasMore: skip + items.length < countRes.total
    }
  } catch (err) {
    console.error('getFavorites error:', err)
    return { success: false, error: err.message }
  }
}
