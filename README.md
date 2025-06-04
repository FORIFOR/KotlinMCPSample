# MCP Sample - AI Function Calling Android アプリ

## 📱 このアプリについて

**MCP Sample**は、AndroidスマートフォンでAI（人工知能）のFunction Calling（関数呼び出し）機能を体験できるサンプルアプリです。

### 🤔 Function Callingって何？

通常のAIチャットは「質問に答える」だけですが、Function Callingを使うとAIが**実際に行動を起こす**ことができます。

**例：**
- 「今日の天気を教えて」→ AIが実際に天気APIから最新データを取得
- 「Notionにメモして」→ AIが実際にNotionにページを作成
- 「現在時刻は？」→ AIがスマホの時計から正確な時刻を取得

## 🚀 できること

### 1. 🌤️ 天気予報取得
```
「横浜の天気を教えて」
```
→ AIが気象APIから最新の天気データを取得してお答えします

### 2. 📝 Notion連携
```
「今日の学習記録というタイトルでNotionページを作成して」
```
→ AIが実際にNotionワークスペースに新しいページを作成します

### 3. 🧠 メモリ機能
```
「test_keyで『重要な情報』をメモリに保存して」
```
→ AIがアプリ内にデータを保存し、後で呼び出せます

### 4. ⏰ システム情報
```
「現在の時刻を教えて」
```
→ AIがスマホから正確な日時情報を取得します

## 🏗️ アプリの仕組み

### アーキテクチャ概要
```
あなたの質問
    ↓
┌─────────────────┐
│   Androidアプリ    │
├─────────────────┤
│ 1. AIサービス      │ ← プロバイダー選択（OpenAI/Gemini等）
│ 2. Function呼び出し │ ← AIが必要な機能を判断
│ 3. 実際のAPI実行   │ ← 天気/Notion/メモリ等
└─────────────────┘
    ↓
実際の結果をお返し
```

### 主要コンポーネント

#### 🤖 AIプロバイダー（4種類）
1. **Mock AI** 🤖 - テスト用（インターネット不要）
2. **OpenAI** 🧠 - ChatGPTのAPI
3. **Gemini** 💎 - GoogleのAI（基本版）
4. **Gemini + Tools** 🛠️ - Function Calling対応（推奨）

#### 🔧 Function Tools
- **Weather Service**: Open-Meteo API（無料天気予報）
- **Notion Service**: Notion API（メモ・ドキュメント作成）
- **Memory Service**: アプリ内データ保存
- **System Service**: デバイス情報取得

#### 📡 MCP（Model Context Protocol）
- サーバー：ツール（機能）を提供
- クライアント：AIからツールを呼び出し
- 通信層：リクエスト・レスポンスの管理

## 🛠️ セットアップ方法

### 1. 必要なAPIキー

#### Gemini API キー（必須）
1. [Google AI Studio](https://makersuite.google.com/app/apikey) にアクセス
2. 「Create API Key」をクリック
3. 生成されたキーをコピー

#### Notion API（Notion連携を使う場合）
1. [Notion Developers](https://www.notion.so/my-integrations) にアクセス
2. 「New integration」で新しい統合を作成
3. 生成されたトークンをコピー
4. 使用したいNotionページで統合を追加

### 2. プロジェクトの設定

#### gradle.properties ファイルの作成
プロジェクトルートに `gradle.properties` ファイルを作成：

```properties
# API Keys
GEMINI_API_KEY=your_gemini_api_key_here
OPENAI_API_KEY=your_openai_api_key_here
```

### 3. Android Studioでの実行

```bash
1. Android Studioでプロジェクトを開く
2. Build > Rebuild Project
3. Run > Run 'app'
```

## 📱 使い方

### 基本的な操作手順

1. **アプリを起動**
2. **AIプロバイダーを選択**
   - 「Gemini + Tools」を推奨（Function Calling対応）
3. **質問を入力**
   - 下のクイックテストボタンも使えます
4. **結果を確認**
   - ログエリアで詳細な実行過程を確認

### 🎯 おすすめテストプロンプト

#### 初心者向け
```
今日の東京の天気を教えて
```

#### 中級者向け
```
横浜の天気をNotionの「今日の記録」ページに保存して
```

#### 上級者向け
```
現在の時刻と天気を確認して、適切なコメントと一緒にNotionにまとめて
```

## 🔍 ログの見方

アプリ下部のログエリアで実行状況を確認できます：

### 正常な実行例
```
[14:30:15] 🤖 Sending to Gemini + Tools: 今日の横浜の天気を教えて
[14:30:16] ⏳ Waiting for response...
[14:30:18] 🔧 Function get_weather executed: {"success":true,"location":"横浜"}
[14:30:18] ✅ Response received: 横浜の天気は晴れ、気温22°C...
```

### エラーの例
```
[14:30:15] ❌ Error: API key not found
```

## 🚨 トラブルシューティング

### よくある問題と解決法

#### 1. 「API key not found」エラー
**原因**: APIキーが設定されていない
**解決**: `gradle.properties` ファイルにAPIキーを正しく設定

#### 2. Function Callingが動作しない
**原因**: AIプロバイダーが「Gemini + Tools」以外に設定されている
**解決**: 画面右上のプロバイダー切り替えボタンで「Gemini + Tools」を選択

#### 3. Notion連携エラー
**原因**: NotionのIntegration設定が不完全
**解決**: 
- Notion Integration Tokenが正しいか確認
- 対象ページでIntegrationが追加されているか確認

#### 4. ビルドエラー
**解決**:
```bash
./gradlew clean
rm -rf .gradle
rm -rf app/build
```

### デバッグのコツ

1. **段階的テスト**
   - まずMock AIで基本動作確認
   - 次にGemini基本版でAPI接続確認
   - 最後にFunction Calling機能をテスト

2. **ログの活用**
   - エラーメッセージを注意深く読む
   - Function Call実行の各ステップを確認

3. **Android Studio Logcat**
   ```
   フィルター: tag:MCPSample OR tag:System.out
   ```

## 🎓 学習のポイント

### 初心者の方へ
- まずはMock AIで基本操作に慣れましょう
- ログを見ながらAIがどう動いているか観察してみてください
- 簡単なプロンプトから始めて、徐々に複雑にしていきましょう

### 開発者の方へ
- `FunctionRegistry.kt` でFunction定義を確認
- `GeminiClientWithFunctions.kt` でAPI呼び出し実装を確認
- `tools/ToolServices.kt` で実際のサービス連携を確認

## 🤝 貢献・カスタマイズ

### 新しいFunction Toolの追加方法

1. **新しいサービスクラスを作成** (`tools/ToolServices.kt`)
2. **Function定義を追加** (`tools/FunctionRegistry.kt`)
3. **実行ロジックを実装** (`FunctionRegistry.executeFunction`)

### コード構造
```
app/src/main/java/com/example/mcpsample/
├── MainActivity.kt          # UI画面
├── AIService.kt            # AIプロバイダー管理
├── GeminiClientWithFunctions.kt  # Function Calling実装
├── McpServer.kt            # MCPサーバー
├── McpClient.kt            # MCPクライアント
└── tools/
    ├── FunctionRegistry.kt  # Function定義・実行
    └── ToolServices.kt     # 実際のAPI呼び出し
```

## 📚 関連技術・参考資料

- **MCP (Model Context Protocol)**: [公式ドキュメント](https://modelcontextprotocol.io/)
- **Gemini API**: [Google AI Studio](https://makersuite.google.com/)
- **Function Calling**: [OpenAI Documentation](https://platform.openai.com/docs/guides/function-calling)
- **Android Jetpack Compose**: [公式ガイド](https://developer.android.com/jetpack/compose)

---

このサンプルアプリを通じて、AI Function Callingの基本から実践的な活用まで学んでいただけます。質問やフィードバックがあれば、お気軽にお知らせください！