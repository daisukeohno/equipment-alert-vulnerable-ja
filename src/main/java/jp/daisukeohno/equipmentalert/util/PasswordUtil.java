package jp.daisukeohno.equipmentalert.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * パスワードのハッシュ化ユーティリティ。
 *
 * ★セキュリティスキャン検証用に、意図的に脆弱なハッシュ関数(MD5)を
 *   ソルトなしで使用しています(CWE-327: 脆弱な暗号アルゴリズムの使用)。
 *   本来はbcrypt / scrypt / Argon2等、ソルト付きの方式を使うべき。
 */
public class PasswordUtil {

    private static final int SECURE_ITERATIONS = 210000;
    private static final int SECURE_KEY_LENGTH = 256;
    private static final int SECURE_SALT_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

    public static String hashSecure(String rawPassword) {
        byte[] salt = new byte[SECURE_SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = deriveSecureHash(rawPassword, salt, SECURE_ITERATIONS, SECURE_KEY_LENGTH);
        return "pbkdf2_sha256$" + SECURE_ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifySecure(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);
        if (parts.length != 4 || !"pbkdf2_sha256".equals(parts[0])) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            if (iterations <= 0 || salt.length == 0 || expectedHash.length == 0
                    || expectedHash.length > Integer.MAX_VALUE / 8) {
                return false;
            }

            byte[] actualHash = deriveSecureHash(rawPassword, salt, iterations, expectedHash.length * 8);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] deriveSecureHash(String rawPassword, byte[] salt, int iterations, int keyLength) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, keyLength);
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return keyFactory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("ハッシュ化に失敗しました", e);
        } finally {
            spec.clearPassword();
        }
    }
}
