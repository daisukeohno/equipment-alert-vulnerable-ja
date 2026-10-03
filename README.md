# 設備アラートバッチ (equipment-alert-vulnerable-ja)

製造業向け「設備アラートバッチ処理」を模した、レガシーJava(Struts 1.x / JDBC相当)アプリケーションのデモです。

> **注意**: 本リポジトリはセキュリティスキャン(Devin Security Swarm等)の検証を目的として、
> **意図的に脆弱性を埋め込んだサンプルコード**です。本番環境には絶対に使用しないでください。

## 想定シナリオ

工場の設備から送られてくるアラート情報を受信・保存し、担当者が検索・ダウンロード・
レポート出力できるバッチ/Webアプリケーション。レガシー資産のモダナイゼーション案件を
想定したサンプルです。

## 構成

```
src/main/java/jp/daisukeohno/equipmentalert/
├── dao/
│   └── EquipmentAlertDao.java      // SQLインジェクション、認証情報のハードコード
├── util/
│   ├── PasswordUtil.java           // 脆弱なハッシュ関数(MD5)
│   ├── AlertFileDownloadServlet.java // パストラバーサル
│   ├── AlertXmlImportService.java  // XXE (XML External Entity)
│   ├── MaintenanceCommandExecutor.java // OSコマンドインジェクション
│   └── AlertReportSerializer.java  // 安全でないデシリアライズ
└── model/
    └── EquipmentAlert.java
src/main/resources/
└── log4j.properties                // 既知のCVEを持つ古いlog4j 1.2系を想定
pom.xml                              // 既知脆弱性を含む古い依存バージョンを指定
```

## 埋め込んだ脆弱性の一覧(セキュリティスキャン検証用)

| 分類 | ファイル | 内容 |
|---|---|---|
| SQLインジェクション (CWE-89) | `EquipmentAlertDao.java` | ユーザー入力を文字列連結でSQLに埋め込み |
| 認証情報のハードコード (CWE-798) | `EquipmentAlertDao.java` | DB接続のID/パスワードをソースに直書き |
| 脆弱な暗号化 (CWE-327) | `PasswordUtil.java` | パスワードハッシュにMD5を使用、ソルトなし |
| パストラバーサル (CWE-22) | `AlertFileDownloadServlet.java` | ファイル名パラメータの検証なし |
| XXE (CWE-611) | `AlertXmlImportService.java` | 外部エンティティ解決を無効化していないXMLパーサ |
| OSコマンドインジェクション (CWE-78) | `MaintenanceCommandExecutor.java` | `Runtime.exec`にユーザー入力をそのまま渡す |
| 安全でないデシリアライズ (CWE-502) | `AlertReportSerializer.java` | `ObjectInputStream`で検証なしに復元 |
| 既知脆弱性のある依存関係 | `pom.xml` | log4j 1.2.17 / commons-collections 3.2.1 など |
| 機密情報のログ出力 (CWE-532) | `EquipmentAlertDao.java` | パスワードを平文でログ出力 |

## ビルド

```bash
mvn clean compile
```

ネットワークやJDKの都合で実行できない場合でも、静的なソースコード/依存関係スキャンの
対象としては利用可能です。
