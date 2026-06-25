package com.minimace.mace;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class MiniMaceItem {

    private final TrueMiniMace plugin;
    private final NamespacedKey maceKey;

    public MiniMaceItem(TrueMiniMace plugin) {
        this.plugin = plugin;
        this.maceKey = new NamespacedKey(plugin, "minimace");
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        // Custom name with color codes
        String name = plugin.getConfig().getString("item.name", "&4&lMini Mace");
        meta.displayName(Component.text(name.replace('&', '§')).decoration(TextDecoration.ITALIC, false));

        // Lore
        List<String> loreStrings = plugin.getConfig().getStringList("item.lore");
        if (!loreStrings.isEmpty()) {
            List<Component> lore = loreStrings.stream()
                .map(s -> Component.text(s.replace('&', '§')))
                .toList();
            meta.lore(lore);
        }

        // Mark as MiniMace so listeners can detect it
        meta.getPersistentDataContainer().set(maceKey, PersistentDataType.BYTE, (byte) 1);

        // Attributes - base damage and attack speed
        double baseDamage = plugin.getConfig().getDouble("item.base-damage", 6.0);
        double attackSpeed = plugin.getConfig().getDouble("item.attack-speed", 1.6);

        // Add generic attack damage attribute
        meta.addAttributeModifier(
            Attribute.GENERIC_ATTACK_DAMAGE,
            new AttributeModifier(
                new UUID(0, 1),
                "minimace_damage",
                baseDamage - 1, // subtract 1 because base fist damage is 1
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            )
        );

        // Add attack speed attribute
        meta.addAttributeModifier(
            Attribute.GENERIC_ATTACK_SPEED,
            new AttributeModifier(
                new UUID(0, 2),
                "minimace_speed",
                attackSpeed - 4.0, // base is 4.0, so we add the difference
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.MAINHAND
            )
        );

        // Hide attributes so lore is clean
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // Set custom model data for resource pack (1000001 = MiniMace model)
        meta.setCustomModelData(1000001);

        // Durability
        int durability = plugin.getConfig().getInt("item.durability", 250);
        item.setType(Material.DIAMOND_SWORD); // diamond sword has 1561 durability by default
        // We can't actually set max durability on custom items easily without NMS
        // But we can use damage to represent used durability
        // For simplicity, we'll just set the item type to IRON_SWORD (250 durability)
        item.setType(Material.IRON_SWORD);
        item.setItemMeta(meta);

        return item;
    }

    public boolean isMiniMace(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(maceKey, PersistentDataType.BYTE);
    }

    public NamespacedKey getMaceKey() {
        return maceKey;
    }
}
