package jp.daisukeohno.equipmentalert.util;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * アラートに添付された帳票PDF/ログファイルをダウンロードするためのサーブレット。
 *
 * ★セキュリティスキャン検証用に、意図的にパストラバーサル脆弱性(CWE-22)を
 *   埋め込んでいます。ファイル名パラメータの検証を一切行っていないため、
 *   例えば fileName=../../../../etc/passwd のようなリクエストでサーバー上の
 *   任意ファイルを読み取れてしまう。
 */
public class AlertFileDownloadServlet extends HttpServlet {

    // 添付ファイルの格納ベースディレクトリ(のつもり)
    private static final String BASE_DIR = "/var/factory-app/alert-attachments/";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ★脆弱性: ユーザー入力の fileName をそのままファイルパスに連結している
        String fileName = request.getParameter("fileName");
        File file = new File(BASE_DIR + fileName);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}
