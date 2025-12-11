package me.daoge.aconomy.storage;

import java.nio.file.Path;

/**
 * SQLite database storage implementation for economy data.
 *
 * @author daoge_cmd
 */
public class SqliteStorage extends AbstractDatabaseStorage {

    private static final String DB_FILE_NAME = "economy.db";
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS accounts (
                uuid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                balance TEXT NOT NULL
            )
            """;

    public SqliteStorage(Path dataFolder) {
        super(dataFolder);
    }

    @Override
    protected String getDatabaseName() {
        return "SQLite";
    }

    @Override
    protected String getDriverClassName() {
        return "org.sqlite.JDBC";
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:sqlite:" + dataFolder.resolve(DB_FILE_NAME).toAbsolutePath();
    }

    @Override
    protected String getCreateTableSql() {
        return CREATE_TABLE_SQL;
    }
}
