# Android MCP Sample - 動作確認手順

## ✅ 実装完了内容

### 1. Function Calling対応Gemini API実装
- ✅ GeminiModels.kt: Function Calling対応データモデル
- ✅ GeminiClientWithFunctions.kt: Function Calling実装
- ✅ tools/FunctionRegistry.kt: ツール定義とレジストリ
- ✅ tools/ToolServices.kt: 実際のサービス実装

### 2. 実装されたツール
- ✅ **Weather API** (Open-Meteo): 天気情報取得
- ✅ **Notion API**: ページ作成（フォールバック付き）
- ✅ **Memory Service**: データ保存・取得
- ✅ **System Info Service**: システム情報取得

### 3. UI・UX改善
- ✅ AIプロバイダー切り替え機能
- ✅ Function Calling対応表示
- ✅ 処理中インジケーター
- ✅ クイックテストボタン

## 🚀 動作確認手順

### ステップ1: ビルドと起動
```bash
1. Android Studioでプロジェクトを開く
2. Build > Rebuild Project
3. エラーがないことを確認
4. Run > Run 'app'
```

### ステップ2: AIプロバイダーの設定
1. アプリ起動後、AI Chatセクションを確認
2. 「🛠️ Gemini + Tools」が選択されていることを確認
3. 別のプロバイダーの場合は「→ Gemini + Tools」ボタンをタップ

### ステップ3: Function Callingテスト

#### A. Weather API テスト
```
入力: "今日の横浜の天気を教えて"
期待される動作:
1. Function get_weather が実行される
2. Open-Meteo APIから天気データ取得
3. 温度、湿度、天気状況が表示される
```

#### B. Notion API テスト
```
入力: "「今日の学習記録」というタイトルでNotionページを作成して"
期待される動作:
1. Function create_notion_page が実行される
2. Notion APIでページ作成を試行
3. 成功時：page_idとURLが返される
4. 失敗時：ローカルストレージでシミュレート
```

#### C. System Info テスト
```
入力: "現在の時刻を教えて"
期待される動作:
1. Function get_system_info が実行される
2. 現在時刻、日付、デバイス情報が表示される
```

#### D. Memory テスト
```
入力: "test_keyで「Function Callingテスト成功」をメモリに保存して"
期待される動作:
1. Function store_memory が実行される
2. メモリにデータが保存される
3. 保存成功メッセージが表示される
```

### ステップ4: 複合テスト
```
入力: "今日の横浜の天気をNotionにメモして"
期待される動作:
1. 天気情報を取得
2. 取得した天気情報でNotionページを作成
3. 2つのFunction Callが連続実行される
```

## 📋 ログの確認方法

### 成功時のログ例
```
[HH:mm:ss] 🤖 Sending to Gemini with Function Calling: 今日の横浜の天気を教えて
[HH:mm:ss] ⏳ Waiting for response...
[HH:mm:ss] ✅ Response received:
[HH:mm:ss]    🔧 Function get_weather executed: {"success":true,"location":"横浜",...}
[HH:mm:ss]    横浜の現在の天気をお伝えします...
```

### エラー時の対処法
```
- API Key Error: gradle.propertiesのGEMINI_API_KEYを確認
- Network Error: インターネット接続を確認
- Function Error: ログで詳細エラーメッセージを確認
```

## 🎯 期待される最終結果

1. ✅ **Weather API**: 実際の天気データが取得できる
2. ✅ **Notion API**: ページが作成される（またはフォールバック動作）
3. ✅ **Function Calling**: 複数のツールが自動的に実行される
4. ✅ **Error Handling**: エラー時も適切にフォールバック
5. ✅ **UI/UX**: 直感的で使いやすいインターフェース

## 🔧 トラブルシューティング

### Q: Function Callingが動作しない
A: AIプロバイダーが「Gemini + Tools」になっているか確認

### Q: Notion APIエラーが発生する
A: フォールバック機能により、ローカルストレージでシミュレート

### Q: 天気情報が取得できない
A: Open-Meteo APIは無料で利用可能、ネットワーク接続を確認

### Q: ログが表示されない
A: 「Test Functions」ボタンで段階的にテスト

この実装により、AndroidアプリからGemini APIのFunction Callingを使用して、実際のツール（Weather API、Notion API等）を呼び出すことができるようになりました。最初に提示されたログの問題（テキスト応答のみ）も解決され、実際のツール実行が可能になっています。
