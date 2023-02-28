package de.lars.shop.commands;

import de.lars.shop.ShopPlugin;
import de.lars.shop.object.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ShopCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public ShopCommand(final ShopPlugin plugin) {
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
        if (!player.hasPermission("shops.teleport") && !player.isOp()) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("no_permission")
                    .orElse("§cYou have no authorization to do so."));
            return true;
        }
        if (args.length == 0) {
            Optional<Shop> shop = plugin.getShopHandler().getShop(player.getUniqueId());
            if (!shop.isPresent()) {
                player.sendMessage(plugin.getMessageHandler()
                        .getMessage("dont_have_a_shop")
                        .orElse("§cYou do not currently own a store."));
                return true;
            }
            player.teleport(shop.get().getLocation());
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("teleported_to_shop")
                    .orElse("§aYou have been teleported to the shop."));
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("unknown_command")
                    .orElse("§cUnknown command. Please use: §f") + " /shop <NAME>");
            return true;
        }
        Optional<Shop> shop = plugin.getShopHandler().getShop(args[0]);
        if (!shop.isPresent()) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("no_such_shop")
                    .orElse("§cNo shop with this name could be found."));
            return true;
        }
        player.teleport(shop.get().getLocation());
        player.sendMessage(plugin.getMessageHandler()
                .getMessage("teleported_to_shop")
                .orElse("§aYou have been teleported to the shop."));
        return true;
    }

}
