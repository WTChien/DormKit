# DormKit

DormKit 是一個以 Kotlin 與 Jetpack Compose 開發的離線宿舍／房間生活管理 App。它可以分開盤點宿舍與家裡房間的物品、整理每週回家與回宿舍清單，並在洗衣完成時發送通知。

> 目前版本：**1.1.0** · 最低支援 Android 8.0（API 26）

## 下載

- [下載最新版本 APK](../../releases/latest)
- [查看所有版本與更新內容](../../releases)
- [完整更新紀錄](CHANGELOG.md)

GitHub Release 會同時提供：

- `DormKit-x.y.z.apk`：可安裝的正式簽章 APK
- `DormKit-x.y.z.apk.sha256`：APK 完整性校驗值
- `Source code (zip)` 與 `Source code (tar.gz)`：GitHub 自動產生的原始碼壓縮檔

若目前尚未建立 GitHub Release，也可以從本機建置 Debug APK。

## 功能

### 宿舍與房間庫存

- 在「宿舍」與「家裡房間」之間切換
- 新增、編輯及刪除物品
- 快速增加或減少數量
- 搜尋、分類篩選及低庫存篩選
- 自訂數量、單位、最低庫存與備註
- 低於最低庫存時顯示紅色提醒

### 回家／回宿舍清單

- 宿舍 → 家裡與家裡 → 宿舍兩種方向
- 新增、編輯、刪除及勾選完成
- 調整數量與單位
- 已完成項目保留並降低視覺強度

### 洗衣計時器

- 30、40、45、60 分鐘快速設定
- 支援自訂時間與提前取消
- 儲存實際開始及結束時間，不依賴畫面倒數
- App 進入背景或重新開啟後仍能正確計算
- Android Notification Channel 與完成通知
- 手機重新開機後恢復尚未完成的計時器

### 其他

- Dashboard 低庫存、待帶物品與洗衣狀態摘要
- 分類新增、編輯及刪除
- Light／Dark Mode
- 所有資料只儲存在本機，不需要帳號或網路

## 畫面 Demo

用瀏覽器開啟 [`demo/dormkit-demo.html`](demo/dormkit-demo.html) 即可操作 UI Demo。Demo 只提供畫面與互動預覽，不會讀寫 Android App 的 Room 資料。

## 技術架構

| 項目 | 使用技術 |
| --- | --- |
| 語言 | Kotlin |
| UI | Jetpack Compose、Material 3 |
| 架構 | MVVM、Repository |
| 資料庫 | Room Database |
| 設定儲存 | Preferences DataStore |
| 非同步 | Coroutines、Flow |
| 導覽 | Navigation Compose |
| 背景提醒 | AlarmManager、BroadcastReceiver、Notification |

專案維持單一 `app` module，避免為個人使用情境加入不必要的架構層。

## 開發環境

- Android Studio
- JDK 17
- Android SDK 35

使用 Android Studio 開啟專案根目錄，等待 Gradle Sync 完成後，即可選擇 Android 8.0 以上的模擬器或實體裝置執行 `app`。

Android 13 以上需要允許通知權限。Android 12 以上若要準時收到洗衣通知，請在洗衣頁開啟「鬧鐘與提醒」權限。

### 命令列建置

Windows：

```powershell
.\gradlew.bat assembleDebug
```

macOS／Linux：

```bash
./gradlew assembleDebug
```

Debug APK 位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

執行完整檢查：

```bash
./gradlew assembleDebug lintDebug
```

## 專案結構

```text
app/src/main/java/com/dormkit/app/
├── data/           DataStore 設定
├── database/       Room Database、DAO、Converters、Migrations
├── model/          Entities 與 UI 使用的資料模型
├── navigation/     Navigation Compose 與 Bottom Navigation
├── notification/   洗衣提醒、AlarmManager 與開機恢復
├── repository/     資料存取層
├── ui/             Compose 畫面、元件與主題
└── viewmodel/      畫面狀態與操作
```

## Database schema v2

- `categories(id, name, icon)`
- `items(id, name, category_id, quantity, unit, minimum_quantity, note, location)`
- `packing_items(id, name, direction, quantity, unit, checked, note)`
- `laundry_timers(id, start_time, end_time, active)`

`items.location` 使用 `DORM` 或 `ROOM` 區分宿舍與家裡房間。從 schema v1 升級時，既有物品會自動保留並歸入宿舍。刪除分類時，物品會保留並改為未分類。

## 版本與發布

- App 版本記錄在 [`version.properties`](version.properties)
- 每次版本內容記錄在 [`CHANGELOG.md`](CHANGELOG.md)
- 一般 push／PR 由 `Android CI` 自動執行 Build 與 Lint
- 推送 `v*` tag 後，`Publish release` 會自動建立 GitHub Release
- 正式簽章與完整發布步驟請看 [`RELEASE.md`](RELEASE.md)

版本格式採用 `主版本.次版本.修訂版本`：

- `1.1.0`：加入向下相容的新功能
- `1.1.1`：修正問題
- `2.0.0`：包含不相容的重要變更

## 隱私

DormKit 不包含分析工具、廣告、登入系統、後端服務或雲端資料庫。庫存、清單和計時器資料都保存在使用者的 Android 裝置上。
