# 烘焙工坊 · 自用 ERP（Android）

> 单用户、本地存储、覆盖"原料采购 → 配方管理 → 制作消耗 → 销售登记 → 保质期预警"全链路的家庭烘焙移动端管理工具。

---

## 一、技术栈

- Kotlin 1.9.24 + Jetpack Compose（Material 3）
- 单 Activity + Navigation Compose
- Room 2.6.1（本地数据库）
- WorkManager 2.9.1（每日 09:00 巡检）
- DataStore Preferences（轻量偏好）
- minSdk 26（Android 8.0），targetSdk 34
- 构建：GitHub Actions（macOS runner，AGP 8.5.2 + Gradle 8.9）

## 二、功能模块

| 编号 | 模块 | 能力 | 优先级 |
|---|---|---|---|
| M1 | 原材料管理 | 基础信息、采购记录、库存查询、自动扣减 | P0 |
| M2 | 配方管理 | 配方定义、原料用量、复制与调整 | P0 |
| M3 | 制作管理 | 按配方生产、自动扣减原料、原料成本核算 | P0 |
| M4 | 销售管理 | 销售登记、关联产品、客户（邻居）档案 | P0 |
| M5 | 保质期预警 | 采购批次保质期、临近过期系统通知 | P0 |

## 三、从源码到 APK · 完整路径

### 路径 A：GitHub Actions 云端构建（推荐）

1. 在 GitHub 新建空仓库（Public 即可，便于免费 Actions）。
2. 将本目录所有文件推送至默认分支：
   ```bash
   cd bake-erp-app
   git init
   git add .
   git commit -m "init: bake-erp app"
   git branch -M main
   git remote add origin https://github.com/<your-name>/bake-erp-app.git
   git push -u origin main
   ```
3. 进入 GitHub 仓库页面 → Actions → "Build Android APK" 工作流自动触发。
4. 工作流完成后，在该次运行页面底部 Artifacts 区下载：
   - `bake-erp-debug`（推荐自用，体积小）
   - `bake-erp-release`（若构建成功）
5. 把下载的 APK 拷贝到 Android 手机，点击安装。首次安装需允许"未知来源"。
6. 首次启动时授予"通知"权限，保质期与库存预警方可生效。

### 路径 B：本地 Android Studio 构建

1. 安装 [Android Studio Koala (2024.1.1)](https://developer.android.com/studio) 或更新版本。
2. 打开 Android Studio → Open → 选择本项目根目录。
3. 等待 Gradle 同步完成（首次会下载 Android SDK 34 与依赖）。
4. 顶部菜单 Build → Build Bundle(s) / APK(s) → Build APK(s)。
5. 构建完成后右下角提示"locate"，点击跳转到 `app/build/outputs/apk/debug/app-debug.apk`。
6. 拷贝到手机安装。

## 四、目录结构

```
bake-erp-app/
├── .github/workflows/android-build.yml   # GitHub Actions 构建脚本
├── app/
│   ├── build.gradle.kts                  # app 模块构建配置
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/bakeerp/app/         # Kotlin 源码
│       │   ├── BakeErpApplication.kt
│       │   ├── MainActivity.kt
│       │   ├── data/                     # 数据层（Entity/DAO/Repository）
│       │   ├── ui/                       # Compose UI（theme/nav/各页面）
│       │   ├── work/                     # WorkManager 提醒任务
│       │   └── util/
│       └── res/                          # 资源文件
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## 五、数据模型

- **Material（原材料）**：名称、单位、当前库存、安全库存、单价、备注
- **PurchaseRecord（采购记录）**：原料、数量、单价、供应商、采购日期、保质期、剩余库存
- **Recipe（配方）**：配方名、产品名、产出数量与单位
- **RecipeItem（配方项）**：配方与原料的多对多关系、用量
- **ProductionRecord（制作记录）**：配方、批次量、产出数量、制作日期、原料成本
- **SaleRecord（销售记录）**：产品、数量、单价、客户、支付方式、销售日期
- **Customer（客户/邻居）**：姓名、联系方式、累计订单与金额

## 六、首次使用建议

1. 进入"原材料" → 新增若干原材料（高筋面粉、黄油、鸡蛋、白砂糖等），录入采购记录与保质期。
2. 进入"配方" → 新增配方（如"经典海绵蛋糕"），按配料填入原料与用量。
3. 进入"制作" → 选择配方并输入批次量，确认扣减库存。
4. 进入"销售" → 登记邻居购买，自动维护客户档案。
5. 每日 09:00 自动推送保质期与库存预警通知。

## 七、避坑提示

- **保质期输入**：日期格式严格为 `yyyy-MM-dd`，如 `2026-12-31`。
- **库存单位**：原料单库存为可自定义字符串（`g`/`ml`/`个`/`kg`/`盒`等），仅作展示用途，不做单位换算。
- **首次启动**：建议预先录入 3-5 条原料与 1-2 条配方，便于"制作"页预览消耗。
- **数据备份**：项目骨架中备份/恢复按钮为占位，建议通过 Android 系统"自动备份"或在 Room 中手动 export JSON（可作为后续扩展）。
- **跨设备迁移**：更换手机时启用系统的"自动备份 Google Drive"即可同步 Room 数据库。

## 八、二次开发路线

- 数据看板（销售概览、成本利润、库存总览）
- 备份/恢复 JSON（Storage Access Framework）
- 报表导出 PDF（iText 或 Android PDF API）
- 多配方版本与历史快照
- 原料批次级 FIFO 扣减（当前为总量扣减）