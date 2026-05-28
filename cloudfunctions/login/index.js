const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

exports.main = async (event, context) => {
  const wxContext = cloud.getWXContext()
  const openid = wxContext.OPENID

  try {
    const userRes = await db.collection('users').where({ _openid: openid }).get()

    if (userRes.data.length > 0) {
      return { success: true, user: userRes.data[0] }
    }

    const newUser = {
      _openid: openid,
      nickname: '',
      avatarUrl: '',
      createdAt: new Date(),
      updatedAt: new Date(),
      processingCount: 0,
      freeCredits: 10,
      plan: 'free',
      settings: {
        autoDetectPlatform: true,
        imageQuality: 'high',
        notifications: true
      }
    }

    const addRes = await db.collection('users').add({ data: newUser })
    newUser._id = addRes._id

    return { success: true, user: newUser }
  } catch (err) {
    console.error('login error:', err)
    return { success: false, error: err.message }
  }
}
