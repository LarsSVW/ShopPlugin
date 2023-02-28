package de.lars.shop;

import de.lars.shop.config.Configuration;

import java.io.File;

public class PluginConfiguration extends Configuration {

    private final ShopPlugin plugin;

    private boolean updateShopNames;
    private int advertisementInterval;

    public PluginConfiguration(final ShopPlugin plugin) {
        super(new File(plugin.getDataFolder(), "config.yml"));
        this.plugin = plugin;
        setTemplateName("/config.yml");

        reloadConfig();
    }

    public void reloadConfig() {
        load();
        updateShopNames = getBoolean("update_shop_names", false);
        advertisementInterval = (getInt("advertisement_interval", 10) * 60 * 1000); // Store as millis
    }

    public boolean updateShopNames() {
        return this.updateShopNames;
    }

    public int getAdvertisementInterval() {
        return advertisementInterval;
    }

}
