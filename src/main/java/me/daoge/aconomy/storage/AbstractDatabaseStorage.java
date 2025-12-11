package me.daoge.aconomy.storage;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;

/**
 * Abstract base class for JDBC-based database storage implementations.
 *
 * @author daoge_cmd
 */
@Slf4j
public abstract class AbstractDatabaseStorage implements EconomyStorage {

    protected final Path dataFolder;
    protected Connection connection;

    protected AbstractDatabaseStorage(Path dataFolder) {
        this.dataFolder = dataFolder;
    }

    /**
     * Get the name of the database type for logging purposes.
     */
    protected abstract String getDatabaseName();

    /**
     * Get the JDBC driver class name.
     */
    protected abstract String getDriverClassName();

    /**
     * Get the JDBC connection URL.
     */
    protected abstract String getJdbcUrl();

    /**
     * Get the SQL statement to create the accounts table.
     */
    protected abstract String getCreateTableSql();

    @Override
    public boolean init() {
        try {
            // Load JDBC driver
            Class.forName(getDriverClassName());

            Files.createDirectories(dataFolder);
            connection = DriverManager.getConnection(getJdbcUrl());
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(getCreateTableSql());
            }
            log.info("{} storage initialized successfully", getDatabaseName());
            return true;
        } catch (Exception e) {
            log.error("Failed to initialize {} storage", getDatabaseName(), e);
            return false;
        }
    }

    @Override
    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Failed to close {} connection", getDatabaseName(), e);
            }
        }
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        String sql = "SELECT COUNT(*) FROM accounts WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("Failed to check account existence", e);
        }
        return false;
    }

    @Override
    public BigDecimal getBalance(UUID uuid) {
        String sql = "SELECT balance FROM accounts WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new BigDecimal(rs.getString("balance"));
            }
        } catch (SQLException e) {
            log.error("Failed to get balance", e);
        }
        return BigDecimal.ZERO;
    }

    @Override
    public void setBalance(UUID uuid, BigDecimal balance) {
        String sql = "UPDATE accounts SET balance = ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, balance.toPlainString());
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to set balance", e);
        }
    }

    @Override
    public String getAccountName(UUID uuid) {
        String sql = "SELECT name FROM accounts WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            log.error("Failed to get account name", e);
        }
        return null;
    }

    @Override
    public void setAccountName(UUID uuid, String name) {
        String sql = "UPDATE accounts SET name = ? WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to set account name", e);
        }
    }

    @Override
    public boolean createAccount(UUID uuid, String name, BigDecimal initialBalance) {
        if (hasAccount(uuid)) {
            return false;
        }
        String sql = "INSERT INTO accounts (uuid, name, balance) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setString(3, initialBalance.toPlainString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            log.error("Failed to create account", e);
        }
        return false;
    }

    @Override
    public boolean deleteAccount(UUID uuid) {
        String sql = "DELETE FROM accounts WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            int affected = pstmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            log.error("Failed to delete account", e);
        }
        return false;
    }

    @Override
    public Map<UUID, BigDecimal> getAllBalances() {
        Map<UUID, BigDecimal> result = new HashMap<>();
        String sql = "SELECT uuid, balance FROM accounts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                BigDecimal balance = new BigDecimal(rs.getString("balance"));
                result.put(uuid, balance);
            }
        } catch (SQLException e) {
            log.error("Failed to get all balances", e);
        }
        return result;
    }

    @Override
    public Set<UUID> getAllAccountIds() {
        Set<UUID> result = new HashSet<>();
        String sql = "SELECT uuid FROM accounts";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(UUID.fromString(rs.getString("uuid")));
            }
        } catch (SQLException e) {
            log.error("Failed to get all account IDs", e);
        }
        return result;
    }

    @Override
    public void save() {
        // Most databases auto-commit by default, nothing to do here
    }
}
