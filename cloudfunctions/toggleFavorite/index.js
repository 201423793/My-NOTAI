const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

exports.main = async (event, context) => {
  const wxContext = cloud.getWXContext()
  const openid = wxContext.OPENID
  const { historyId } = event

  if (!historyId) {
    return { success: false, error: '缺少历史记录ID' }
  }

  try {
    const existing = await db.collection('favorites')
      .where({
        _openid: openid,
        historyId: historyId
      })
      .get()

    if (existing.data.length > 0) {
      await db.collection('favorites').doc(existing.data[0]._id).remove()
      await db.collection('history').doc(historyId).update({
        data: { isFavorited: false }
      })
      return { success: true, favorited: false }
    }

    await db.collection('favorites').add({
      data: {
        _openid: openid,
        userId: openid,
        historyId: historyId,
        createdAt: new Date()
      }
    })

    await db.collection('history').doc(historyId).update({
      data: { isFavorited: true }
    })

    return { success: true, favorited: true }
  } catch (err) {
    console.error('toggleFavorite error:', err)
    return { success: false, error: err.message }
  }
}
