package de.lars.shop.commands;

import de.lars.shop.ShopPlugin;
import de.lars.shop.object.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class DelShopCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public DelShopCommand(final ShopPlugin plugin) {
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
        if (!player.hasPermission("shops.delete") && !player.isOp()) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("no_permission")
                    .orElse("§cYou have no authorization to do so."));
            return true;
        }
        if (args.length != 0) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("unknown_command")
                    .orElse("§cUnknown command. Please use: §f") + " /delshop");
            return true;
        }
        Optional<Shop> shop = plugin.getShopHandler().getShop(player.getUniqueId());
        if (!shop.isPresent()) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("dont_have_a_shop")
                    .orElse("§cYou do not currently own a store."));
            return true;
        }
        plugin.getShopHandler().unregisterShop(shop.get());
        player.sendMessage(plugin.getMessageHandler()
                .getMessage("shop_was_deleted")
                .orElse("§aYour shop has been successfully deleted."));
        return true;
    }

}
