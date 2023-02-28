package de.lars.shop.listener;

import de.lars.shop.ShopPlugin;
import de.lars.shop.object.Shop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Optional;

public class PlayerListener implements Listener {

    private final ShopPlugin plugin;

    public PlayerListener(final ShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        if (!plugin.getConfig().updateShopNames())
            return;
        Player player = event.getPlayer();
        Optional<Shop> shopOptional = plugin.getShopHandler().getShop(player.getUniqueId());
        if (!shopOptional.isPresent())
            return;
        if (!shopOptional.get().getOwnerName().equals(player.getName())) {
            plugin.getShopHandler().unregisterShop(shopOptional.get());
            shopOptional.get().setOwnerName(player.getName());
            plugin.getShopHandler().registerShop(shopOptional.get());
        }
    }

}
