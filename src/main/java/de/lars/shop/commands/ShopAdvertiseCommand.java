package de.lars.shop.commands;

import de.lars.shop.ShopPlugin;
import de.lars.shop.object.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

public class ShopAdvertiseCommand implements CommandExecutor {

    private final ShopPlugin plugin;

    public ShopAdvertiseCommand(final ShopPlugin plugin) {
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
        if (!player.hasPermission("shops.advertise") && !player.isOp()) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("no_permission")
                    .orElse("§cYou have no authorization to do so."));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("unknown_command")
                    .orElse("§cUnknown command. Please use: §f") + " /shopadvertise <ADVERTISEMENT>");
            return true;
        }
        Optional<Shop> shop = plugin.getShopHandler().getShop(player.getUniqueId());
        if (!shop.isPresent()) {
            player.sendMessage(plugin.getMessageHandler()
                    .getMessage("dont_have_a_shop")
                    .orElse("§cYou do not currently own a store."));
            return true;
        }
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            builder.append(arg).append(" ");
        }
        if (!plugin.getAdvertiseHandler().makeAdvertise(player.getUniqueId(), builder.toString())) {
            StringBuilder builder2 = new StringBuilder();
            builder2.append(plugin.getMessageHandler()
                            .getMessage("advertisement_timer")
                            .orElse("§cYou can't advertise yet. Please wait: §f"))
                    .append(" ")
                    .append(plugin.getAdvertiseHandler().nextAdvertiseIn(player.getUniqueId()))
                    .append(" ")
                    .append(plugin.getMessageHandler()
                            .getMessage("seconds")
                            .orElse("second(s)"));
            player.sendMessage(builder2.toString());
            return true;
        }
        player.sendMessage(plugin.getMessageHandler()
                .getMessage("advertisement_sent")
                .orElse("§aYour advertisement has been successfully placed."));
        return true;
    }

}
