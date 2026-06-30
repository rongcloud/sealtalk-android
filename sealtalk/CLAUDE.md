# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 提供在此仓库中工作时的指导信息。

## 项目概述

android-sealtalk 是基于融云 IM SDK 的即时通讯演示应用,展示完整的 IM 功能集成。采用 MVVM 架构(ViewModel + LiveData + Room),支持多渠道打包、多主题切换和多厂商推送。

## 构建命令

```bash
# 构建开发版本
./gradlew :android-sealtalk:assembleDevelop

# 构建应用商店版本
./gradlew :android-sealtalk:assemblePublishstore

# 构建 Google Play 版本
./gradlew :android-sealtalk:assemblePublishgoogle

# 运行测试
./gradlew :android-sealtalk:testDevelopDebugUnitTest

# 代码格式化
./gradlew spotlessApply
```

## 架构

### MVVM 架构模式

- **View 层**: Activity / Fragment,负责 UI 展示和用户交互
- **ViewModel 层**: 基于 AndroidViewModel + LiveData/MediatorLiveData 的响应式状态管理
- **Model 层**: Repository 模式 + Room 数据库 + Retrofit 网络层

核心 ViewModel(20+):
- `MainViewModel` - 主界面
- `LoginViewModel` - 登录
- `ConversationViewModel` - 会话

### 源码组织(包名: `cn.rongcloud.im`)

```
common/          - 工具类、错误码
contact/         - 联系人管理
db/              - Room 数据库(DAO、Entity)
net/             - 网络层(Retrofit Service、代理配置)
ui/              - UI 层
  activity/      - 100+ Activity
  adapter/       - RecyclerView 适配器
  dialog/        - 自定义对话框
  fragment/      - Fragment
  view/          - 自定义 View
  widget/        - 可复用控件
viewmodel/       - ViewModel 层
ultraGroup/      - 超级群(频道)管理
openclaw/        - AI 机器人集成
```

### 产品风味(Build Flavors)

| Flavor | 用途 | 特点 |
|--------|------|------|
| `develop` | 开发调试 | 完整功能,含微信 SDK、调试工具 |
| `publishstore` | 国内应用商店 | 生产配置 |
| `publishMEIZU` | 魅族商店 | 移除 32 位 SO 库 |
| `publishgoogle` | Google Play | 仅 FCM 推送,无国内厂商推送 |

### 导航流程

`SplashActivity` → `LoginActivity` → `MainActivity` → `ConversationActivity`

### 深链接支持
- `sealtalk://` 自定义 scheme
- `rong://` 协议(conversation, push_message, sight/player 等路径)

### 模块依赖关系

```
android-sealtalk (本模块)
  ├── IMKit (:kit) — 会话列表、聊天界面等 UI 组件
  ├── IMLib (:lib) — IM 核心 API
  ├── CallKit (:callkit) — 音视频通话 UI
  ├── CallAdapter (:callAdapter) — 通话适配器
  ├── 推送插件 (:pushplugin:*) — 多厂商推送
  └── 底层依赖链:
        :kit → :lib → :libcore → protocol-cpp (C++ 协议层/JNI)
```

本应用通过依赖 IMKit/IMLib 获得完整 IM 能力,底层实际由 `protocol-cpp` C++ 协议栈驱动。

## 构建配置

- **命名空间**: `cn.rongcloud.im`
- **Java**: 11(sourceCompatibility/targetCompatibility)
- **NDK ABI**: arm64-v8a, armeabi-v7a, x86, x86_64
- **MultiDex**: 启用
- **签名**: 使用 `rong.key` 文件

### 关键配置属性(gradle.properties)

- `SEALTALK_SERVER` - 后端 API 服务器
- `SEALTALK_NAVI_SERVER` - 导航/发现服务器
- `SEALTALK_FILE_SERVER` - 文件上传/下载
- `SEALTALK_APP_KEY` - 融云 SDK AppKey
- `SEALTALK_VER` - 应用版本号

## 主要依赖

- **融云 SDK**: IMKit, IMLib, CallKit, CallAdapter(全功能 IM + 音视频)
- **AndroidX**: AppCompat, Lifecycle, Room(2.4.0), ConstraintLayout
- **网络**: Retrofit 2.5.0 + Gson
- **图片**: Glide 4.16.0
- **二维码**: ZXing 3.3.2
- **文件上传**: 七牛 SDK
- **崩溃上报**: 腾讯 Bugly
- **调试**: Stetho
- **推送**: FCM, 华为, 小米, OPPO, vivo, 魅族, 荣耀

## 主题系统

支持多主题切换(内置传统主题、Lively 主题),支持亮色/暗色模式:
- `res-lively-sealtalk/` - Lively 主题资源
- `res-lively-sealtalk-light/` - 亮色模式
- `res-lively-sealtalk-dark/` - 暗色模式

## 测试

[//]: # (测试覆盖率较低,主要依赖 Debug Activity 进行 QA。)

[//]: # (- 测试目录: `src/test/java/`)

[//]: # (- 测试框架: JUnit 4.12)

[//]: # (- Debug 工具: `SealTalkDebugTestActivity`&#40;50+ 调试界面&#41;)
