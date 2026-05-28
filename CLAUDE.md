# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

AI 去水印微信小程序（my-notai），使用微信云开发（云开发）+ Stability AI inpainting API 实现。支持去除 Midjourney、DALL-E、Stable Diffusion、通义万相、文心一格等平台的水印。

## 开发命令

本项目使用**微信开发者工具**开发，无法通过命令行构建或运行。

- **导入项目**: 用微信开发者工具打开项目根目录，appid 需要在 `project.config.json` 中配置
- **上传云函数**: 右键 `cloudfunctions/` 下的每个子目录 → "上传并部署：云端安装依赖"
- **安装云函数依赖**: 每个云函数目录内执行 `npm install`（本地调试用）
- **数据库集合**: 需在云开发控制台手动创建 `users`、`history`、`favorites` 三个集合
- **无测试框架**: 项目未配置测试，无 lint 工具

## 架构

### 双根目录结构

```
project.config.json        ← 声明 miniprogramRoot 和 cloudfunctionRoot
miniprogram/               ← 前端小程序代码
cloudfunctions/            ← 无服务器云函数（7 个）
```

### 数据流

```
用户选图 → wx.compressImage → wx.cloud.uploadFile → 云存储
    → detectWatermark 云函数（平台识别）
    → removeWatermark 云函数（mask 生成 → Stability AI API → 结果上传）
    → history 集合写入 → 前端跳转结果页
```

### 核心云函数

- `login` — openid 鉴权，新用户自动建表
- `detectWatermark` — 多阶段水印检测（元数据关键词 + 尺寸匹配），返回 `platform` + `watermarkRegion`（归一化坐标 x/y/w/h）
- `removeWatermark` — **核心**，调用 Stability AI SDXL inpainting API，60 秒超时，API key 从 `process.env.STABILITY_API_KEY` 读取

### 前端工具层 (`miniprogram/utils/`)

- `api.js` — 云函数调用封装，自带 `callWithRetry`（指数退避，最多 2 次重试）
- `auth.js` — 单例登录模式，共享 Promise 防止重复请求
- `platform.js` — 平台检测逻辑（客户端版本，服务端 `detectWatermark` 内有独立副本）
- `image.js` — 图片压缩、校验（最小 256x256，最大 10MB）
- `constants.js` — 全局常量（额度、分页、尺寸限制等）

### 组件通信模式

组件通过 `triggerEvent` 向上派发事件，页面通过 `bind:xxx` 监听。组件不直接调用 API。

### 页面导航

- TabBar: index / history / profile
- processing 和 result 为非 Tab 页，通过 `wx.navigateTo` / `wx.redirectTo` 跳转
- result 页用 `clip-path: inset()` 实现前后对比滑块

## 已知问题

1. **`removeWatermark` 依赖缺失**: `package.json` 只声明了 `axios`，但代码中使用了 `FormData`（来自 `form-data` 包），需补加依赖
2. **`result-card` 组件为空目录**: `miniprogram/components/result-card/` 无文件，属于残留占位
3. **`history` 页 `filter=favorites` 未实现**: 接收了查询参数但 JS 未做分支处理
4. **`profile/clearHistory` 效率问题**: 逐条删除，大量记录时性能差
5. **平台检测逻辑重复**: `utils/platform.js` 和 `detectWatermark/index.js` 各有一份，服务端版本为准

## Android 版本

`android/` 目录下是 Kotlin + Jetpack Compose 原生 Android 应用，使用**本地 OpenCV inpainting** 去水印（无需 API Key，完全离线）。

- 构建：`cd android && ./gradlew assembleRelease`
- 需要 Android SDK（`local.properties` 中 `sdk.dir`）
- 核心逻辑：`android/app/src/main/java/com/notai/app/domain/usecase/RemoveWatermarkUseCase.kt`

## 配置占位符（需替换）

| 文件 | 占位符 | 说明 |
|------|--------|------|
| `project.config.json` | `your-appid-here` | 微信小程序 AppID |
| `miniprogram/app.js` | `your-env-id` | 云开发环境 ID |

## 缺失资源

`miniprogram/images/` 目录为空，以下文件被引用但不存在：
- TabBar 图标: `tab-home.png` / `tab-home-active.png` / `tab-history.png` / `tab-history-active.png` / `tab-profile.png` / `tab-profile-active.png`
- 平台图标: `platforms/midjourney.png` / `dalle.png` / `stable-diffusion.png` / `tongyi.png` / `wenyi.png`
- 默认头像: `default-avatar.png`

## 免费额度机制

新用户默认 10 次免费额度（`users.freeCredits`），每次去水印扣 1。无支付/升级流程，额度用完后前端弹窗提示。扣减在 `removeWatermark` 云函数内通过 `db.command.inc(-1)` 原子操作完成。
