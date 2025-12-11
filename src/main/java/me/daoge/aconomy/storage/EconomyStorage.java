package me.daoge.aconomy.storage;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Storage interface for economy data persistence.
 *
 * @author daoge_cmd
 */
public interface EconomyStorage {

    /**
     * Initialize the storage system.
     *
     * @return true if initialization was successful
     */
    boolean init();

    /**
     * Shutdown the storage system and release resources.
     */
    void shutdown();

    /**
     * Check if an account exists for the given UUID.
     *
     * @param uuid the unique identifier of the account
     * @return true if the account exists
     */
    boolean hasAccount(UUID uuid);

    /**
     * Get the balance for an account.
     *
     * @param uuid the unique identifier of the account
     * @return the balance, or BigDecimal.ZERO if the account doesn't exist
     */
    BigDecimal getBalance(UUID uuid);

    /**
     * Set the balance for an account.
     *
     * @param uuid    the unique identifier of the account
     * @param balance the new balance
     */
    void setBalance(UUID uuid, BigDecimal balance);

    /**
     * Get the name associated with an account.
     *
     * @param uuid the unique identifier of the account
     * @return the account name, or null if the account doesn't exist
     */
    String getAccountName(UUID uuid);

    /**
     * Set the name associated with an account.
     *
     * @param uuid the unique identifier of the account
     * @param name the account name
     */
    void setAccountName(UUID uuid, String name);

    /**
     * Create a new account with the given UUID and name.
     *
     * @param uuid           the unique identifier of the account
     * @param name           the account name
     * @param initialBalance the initial balance
     * @return true if the account was created successfully
     */
    boolean createAccount(UUID uuid, String name, BigDecimal initialBalance);

    /**
     * Delete an account.
     *
     * @param uuid the unique identifier of the account to delete
     * @return true if the account was deleted
     */
    boolean deleteAccount(UUID uuid);

    /**
     * Get all account UUIDs and their balances.
     *
     * @return a map of UUID to balance
     */
    Map<UUID, BigDecimal> getAllBalances();

    /**
     * Get all account UUIDs.
     *
     * @return a set of all account UUIDs
     */
    java.util.Set<UUID> getAllAccountIds();

    /**
     * Save any pending changes to persistent storage.
     */
    void save();
}
