package me.daoge.aconomy.api;

import lombok.Getter;
import me.daoge.aconomy.storage.EconomyStorage;
import org.allaymc.api.server.Server;
import org.allaymc.economyapi.Account;
import org.allaymc.economyapi.Currency;
import org.allaymc.economyapi.EconomyAPI;
import org.allaymc.economyapi.event.BalanceChangeEvent;
import org.allaymc.economyapi.event.BalanceTransferEvent;
import org.jetbrains.annotations.Unmodifiable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the Account interface for Aconomy.
 * This implementation supports only a single currency type.
 *
 * @author daoge_cmd
 */
public class AcoAccount implements Account {

    @Getter
    private final UUID uniqueId;
    private final EconomyStorage storage;

    public AcoAccount(UUID uniqueId, EconomyStorage storage) {
        this.uniqueId = uniqueId;
        this.storage = storage;
    }

    @Override
    public String getName() {
        String name = storage.getAccountName(uniqueId);
        return name != null ? name : uniqueId.toString();
    }

    @Override
    public BigDecimal getBalance(Currency currency) {
        // Since we only support a single currency, we ignore the currency parameter
        return storage.getBalance(uniqueId);
    }

    @Override
    @Unmodifiable
    public Map<Currency, BigDecimal> getBalances() {
        Currency defaultCurrency = EconomyAPI.getAPI().getDefaultCurrency();
        Map<Currency, BigDecimal> balances = new HashMap<>();
        balances.put(defaultCurrency, storage.getBalance(uniqueId));
        return Collections.unmodifiableMap(balances);
    }

    @Override
    public boolean setBalance(Currency currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        BigDecimal oldBalance = storage.getBalance(uniqueId);

        // Fire BalanceChangeEvent
        BalanceChangeEvent event = new BalanceChangeEvent(this, currency, oldBalance, amount);
        if (!event.call(Server.getInstance().getEventBus())) {
            return false;
        }

        storage.setBalance(uniqueId, amount);
        return true;
    }

    @Override
    public boolean transfer(Account to, Currency currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal currentBalance = getBalance(currency);
        if (currentBalance.compareTo(amount) < 0) {
            return false;
        }

        // Fire BalanceTransferEvent
        BalanceTransferEvent event = new BalanceTransferEvent(this, to, currency, amount);
        if (!event.call(Server.getInstance().getEventBus())) {
            return false;
        }

        // Perform the transfer
        BigDecimal newFromBalance = currentBalance.subtract(amount);
        BigDecimal newToBalance = to.getBalance(currency).add(amount);

        storage.setBalance(uniqueId, newFromBalance);
        if (to instanceof AcoAccount acoTo) {
            acoTo.storage.setBalance(acoTo.uniqueId, newToBalance);
        } else {
            to.setBalance(currency, newToBalance);
        }

        return true;
    }

    /**
     * Update the account name in storage.
     *
     * @param name the new name
     */
    public void setName(String name) {
        storage.setAccountName(uniqueId, name);
    }
}
