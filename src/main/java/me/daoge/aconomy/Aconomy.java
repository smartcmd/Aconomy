package me.daoge.aconomy;

import lombok.Getter;
import lombok.SneakyThrows;
import me.daoge.aconomy.api.AcoCurrency;
import me.daoge.aconomy.api.AcoEconomyAPI;
import me.daoge.aconomy.command.AcoCommand;
import me.daoge.aconomy.storage.EconomyStorage;
import me.daoge.aconomy.storage.H2Storage;
import me.daoge.aconomy.storage.JsonStorage;
import me.daoge.aconomy.storage.SqliteStorage;
import org.allaymc.api.eventbus.EventBus;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.plugin.Plugin;
import org.allaymc.api.plugin.PluginException;
import org.allaymc.api.registry.Registries;
import org.allaymc.api.server.Server;
import org.allaymc.api.eventbus.event.server.PlayerJoinEvent;
import org.allaymc.api.utils.config.Config;
import org.allaymc.api.utils.config.ConfigSection;
import org.allaymc.economyapi.EconomyAPI;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Aconomy - A simple economy plugin for AllayMC that implements EconomyAPI.
 *
 * @author daoge_cmd
 */
public class Aconomy extends Plugin {

    @Getter
    private static Aconomy instance;

    @Getter
    private Config config;

    private EconomyStorage storage;
    private AcoEconomyAPI economyAPI;
    private EventListener eventListener;

    @Override
    public void onLoad() {
        instance = this;
        this.pluginLogger.info("Aconomy is loading...");
        loadConfig();
        // Initialize storage based on config
        String storageType = config.getString("storage.type", "json").toLowerCase(Locale.ROOT);
        Path dataFolder = this.pluginContainer.dataFolder();

        switch (storageType) {
            case "sqlite" -> {
                storage = new SqliteStorage(dataFolder);
                this.pluginLogger.info("Using SQLite storage");
            }
            case "h2" -> {
                storage = new H2Storage(dataFolder);
                this.pluginLogger.info("Using H2 storage");
            }
            default -> {
                storage = new JsonStorage(dataFolder);
                this.pluginLogger.info("Using JSON storage");
            }
        }
        storage.init();

        // Load currency configuration
        ConfigSection currencySection = config.getSection("currency");
        String currencyName = currencySection.getString("name", "Coin");
        String currencyPlural = currencySection.getString("plural", "Coins");
        String currencySymbol = currencySection.getString("symbol", "$");
        int fractionDigits = currencySection.getInt("fraction_digits", 2);

        // Load default balance
        BigDecimal defaultBalance = BigDecimal.valueOf(config.getDouble("economy.default_balance", 0.0));

        // Create currency
        AcoCurrency currency = new AcoCurrency(currencyName, currencyPlural, currencySymbol, fractionDigits, true);

        // Create and register EconomyAPI implementation
        economyAPI = new AcoEconomyAPI(currency, storage, defaultBalance);
        EconomyAPI.API.set(economyAPI);

        this.pluginLogger.info("EconomyAPI implementation registered successfully!");
        this.pluginLogger.info("Aconomy loaded successfully!");
    }

    @Override
    public void onEnable() {
        this.pluginLogger.info("Aconomy is enabling...");

        // Register commands
        Registries.COMMANDS.register(new AcoCommand());
        this.pluginLogger.info("Registered command: /aconomy (alias: /aco)");

        // Register event listener
        EventBus eventBus = Server.getInstance().getEventBus();
        eventListener = new EventListener();
        eventBus.registerListener(eventListener);

        this.pluginLogger.info("Aconomy enabled successfully!");
    }

    @Override
    public void onDisable() {
        this.pluginLogger.info("Aconomy is disabling...");

        // Unregister event listener
        if (eventListener != null) {
            Server.getInstance().getEventBus().unregisterListener(eventListener);
        }

        // Shutdown storage
        if (storage != null) {
            storage.shutdown();
        }

        this.pluginLogger.info("Aconomy disabled successfully!");
    }

    /**
     * Load configuration from config.yml.
     * If config file doesn't exist, it will be created with default values from resources.
     */
    @SneakyThrows
    private void loadConfig() {
        var configFile = this.pluginContainer.dataFolder().resolve("config.yml");

        // If config file doesn't exist, create it from resources first (to preserve comments)
        if (!Files.exists(configFile)) {
            try (InputStream defaultConfigStream = this.getClass().getResourceAsStream("/config.yml")) {
                if (defaultConfigStream != null) {
                    Files.copy(defaultConfigStream, configFile);
                    this.pluginLogger.info("Created default config.yml");
                }
            }
        }

        // Load config
        config = new Config(configFile.toFile(), Config.YAML);

        this.pluginLogger.info("Configuration loaded successfully!");
    }

    /**
     * Event listener for player events.
     */
    public class EventListener {

        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            var player = event.getPlayer();
            var uuid = player.getLoginData().getUuid();
            var name = player.getOriginName();

            // Ensure account exists and update name
            economyAPI.getOrCreateAccount(uuid);
            economyAPI.updateAccountName(uuid, name);
        }
    }
}
