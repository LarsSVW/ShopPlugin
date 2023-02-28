package de.lars.shop.commands;

import de.lars.shop.ShopPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopsCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public ShopsCommand(final ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessageHandler()
                    .getMessage("not_a_player")
                    .orElse("§cOnly players can execute this command."));
            return true;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("shops.list") && !player.isOp()) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("no_permission")
                    .orElse("§cYou have no authorization to do so."));
            return true;
        }
        if (args.length != 0) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("unknown_command")
                    .orElse("§cUnknown command. Please use: §f") + " /shops");
            return true;
        }
        plugin.getShopGuiService().showGui(player);
        return true;
    }

}
