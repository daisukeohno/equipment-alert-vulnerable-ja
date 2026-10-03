package jp.daisukeohno.equipmentalert.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * パスワードのハッシュ化ユーティリティ。
 *
 * ★セキュリティスキャン検証用に、意図的に脆弱なハッシュ関数(MD5)を
 *   ソルトなしで使用しています(CWE-327: 脆弱な暗号アルゴリズムの使用)。
 *   本来はbcrypt / scrypt / Argon2等、ソルト付きの方式を使うべき。
 */
public class PasswordUtil {

    public static String hash(String rawPassword) {
        try {
            // ★脆弱性: MD5は衝突耐性が低く、パスワードハッシュとして非推奨
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(rawPassword.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("ハッシュ化に失敗しました", e);
        }
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        return hash(rawPassword).equals(hashedPassword);
    }
}
