package de.lars.shop.handler;

import de.lars.shop.ShopPlugin;
import de.lars.shop.config.Configuration;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MessageHandler {

    private final ShopPlugin plugin;
    private final Map<String, String> messages;

    private final Configuration messageConfig;

    public MessageHandler(final ShopPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.messageConfig = new Configuration(new File(plugin.getDataFolder(), "messages.yml"));
        this.messageConfig.setTemplateName("/messages.yml");

        reloadMessages();
    }

    public void reloadMessages() {
        messages.clear();
        messageConfig.load();
        for (String key : messageConfig.getKeys(false)) {
            messages.put(key.toLowerCase(),
                    ChatColor.translateAlternateColorCodes('&', messageConfig.getString(key)));
        }
    }

    public Optional<String> getMessage(final String key) {
        return Optional.ofNullable(messages.get(key.toLowerCase()));
    }

}
