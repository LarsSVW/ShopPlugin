package de.lars.shop.commands;

import de.lars.shop.ShopPlugin;
import de.lars.shop.object.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class SetShopCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public SetShopCommand(final ShopPlugin plugin) {
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
        if (!player.hasPermission("shops.set") && !player.isOp()) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("no_permission")
                    .orElse("§cYou have no authorization to do so."));
            return true;
        }
        if (args.length != 0) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("unknown_command")
                    .orElse("§cUnknown command. Please use: §f") + " /setshop");
            return true;
        }
        Optional<Shop> shop = plugin.getShopHandler().getShop(player.getUniqueId());
        if (!shop.isPresent()) {
            plugin.getShopHandler().registerShop(new Shop(player.getUniqueId(), player.getName(), player.getLocation()));
        } else {
            plugin.getShopHandler().unregisterShop(shop.get());
            shop.get().setLocation(player.getLocation());
            plugin.getShopHandler().registerShop(shop.get());
        }
        player.sendMessage(plugin.getMessageHandler()
                .getMessage("shop_was_set")
                .orElse("§aYour shop was successfully set."));
        return true;
    }

}
