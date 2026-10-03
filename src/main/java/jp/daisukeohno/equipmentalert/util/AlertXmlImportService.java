package jp.daisukeohno.equipmentalert.util;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

/**
 * 他社設備管理システムから出力されたXML形式のアラートデータを取り込むサービス。
 *
 * ★セキュリティスキャン検証用に、意図的にXXE(XML External Entity)脆弱性
 *   (CWE-611)を埋め込んでいます。DocumentBuilderFactory に対して
 *   外部エンティティ・外部DTDの読み込みを無効化する設定(FEATURE_SECURE_PROCESSING等)
 *   を行っていないため、悪意のあるXMLを読み込ませることでサーバー内の
 *   任意ファイル読み取りやSSRFにつながる可能性がある。
 */
public class AlertXmlImportService {

    public Document importAlertXml(InputStream xmlInputStream) throws Exception {
        // ★脆弱性: デフォルト設定のままDocumentBuilderFactoryを使用しており、
        //   外部エンティティの解決が有効なままになっている
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlInputStream);
    }
}
