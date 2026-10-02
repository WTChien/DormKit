# 發布新版本

DormKit 使用 `version.properties`、`CHANGELOG.md` 和 Git tag 管理版本。推送符合 `v*` 的 tag 後，GitHub Actions 會建置簽章版 APK、產生 SHA-256 校驗檔並建立 GitHub Release。GitHub 會自動為每個 Release 提供 Source code ZIP 與 tar.gz。

## 第一次設定 APK 簽章

正式簽章檔不可提交到 Git。請先在自己的電腦建立一次：

```powershell
keytool -genkeypair -v -keystore dormkit-release.jks -alias dormkit -keyalg RSA -keysize 2048 -validity 10000
```

妥善備份 `dormkit-release.jks` 與密碼。日後若遺失簽章檔，就無法用新版 APK 覆蓋安裝舊版。

將簽章檔轉成 Base64：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("dormkit-release.jks")) | Set-Clipboard
```

到 GitHub repository 的 **Settings → Secrets and variables → Actions** 新增：

- `DORMKIT_KEYSTORE_BASE64`：簽章檔的 Base64 內容
- `DORMKIT_KEYSTORE_PASSWORD`：keystore 密碼
- `DORMKIT_KEY_ALIAS`：例如 `dormkit`
- `DORMKIT_KEY_PASSWORD`：key 密碼

## 發布步驟

1. 修改 `version.properties`，增加 `VERSION_NAME` 和 `VERSION_CODE`。
2. 將本次變更從 `CHANGELOG.md` 的 Unreleased 移至新版本標題。
3. Commit 並推送到 GitHub。
4. 建立並推送相同版本的 tag：

```powershell
git tag -a v1.1.0 -m "DormKit 1.1.0"
git push origin v1.1.0
```

5. 到 GitHub 的 **Actions** 頁確認 `Publish release` 成功。
6. 到 **Releases** 頁下載 APK、SHA-256 或 GitHub 自動產生的原始碼壓縮檔。

工作流程會檢查 tag 與 `VERSION_NAME` 是否一致，不一致時會停止發布。
