const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

exports.main = async (event, context) => {
  const wxContext = cloud.getWXContext()
  const openid = wxContext.OPENID
  const { skip = 0, limit = 20 } = event

  try {
    const countRes = await db.collection('history')
      .where({ _openid: openid })
      .count()

    const dataRes = await db.collection('history')
      .where({ _openid: openid })
      .orderBy('processedAt', 'desc')
      .skip(skip)
      .limit(limit)
      .get()

    return {
      success: true,
      items: dataRes.data,
      total: countRes.total,
      hasMore: skip + dataRes.data.length < countRes.total
    }
  } catch (err) {
    console.error('getHistory error:', err)
    return { success: false, error: err.message }
  }
}
