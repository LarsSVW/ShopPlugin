package de.lars.shop.object;

import org.bukkit.Location;

import java.util.UUID;

public class Shop {

    private final UUID owner;
    private String ownerName;

    private Location location;

    public Shop(final UUID owner, final String ownerName, final Location location) {
        this.owner = owner;
        this.ownerName = ownerName;
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(final String ownerName) {
        this.ownerName = ownerName;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

}
