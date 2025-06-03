# MCP Sample - Production Ready

Android MCP (Model Context Protocol) サンプルアプリケーション - 本番環境対応版

## 📱 アプリ概要

このアプリは、**Model Context Protocol (MCP)** の本格実装のためのベースアプリケーションです。MCPサーバーの管理とAI APIとの連携機能を提供し、実際のプロダクション環境での使用を想定しています。

### 🌟 主な機能

- **MCPサーバー管理**: Android端末上でMCPサーバーの起動・停止
- **AI統合**: OpenAI GPT-4o-mini / Google Gemini APIとの連携
- **MCP接続管理**: 外部MCPサーバーへの接続とリソース管理
- **拡張可能なアーキテクチャ**: 新しいMCPツールやサービスの追加が容易
- **本番対応設計**: セキュリティとパフォーマンスを考慮した実装

## 🏗️ 技術スタック

### 開発環境
- **Android Studio**: 最新版推奨
- **Kotlin**: 2.1.0
- **Android API**: 最小26、ターゲット34
- **Java**: 17

### 主要ライブラリ
- **Jetpack Compose**: Modern Android UI toolkit
- **Compose BOM**: 2024.06.00
- **Coroutines**: 1.7.3 (非同期処理)
- **Ktor Client**: 2.3.7 (HTTP通信)
- **Kotlinx Serialization**: 1.7.3 (JSON処理)

### API統合
- **OpenAI API**: GPT-4o-mini モデル
- **Google Gemini API**: Gemini-1.5-flash-latest モデル
- **MCP Protocol**: Model Context Protocol実装

## 🚀 セットアップ手順

### 1. プロジェクトのクローン
```bash
git clone <repository-url>
cd MCPSample
```

### 2. OpenAI API キーの設定
`app/build.gradle.kts`の以下の部分を編集：
```kotlin
buildConfigField("String", "OPENAI_API_KEY", "\"your_openai_api_key_here\"")
```

⚠️ **セキュリティ注意**: 本番アプリでは、API keyをより安全な方法（環境変数、暗号化ストレージなど）で管理してください。

### 3. ビルドと実行
```bash
./gradlew clean build
./gradlew installDebug
```

または Android Studio で:
1. **Build** → **Clean Project**
2. **Build** → **Rebuild Project**
3. **Run** → **Run 'app'**

## 📋 使用方法

### 基本操作

1. **サーバー起動**
   - 「Start Server」ボタンでMCPサーバーを起動
   - サーバーステータスが🟢に変化することを確認

2. **OpenAI チャット**
   - チャットプロンプト欄にメッセージを入力
   - 「🚀 Send to OpenAI」ボタンでGPT-4o-miniに送信
   - レスポンスがログエリアに表示

3. **MCP機能テスト**
   - 「Test MCP」ボタンで各種MCP接続をテスト
   - Notion、ファイルシステム、メモリサーバーの接続状況を確認

4. **その他の機能**
   - 「Send Ping」: サーバーの疎通確認
   - 「Send Echo」: エコーテスト（カスタムメッセージ）
   - 「Clear」: ログのクリア

### UI構成

```
┌─────────────────────────────────────┐
│ MCP Sample - OpenAI Edition         │
├─────────────────────────────────────┤
│ 🟢 Server Status: Running           │
│ [Start Server] [Stop Server]        │
├─────────────────────────────────────┤
│ Test Actions                        │
│ [Send Ping] [Send Echo] [Test MCP]  │
│ Echo Message: [入力フィールド]       │
├─────────────────────────────────────┤
│ 🤖 OpenAI Chat (GPT-4o-mini)       │
│ Chat Prompt: [入力フィールド]        │
│ [🚀 Send to OpenAI]                 │
├─────────────────────────────────────┤
│ Logs (count)              [Clear]   │
│ ┌─────────────────────────────────┐ │
│ │ [12:07:16.834] Server started   │ │
│ │ [12:07:17.835] Sending to AI... │ │
│ │ [12:07:18.793] ✅ AI response   │ │
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

## 🧪 テスト機能

### 自動テスト項目
- MCPサーバー起動/停止テスト
- OpenAI API接続テスト
- JSON シリアライゼーションテスト
- エラーハンドリングテスト

### 手動テスト推奨項目
- [ ] サーバー起動後のUI状態変化
- [ ] OpenAI APIレスポンスの表示
- [ ] ネットワークエラー時の動作
- [ ] ログの自動スクロール機能
- [ ] 長時間実行時のメモリリーク確認

## 🏛️ アーキテクチャ

### ファイル構成
```
app/src/main/java/com/example/mcpsample/
├── MainActivity.kt          # メインアクティビティ & UI
├── MainViewModel.kt         # ビジネスロジック (ViewModelパターン)
├── UiState.kt              # UI状態管理
├── OpenAIClient.kt         # OpenAI API クライアント
├── OpenAIService.kt        # OpenAI サービスラッパー
├── OpenAIModels.kt         # OpenAI データモデル
├── McpServerManager.kt     # MCPサーバー管理
└── McpClientManager.kt     # MCPクライアント管理
```

### 設計パターン
- **MVVM**: Model-View-ViewModel アーキテクチャ
- **StateFlow**: リアクティブな状態管理
- **Coroutines**: 非同期処理とスレッド管理
- **Dependency Injection**: 手動DI（将来的にDagger/Hilt移行予定）

## 🔧 開発者向け情報

### 既知の制限事項
- MCP SDK統合は現在プレースホルダー実装
- ストリーミングレスポンスは未実装
- APIキーのセキュア管理は簡易実装

### 今後の開発予定
- [ ] 本格的なMCP SDK統合
- [ ] ストリーミングレスポンス対応
- [ ] セキュアなAPIキー管理
- [ ] オフライン機能
- [ ] パフォーマンス最適化
- [ ] ユニットテスト拡充

### カスタマイズポイント
1. **OpenAI モデル変更**: `OpenAIClient.kt`の`model`パラメーター
2. **UI テーマ**: `MCPSampleTheme`コンポーザブル
3. **ログフォーマット**: `UiState.kt`の`createLogEntry`関数
4. **MCP設定**: `McpServerManager.kt`のサーバー設定

## 🤝 コントリビューション

### 開発の参加方法
1. このリポジトリをフォーク
2. フィーチャーブランチを作成 (`git checkout -b feature/新機能名`)
3. 変更をコミット (`git commit -am '新機能を追加'`)
4. ブランチをプッシュ (`git push origin feature/新機能名`)
5. プルリクエストを作成

### コーディング規約
- Kotlin公式スタイルガイドに準拠
- Compose推奨パターンの使用
- 適切なコメントと命名規則
- エラーハンドリングの実装

## 📄 ライセンス

このプロジェクトはMITライセンスの下で公開されています。詳細は`LICENSE`ファイルを参照してください。

## 🆘 サポート

### よくある問題

**Q: OpenAI APIエラーが発生する**
A: API キーが正しく設定されているか確認してください。また、OpenAIアカウントに十分なクレジットがあることを確認してください。

**Q: ビルドエラーが発生する**
A: Kotlin 2.1.0とCompose BOM 2024.06.00の互換性を確認し、`./gradlew clean`を実行してください。

**Q: MCPサーバーが起動しない**
A: 現在プレースホルダー実装のため、実際のMCPサーバーは起動しません。ログでの動作確認となります。

### バグ報告・機能要求
GitHubのIssuesページで報告してください。以下の情報を含めてください：
- Android バージョン
- 実行環境（エミュレータ/実機）
- エラーメッセージまたはログ
- 再現手順

---

**開発者**: [Your Name]  
**最終更新**: 2025年6月3日  
**バージョン**: 1.0.0
