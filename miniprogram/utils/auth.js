const api = require('./api')

let loginPromise = null

async function ensureLogin() {
  const app = getApp()
  if (app.globalData.userId) {
    return app.globalData.userInfo
  }

  if (!loginPromise) {
    loginPromise = api.login().then(res => {
      if (res.result && res.result.user) {
        app.globalData.userInfo = res.result.user
        app.globalData.userId = res.result.user._openid
      }
      loginPromise = null
      return app.globalData.userInfo
    }).catch(err => {
      loginPromise = null
      throw err
    })
  }

  return loginPromise
}

function getUserInfo() {
  return getApp().globalData.userInfo
}

function getUserId() {
  return getApp().globalData.userId
}

async function checkCredits() {
  const userInfo = await ensureLogin()
  if (!userInfo) return 0
  return userInfo.freeCredits || 0
}

module.exports = {
  ensureLogin,
  getUserInfo,
  getUserId,
  checkCredits
}
