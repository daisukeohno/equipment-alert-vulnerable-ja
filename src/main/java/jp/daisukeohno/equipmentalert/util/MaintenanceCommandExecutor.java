package jp.daisukeohno.equipmentalert.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 設備のメンテナンス用スクリプトをホストOS上で実行するためのユーティリティ。
 *
 * ★セキュリティスキャン検証用に、意図的にOSコマンドインジェクション脆弱性
 *   (CWE-78)を埋め込んでいます。設備コードをそのままシェルコマンドに
 *   連結しているため、例えば equipmentCode に
 *   "LINE-01; rm -rf /" のような値を渡すと任意コマンドが実行されてしまう。
 */
public class MaintenanceCommandExecutor {

    public String runDiagnostics(String equipmentCode) throws IOException, InterruptedException {
        // ★脆弱性: ユーザー入力をそのままシェル経由でコマンド実行している
        String command = "sh -c /opt/factory-tools/diagnostics.sh " + equipmentCode;

        Process process = Runtime.getRuntime().exec(command);

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor();
        return output.toString();
    }
}
