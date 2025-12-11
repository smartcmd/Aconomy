package me.daoge.aconomy;

import org.allaymc.api.plugin.Plugin;

public class Aconomy extends Plugin {
    @Override
    public void onLoad() {
        this.pluginLogger.info("Aconomy is loaded!");
    }

    @Override
    public void onEnable() {
        this.pluginLogger.info("Aconomy is enabled!");
    }

    @Override
    public void onDisable() {
        this.pluginLogger.info("Aconomy is disabled!");
    }
}