const cloud = require('wx-server-sdk')
cloud.init({ env: cloud.DYNAMIC_CURRENT_ENV })
const db = cloud.database()

exports.main = async (event, context) => {
  const wxContext = cloud.getWXContext()
  const openid = wxContext.OPENID
  const { fileID } = event

  if (!fileID) {
    return { success: false, error: '缺少文件ID' }
  }

  try {
    const fileRes = await cloud.getTempFileURL({ fileList: [fileID] })
    const tempURL = fileRes.fileList[0].tempFileURL

    await db.collection('users').where({ _openid: openid }).update({
      data: {
        processingCount: db.command.inc(1),
        updatedAt: new Date()
      }
    })

    return {
      success: true,
      fileID,
      tempURL
    }
  } catch (err) {
    console.error('uploadImage error:', err)
    return { success: false, error: err.message }
  }
}
