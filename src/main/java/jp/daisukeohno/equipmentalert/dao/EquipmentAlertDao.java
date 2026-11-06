package jp.daisukeohno.equipmentalert.dao;

import jp.daisukeohno.equipmentalert.model.EquipmentAlert;
import jp.daisukeohno.equipmentalert.util.PasswordUtil;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 設備アラート情報のデータアクセスクラス。
 *
 * ★セキュリティスキャン検証用に、意図的に以下の脆弱性を含んでいます★
 *   - SQLインジェクション (CWE-89)
 *   - 認証情報のハードコード (CWE-798)
 *   - 機密情報(パスワード)の平文ログ出力 (CWE-532)
 */
public class EquipmentAlertDao {

    private static final Logger logger = Logger.getLogger(EquipmentAlertDao.class);
    private static final String DUMMY_HASH = PasswordUtil.hashSecure("dummy-password");

    // ★脆弱性: DB接続情報をソースコードに直書きしている(本来は環境変数や
    //   シークレット管理サービス経由で取得すべき)
    private static final String DB_URL = "jdbc:mysql://10.0.1.25:3306/factory_db";
    private static final String DB_USER = "factory_admin";
    private static final String DB_PASSWORD = "F4ct0ry!Adm1n2024";

    public Connection getConnection() throws SQLException {
        // ★脆弱性: 接続に使うパスワードをそのままログへ出力してしまっている
        logger.info("DB接続を開始します。user=" + DB_USER + " password=" + DB_PASSWORD);
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * 設備コードを条件に、アラート一覧を検索する。
     *
     * ★脆弱性: equipmentCode をプレースホルダを使わず文字列連結でSQLに
     *   埋め込んでいるため、SQLインジェクションが可能。
     *   例: equipmentCode に `' OR '1'='1` を渡すと全件取得できてしまう。
     */
    public List<EquipmentAlert> searchByEquipmentCode(String equipmentCode) throws SQLException {
        List<EquipmentAlert> result = new ArrayList<>();

        String sql = "SELECT alert_id, equipment_code, alert_level, message, occurred_at, reported_by "
                + "FROM equipment_alert WHERE equipment_code = '" + equipmentCode + "'";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                EquipmentAlert alert = new EquipmentAlert(
                        rs.getInt("alert_id"),
                        rs.getString("equipment_code"),
                        rs.getString("alert_level"),
                        rs.getString("message"),
                        rs.getTimestamp("occurred_at"),
                        rs.getString("reported_by")
                );
                result.add(alert);
            }
        }
        return result;
    }

    /**
     * ログイン認証処理。
     * ユーザー名をプレースホルダで検索し、PBKDF2ハッシュでパスワードを照合する。
     * operator_account に password_hash 列を追加し、PasswordUtil.hashSecure で
     * 作成した値を格納してください。既存の平文 password 列は移行が必要です。
     */
    public boolean authenticate(String username, String password) throws SQLException {
        if (username == null || password == null) {
            return false;
        }

        String sql = "SELECT password_hash FROM operator_account WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return PasswordUtil.verifySecure(password, rs.getString(1));
                }
            }
        }

        PasswordUtil.verifySecure(password, DUMMY_HASH);
        return false;
    }
}
