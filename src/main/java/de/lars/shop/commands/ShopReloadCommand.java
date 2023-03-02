package de.lars.shop.commands;

import de.lars.shop.ShopPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class ShopReloadCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public ShopReloadCommand(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("shops.reload") && sender.isOp()) {

            plugin.getConfig().reloadConfig();
            plugin.getMessageHandler().reloadMessages();
            plugin.getShopHandler().reloadShops();
            plugin.getShopGuiService().updateGui();
            sender.sendMessage(plugin.getMessageHandler().getMessage("reload_message").orElse("§aPlugin reloaded"));
        }else{
            sender.sendMessage(plugin.getMessageHandler().getMessage("no_permission").orElse("§cYou have no authorization to do so."));
        }
        return false;
    }
}
