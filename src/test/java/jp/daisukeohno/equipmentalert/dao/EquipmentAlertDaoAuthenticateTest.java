package jp.daisukeohno.equipmentalert.dao;

import jp.daisukeohno.equipmentalert.util.PasswordUtil;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EquipmentAlertDaoAuthenticateTest {

    private static final String JDBC_URL = "jdbc:h2:mem:authtest;DB_CLOSE_DELAY=-1";

    private EquipmentAlertDao dao;

    @Before
    public void setUp() throws SQLException {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS operator_account");
            statement.execute("CREATE TABLE operator_account ("
                    + "username VARCHAR(64) PRIMARY KEY, password_hash VARCHAR(255))");
        }

        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO operator_account (username, password_hash) VALUES (?, ?)")) {
            statement.setString(1, "admin");
            statement.setString(2, PasswordUtil.hashSecure("Corr3ct!Pass"));
            statement.executeUpdate();
            statement.setString(1, "alice");
            statement.setString(2, PasswordUtil.hashSecure("Alice!Pass"));
            statement.executeUpdate();
        }

        dao = new EquipmentAlertDao() {
            @Override
            public Connection getConnection() throws SQLException {
                return openConnection();
            }
        };
    }

    @Test
    public void authenticatesCorrectCredentials() throws SQLException {
        assertTrue(dao.authenticate("admin", "Corr3ct!Pass"));
    }

    @Test
    public void rejectsWrongPassword() throws SQLException {
        assertFalse(dao.authenticate("admin", "wrong-password"));
    }

    @Test
    public void rejectsUnknownUser() throws SQLException {
        assertFalse(dao.authenticate("unknown", "Corr3ct!Pass"));
    }

    @Test
    public void rejectsSqlInjectionPayloads() throws SQLException {
        assertFalse(dao.authenticate("admin' -- ", "anything"));
        assertFalse(dao.authenticate("' OR '1'='1", "' OR '1'='1"));
        assertFalse(dao.authenticate("admin", "' OR '1'='1"));
    }

    @Test
    public void rejectsNullCredentials() throws SQLException {
        assertFalse(dao.authenticate(null, "Corr3ct!Pass"));
        assertFalse(dao.authenticate("admin", null));
    }

    @Test
    public void dropTablePayloadDoesNotBreakAuthentication() throws SQLException {
        assertFalse(dao.authenticate("admin'; DROP TABLE operator_account; --", "anything"));

        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM operator_account")) {
            assertTrue(resultSet.next());
            assertTrue(resultSet.getInt(1) > 0);
        }
        assertTrue(dao.authenticate("admin", "Corr3ct!Pass"));
    }

    private static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, "sa", "");
    }
}
