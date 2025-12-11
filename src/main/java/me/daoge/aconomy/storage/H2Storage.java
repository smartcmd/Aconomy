package me.daoge.aconomy.storage;

import java.nio.file.Path;

/**
 * H2 database storage implementation for economy data.
 *
 * @author daoge_cmd
 */
public class H2Storage extends AbstractDatabaseStorage {

    private static final String DB_FILE_NAME = "economy";
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS accounts (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                balance VARCHAR(255) NOT NULL
            )
            """;

    public H2Storage(Path dataFolder) {
        super(dataFolder);
    }

    @Override
    protected String getDatabaseName() {
        return "H2";
    }

    @Override
    protected String getDriverClassName() {
        return "org.h2.Driver";
    }

    @Override
    protected String getJdbcUrl() {
        return "jdbc:h2:" + dataFolder.resolve(DB_FILE_NAME).toAbsolutePath();
    }

    @Override
    protected String getCreateTableSql() {
        return CREATE_TABLE_SQL;
    }
}
