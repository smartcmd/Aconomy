package me.daoge.aconomy.api;

import lombok.Getter;
import org.allaymc.economyapi.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of the Currency interface for Aconomy.
 *
 * @author daoge_cmd
 */
@Getter
public class AcoCurrency implements Currency {

    private final String name;
    private final String pluralName;
    private final String symbol;
    private final int defaultFractionDigits;
    private final boolean isDefault;

    public AcoCurrency(String name, String pluralName, String symbol, int defaultFractionDigits, boolean isDefault) {
        this.name = name;
        this.pluralName = pluralName;
        this.symbol = symbol;
        this.defaultFractionDigits = defaultFractionDigits;
        this.isDefault = isDefault;
    }

    @Override
    public String format(BigDecimal amount, int numFractionDigits) {
        BigDecimal rounded = amount.setScale(numFractionDigits, RoundingMode.HALF_UP);
        return symbol + rounded.toPlainString();
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }
}
