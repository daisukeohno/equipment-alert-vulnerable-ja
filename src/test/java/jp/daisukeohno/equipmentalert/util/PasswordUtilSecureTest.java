package jp.daisukeohno.equipmentalert.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PasswordUtilSecureTest {

    @Test
    public void securelyHashesAndVerifiesPassword() {
        String storedHash = PasswordUtil.hashSecure("Corr3ct!Pass");

        assertTrue(PasswordUtil.verifySecure("Corr3ct!Pass", storedHash));
        assertFalse(PasswordUtil.verifySecure("wrong-password", storedHash));
    }

    @Test
    public void usesDifferentSaltForEachHash() {
        assertNotEquals(PasswordUtil.hashSecure("same-password"),
                PasswordUtil.hashSecure("same-password"));
    }

    @Test
    public void rejectsMalformedStoredHashes() {
        assertFalse(PasswordUtil.verifySecure("password", null));
        assertFalse(PasswordUtil.verifySecure("password", ""));
        assertFalse(PasswordUtil.verifySecure("password", "abc"));
        assertFalse(PasswordUtil.verifySecure("password", "md5$1$x$y"));
        assertFalse(PasswordUtil.verifySecure("password", "pbkdf2_sha256$notnum$a$b"));
        assertFalse(PasswordUtil.verifySecure("password", "pbkdf2_sha256$210000$%%%$%%%"));
    }
}
