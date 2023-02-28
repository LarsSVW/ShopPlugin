package de.lars.shop.handler;

import de.lars.shop.ShopPlugin;
import de.lars.shop.config.Configuration;
import de.lars.shop.object.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopHandler {

    private final ShopPlugin plugin;
    private final Map<UUID, Shop> shops;

    private final Configuration shopConfig;

    public ShopHandler(final ShopPlugin plugin) {
        this.plugin = plugin;
        this.shops = new HashMap<>();
        this.shopConfig = new Configuration(new File(plugin.getDataFolder(), "shops.yml"));


        reloadShops();
    }

    public void reloadShops() {
        shops.clear();
        if (!shopConfig.getFile().exists()) {
            try {
                shopConfig.getFile().createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        shopConfig.load();
        for (String sectionKey : shopConfig.getKeys(false)) {
            ConfigurationSection section = shopConfig.getConfigurationSection(sectionKey);
            World world = Bukkit.getWorld(section.getString("world", "world"));
            if (world == null) continue;
            Location shopLocation = new Location(world,
                    section.getDouble("locX", 0.0D),
                    section.getDouble("locY", 100.0D),
                    section.getDouble("locZ", 0.0D),
                    (float) section.getDouble("yaw", 0.0D),
                    (float) section.getDouble("pitch", 0.0D));
            this.shops.put(UUID.fromString(sectionKey),
                    new Shop(UUID.fromString(sectionKey), section.getString("owner", "Unknown"), shopLocation));
        }
    }

    public void registerShop(final Shop shop) {
        shops.put(shop.getOwner(), shop);
        //ConfigurationSection section = shopConfig.getConfigurationSection(shop.getOwner().toString());
        ConfigurationSection section = shopConfig.createSection(shop.getOwner().toString());
        section.set("ownerName", shop.getOwnerName());
        section.set("world", shop.getLocation().getWorld().getName());
        section.set("locX", shop.getLocation().getX());
        section.set("locY", shop.getLocation().getY());
        section.set("locZ", shop.getLocation().getZ());
        section.set("yaw", shop.getLocation().getYaw());
        section.set("pitch", shop.getLocation().getPitch());

        plugin.getShopGuiService().updateGui();

        shopConfig.save();
    }

    public boolean unregisterShop(final Shop shop) {
        if (shops.containsKey(shop.getOwner())) {
            shops.remove(shop.getOwner());
            plugin.getShopGuiService().updateGui();
            shopConfig.set(shop.getOwner().toString(), null);
            shopConfig.save();
            return true;
        }
        return false;
    }

    public Optional<Shop> getShop(final UUID uuid) {
        return Optional.ofNullable(shops.get(uuid));
    }

    public Optional<Shop> getShop(final String ownerName) {
        for (Shop shop : shops.values()) {
            if (shop.getOwnerName().equalsIgnoreCase(ownerName)) {
                return Optional.of(shop);
            }
        }
        return Optional.empty();
    }

    public Collection<Shop> getShops() {
        return shops.values();
    }

    public List<String> getShopNames() {
        List<String> names = new ArrayList<>();
        shops.forEach((key, value) -> {
            names.add(value.getOwnerName());
        });
        return names;
    }

}
