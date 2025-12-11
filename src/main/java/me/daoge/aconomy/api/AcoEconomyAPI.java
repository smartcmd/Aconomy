package me.daoge.aconomy.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.daoge.aconomy.storage.EconomyStorage;
import org.allaymc.api.server.Server;
import org.allaymc.economyapi.Account;
import org.allaymc.economyapi.Currency;
import org.allaymc.economyapi.EconomyAPI;
import org.allaymc.economyapi.event.AccountCreateEvent;
import org.allaymc.economyapi.event.AccountDeleteEvent;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main implementation of the EconomyAPI interface for Aconomy.
 * This implementation supports a single currency type configured via config.yml.
 *
 * @author daoge_cmd
 */
@Slf4j
public class AcoEconomyAPI implements EconomyAPI {

    @Getter
    private final Currency defaultCurrency;
    private final Set<Currency> currencies;
    private final EconomyStorage storage;
    private final Map<UUID, AcoAccount> accountCache = new ConcurrentHashMap<>();
    private final BigDecimal defaultBalance;

    public AcoEconomyAPI(Currency defaultCurrency, EconomyStorage storage, BigDecimal defaultBalance) {
        this.defaultCurrency = defaultCurrency;
        this.currencies = Collections.singleton(defaultCurrency);
        this.storage = storage;
        this.defaultBalance = defaultBalance;

        // Load existing accounts into cache
        for (UUID uuid : storage.getAllAccountIds()) {
            accountCache.put(uuid, new AcoAccount(uuid, storage));
        }

        log.info("AcoEconomyAPI initialized with {} accounts", accountCache.size());
    }

    @Override
    public Currency getCurrency(String name) {
        if (defaultCurrency.getName().equalsIgnoreCase(name)) {
            return defaultCurrency;
        }
        return null;
    }

    @Override
    public Set<Currency> getCurrencies() {
        return currencies;
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return storage.hasAccount(uuid);
    }

    @Override
    public Account getOrCreateAccount(UUID uuid) {
        AcoAccount existing = accountCache.get(uuid);
        if (existing != null) {
            return existing;
        }

        // Check if account exists in storage
        if (storage.hasAccount(uuid)) {
            AcoAccount account = new AcoAccount(uuid, storage);
            accountCache.put(uuid, account);
            return account;
        }

        // Create new account
        String name = getPlayerName(uuid);
        AcoAccount newAccount = new AcoAccount(uuid, storage);

        // Fire AccountCreateEvent
        AccountCreateEvent event = new AccountCreateEvent(newAccount);
        if (!event.call(Server.getInstance().getEventBus())) {
            return null;
        }

        // Create account in storage
        storage.createAccount(uuid, name, defaultBalance);
        accountCache.put(uuid, newAccount);

        log.info("Created new account for {} with initial balance {}", name, defaultBalance);
        return newAccount;
    }

    @Override
    public Set<Account> getAccounts() {
        return new HashSet<>(accountCache.values());
    }

    @Override
    public boolean deleteAccount(UUID uuid) {
        AcoAccount account = accountCache.get(uuid);
        if (account == null) {
            return false;
        }

        // Fire AccountDeleteEvent
        AccountDeleteEvent event = new AccountDeleteEvent(account);
        if (!event.call(Server.getInstance().getEventBus())) {
            return false;
        }

        accountCache.remove(uuid);
        boolean deleted = storage.deleteAccount(uuid);

        if (deleted) {
            log.info("Deleted account for UUID {}", uuid);
        }

        return deleted;
    }

    /**
     * Get the storage instance.
     *
     * @return the storage instance
     */
    public EconomyStorage getStorage() {
        return storage;
    }

    /**
     * Get sorted list of accounts by balance (descending).
     *
     * @param limit the maximum number of accounts to return
     * @return sorted list of accounts
     */
    public List<Account> getTopAccounts(int limit) {
        return accountCache.values().stream()
                .sorted((a, b) -> b.getBalance(defaultCurrency).compareTo(a.getBalance(defaultCurrency)))
                .limit(limit)
                .map(a -> (Account) a)
                .toList();
    }

    /**
     * Get a player's name from their UUID using the server's player manager.
     *
     * @param uuid the player's UUID
     * @return the player name, or UUID string if not found
     */
    private String getPlayerName(UUID uuid) {
        var player = Server.getInstance().getPlayerManager().getPlayers().get(uuid);
        if (player != null) {
            return player.getOriginName();
        }
        return uuid.toString();
    }

    /**
     * Update the name for an account (called when player joins).
     *
     * @param uuid the player's UUID
     * @param name the player's name
     */
    public void updateAccountName(UUID uuid, String name) {
        AcoAccount account = accountCache.get(uuid);
        if (account != null) {
            account.setName(name);
        }
    }

    /**
     * Find an account by player name.
     *
     * @param name the player name (case-insensitive)
     * @return the account, or null if not found
     */
    public Account getAccountByName(String name) {
        for (AcoAccount account : accountCache.values()) {
            if (account.getName().equalsIgnoreCase(name)) {
                return account;
            }
        }
        return null;
    }
}
