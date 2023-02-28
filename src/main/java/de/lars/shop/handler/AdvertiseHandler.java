package de.lars.shop.handler;

import de.lars.shop.ShopPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdvertiseHandler {

    private final ShopPlugin plugin;

    private final Map<UUID, Long> nextAdvertises;

    public AdvertiseHandler(final ShopPlugin plugin) {
        this.plugin = plugin;
        this.nextAdvertises = new HashMap<>();
    }

    public boolean allowAdvertise(final UUID uuid) {
        if (!nextAdvertises.containsKey(uuid)) {
            System.out.println("NEGER");
            return true;
        }
        System.out.println("Zeit: " + System.currentTimeMillis() + " >= " + nextAdvertises.get(uuid));
        if (System.currentTimeMillis() >= nextAdvertises.get(uuid)) {
            System.out.println("GENAU");
            nextAdvertises.remove(uuid);
            return true;
        }
        return false;
    }

    public boolean makeAdvertise(final UUID uuid, final String message) {
        if (allowAdvertise(uuid)) {
            String ad_message = plugin.getMessageHandler()
                    .getMessage("advertisement_prefix")
                    .orElse("§l§6ADVERTISEMENT: §r") + message;
            plugin.getServer().getOnlinePlayers().forEach(player -> {
                player.sendMessage(ad_message);
            });
            nextAdvertises.put(uuid, System.currentTimeMillis() + plugin.getConfig().getAdvertisementInterval());
            return true;
        }
        return false;
    }

    public long nextAdvertiseIn(final UUID uuid) {
        if (allowAdvertise(uuid)) return 0L;
        return nextAdvertises.get(uuid) - System.currentTimeMillis();
    }

}
