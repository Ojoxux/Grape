# BLUFF Server Side

Spring Boot のバックエンド API サーバー。Docker 上で動作し、ホットリロード対応の開発環境を備える。

## 技術スタック

| 項目 | バージョン |
|---|---|
| Java | 21 (Eclipse Temurin) |
| Spring Boot | 3.5.0 |
| Gradle | 8.x (Wrapper) |
| Docker | required |

## セットアップ

```bash
git clone <repository-url>
cd BLUFF-ServerSide
```

### 開発環境の起動

```bash
docker compose up --build
```

http://localhost:8080 でアクセス可能。

ソースコードを編集すると自動で再コンパイル・リスタートされる（ホットリロード）。

### 開発環境の停止

```bash
docker compose down
```

## ホットリロードの仕組み

開発用コンテナ内で 2 つの Gradle プロセスが並行動作する。

1. `gradlew classes --continuous` — `src/` の変更を監視し `.class` ファイルを自動再コンパイル
2. `gradlew bootRun` — Spring Boot DevTools が `.class` の変更を検知し自動リスタート

ソース保存から反映まで約 5〜10 秒。

## 本番ビルド

```bash
docker build -t bluff .
docker run -p 8080:8080 bluff
```

マルチステージビルドにより、JRE のみの軽量イメージが生成される。

## ディレクトリ構成

```
├── Dockerfile            # 本番用 (マルチステージビルド)
├── Dockerfile.dev        # 開発用 (ホットリロード対応)
├── docker-compose.yml    # 開発環境定義
├── build.gradle          # 依存関係・ビルド設定
├── settings.gradle
├── gradlew / gradle/
└── src/
    ├── main/
    │   ├── java/com/bluff/
    │   │   ├── BluffApplication.java
    │   │   └── HelloController.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/bluff/
            └── BluffApplicationTests.java
```
