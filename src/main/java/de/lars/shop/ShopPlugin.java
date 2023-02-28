package de.lars.shop;

import de.lars.shop.commands.*;
import de.lars.shop.handler.AdvertiseHandler;
import de.lars.shop.handler.MessageHandler;
import de.lars.shop.handler.ShopHandler;
import de.lars.shop.listener.PlayerListener;
import de.lars.shop.service.ShopGuiService;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopPlugin extends JavaPlugin {

    private ShopHandler shopHandler;
    private MessageHandler messageHandler;
    private AdvertiseHandler advertiseHandler;

    private ShopGuiService shopGuiService;

    private PluginConfiguration config;

    @Override
    public void onEnable() {
        config = new PluginConfiguration(this);
        messageHandler = new MessageHandler(this);
        shopHandler = new ShopHandler(this);
        advertiseHandler = new AdvertiseHandler(this);

        shopGuiService = new ShopGuiService(this);

        registerEvents();

        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    private void registerCommands() {
        getCommand("delshop").setExecutor(new DelShopCommand(this));
        getCommand("setshop").setExecutor(new SetShopCommand(this));
        getCommand("shopadvertise").setExecutor(new ShopAdvertiseCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("shops").setExecutor(new ShopsCommand(this));
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    @Override
    public PluginConfiguration getConfig() {
        return config;
    }

    public ShopHandler getShopHandler() {
        return this.shopHandler;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public AdvertiseHandler getAdvertiseHandler() {
        return advertiseHandler;
    }

    public ShopGuiService getShopGuiService() {
        return shopGuiService;
    }

}
