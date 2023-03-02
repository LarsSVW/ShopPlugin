package de.lars.shop.service;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import de.lars.shop.ShopPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGuiService {

    private final ShopPlugin plugin;

    private ChestGui shopList;

    public ShopGuiService(final ShopPlugin plugin) {
        this.plugin = plugin;
        updateGui();
    }

    public void updateGui() {
        ChestGui gui = new ChestGui(6, "Shop");

        List<GuiItem> shops = new ArrayList<>();
        plugin.getShopHandler().getShops().forEach(shop -> {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(plugin.getServer().getOfflinePlayer(shop.getOwner()));
            meta.setDisplayName(shop.getOwnerName());
            item.setItemMeta(meta);
            shops.add(new GuiItem(item, event -> {
                event.getWhoClicked().teleport(shop.getLocation());
                event.getWhoClicked().sendMessage(plugin.getMessageHandler()
                        .getMessage("teleported_to_shop")
                        .orElse("§aYou have been teleported to the shop."));
            }));
        });

        PaginatedPane pages = new PaginatedPane(0, 0, 9, 5);
        pages.populateWithGuiItems(shops); //TODO: Fill with the list

        gui.addPane(pages);

        OutlinePane background = new OutlinePane(0, 5, 9, 1);
        background.addItem(new GuiItem(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);

        gui.addPane(background);

        StaticPane navigation = new StaticPane(0, 5, 9, 1);
        ItemStack previous = new ItemStack(Material.RED_WOOL);
        ItemMeta previousMeta = previous.getItemMeta();
        previousMeta.setDisplayName("-");
        previous.setItemMeta(previousMeta);
        navigation.addItem(new GuiItem(previous, event -> {
            event.setCancelled(true);
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);

                gui.update();
            }
        }), 0, 0);
        ItemStack next = new ItemStack(Material.GREEN_WOOL);
        ItemMeta nextMeta = next.getItemMeta();
        nextMeta.setDisplayName("+");
        next.setItemMeta(nextMeta);
        navigation.addItem(new GuiItem(next, event -> {
            event.setCancelled(true);
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);

                gui.update();
            }
        }), 8, 0);
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§r§cClose");
        close.setItemMeta(closeMeta);
        navigation.addItem(new GuiItem(close, event ->
                event.getWhoClicked().closeInventory()), 4, 0);

        gui.addPane(navigation);

        shopList = gui;
    }

    public void showGui(final Player player) {
        shopList.copy().show(player);
    }

}
