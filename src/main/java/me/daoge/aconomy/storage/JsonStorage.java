package me.daoge.aconomy.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JSON file based storage implementation for economy data.
 *
 * @author daoge_cmd
 */
@Slf4j
public class JsonStorage implements EconomyStorage {

    private static final String DATA_FILE_NAME = "accounts.json";
    private static final Type ACCOUNT_LIST_TYPE = new TypeToken<List<AccountData>>() {}.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataFile;
    private final Map<UUID, AccountData> accounts = new ConcurrentHashMap<>();

    public JsonStorage(Path dataFolder) {
        this.dataFile = dataFolder.resolve(DATA_FILE_NAME);
    }

    @Override
    public boolean init() {
        try {
            Files.createDirectories(dataFile.getParent());
            if (!Files.exists(dataFile)) {
                save();
                return true;
            }
            String content = Files.readString(dataFile);
            if (content.isBlank()) {
                return true;
            }
            List<AccountData> loaded = gson.fromJson(content, ACCOUNT_LIST_TYPE);
            if (loaded != null) {
                loaded.forEach(account -> accounts.put(account.uuid, account));
            }
            log.info("Loaded {} accounts from JSON storage", accounts.size());
            return true;
        } catch (IOException e) {
            log.error("Failed to initialize JSON storage", e);
            return false;
        }
    }

    @Override
    public void shutdown() {
        save();
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return accounts.containsKey(uuid);
    }

    @Override
    public BigDecimal getBalance(UUID uuid) {
        AccountData account = accounts.get(uuid);
        return account != null ? account.balance : BigDecimal.ZERO;
    }

    @Override
    public void setBalance(UUID uuid, BigDecimal balance) {
        AccountData account = accounts.get(uuid);
        if (account != null) {
            account.balance = balance;
            save();
        }
    }

    @Override
    public String getAccountName(UUID uuid) {
        AccountData account = accounts.get(uuid);
        return account != null ? account.name : null;
    }

    @Override
    public void setAccountName(UUID uuid, String name) {
        AccountData account = accounts.get(uuid);
        if (account != null) {
            account.name = name;
            save();
        }
    }

    @Override
    public boolean createAccount(UUID uuid, String name, BigDecimal initialBalance) {
        if (accounts.containsKey(uuid)) {
            return false;
        }
        accounts.put(uuid, new AccountData(uuid, name, initialBalance));
        save();
        return true;
    }

    @Override
    public boolean deleteAccount(UUID uuid) {
        AccountData removed = accounts.remove(uuid);
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    @Override
    public Map<UUID, BigDecimal> getAllBalances() {
        Map<UUID, BigDecimal> result = new HashMap<>();
        accounts.forEach((uuid, account) -> result.put(uuid, account.balance));
        return result;
    }

    @Override
    public Set<UUID> getAllAccountIds() {
        return new HashSet<>(accounts.keySet());
    }

    @Override
    public void save() {
        try {
            Files.createDirectories(dataFile.getParent());
            String json = gson.toJson(new ArrayList<>(accounts.values()), ACCOUNT_LIST_TYPE);
            Files.writeString(
                    dataFile,
                    json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            log.error("Failed to save JSON storage", e);
        }
    }

    /**
     * Internal data class for JSON serialization.
     */
    private static class AccountData {
        UUID uuid;
        String name;
        BigDecimal balance;

        AccountData(UUID uuid, String name, BigDecimal balance) {
            this.uuid = uuid;
            this.name = name;
            this.balance = balance;
        }
    }
}
