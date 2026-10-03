package jp.daisukeohno.equipmentalert.util;

import jp.daisukeohno.equipmentalert.model.EquipmentAlert;

import java.io.*;

/**
 * アラートレポートをファイルへシリアライズ/デシリアライズするユーティリティ。
 * 他拠点とのレポート受け渡しに使用する想定。
 *
 * ★セキュリティスキャン検証用に、意図的に安全でないデシリアライズ脆弱性
 *   (CWE-502)を埋め込んでいます。外部から受け取ったバイト列を検証なしに
 *   ObjectInputStream#readObject() へそのまま渡しているため、
 *   細工されたシリアライズデータによる任意コード実行(ガジェットチェーン攻撃)
 *   のリスクがある。pom.xml の commons-collections 3.2.1 と組み合わせることで
 *   実際に悪用可能なガジェットチェーンの一例を再現できる。
 */
public class AlertReportSerializer {

    public void saveReport(EquipmentAlert alert, String filePath) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(alert);
        }
    }

    /**
     * ★脆弱性: 入力元(ファイル/ネットワーク経由)の信頼性を検証せず、
     *   任意のバイト列をそのままデシリアライズしている。
     */
    public EquipmentAlert loadReport(InputStream rawInput) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(rawInput)) {
            return (EquipmentAlert) in.readObject();
        }
    }
}
