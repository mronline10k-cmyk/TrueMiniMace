package com.minimace.mace;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;

public class RecipeManager {

    private final TrueMiniMace plugin;
    private final NamespacedKey recipeKey;

    public RecipeManager(TrueMiniMace plugin) {
        this.plugin = plugin;
        this.recipeKey = new NamespacedKey(plugin, "minimace");
    }

    public void registerRecipe() {
        if (!plugin.getConfig().getBoolean("crafting.enabled", true)) return;

        // Remove old recipe if exists
        Bukkit.removeRecipe(recipeKey);

        ItemStack result = plugin.getItemManager().create();
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);

        List<List<String>> recipeConfig = (List<List<String>>) plugin.getConfig().getList("crafting.recipe");
        if (recipeConfig == null || recipeConfig.size() != 3) {
            plugin.getLogger().warning("Invalid crafting recipe config. Using default.");
            recipe.shape("BBB", "HCH", "BBB");
            recipe.setIngredient('B', Material.BREEZE_ROD);
            recipe.setIngredient('H', Material.HEAVY_CORE);
            recipe.setIngredient('C', Material.IRON_INGOT);
        } else {
            recipe.shape("ABC", "DEF", "GHI");
            char[] slots = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
            int idx = 0;
            for (List<String> row : recipeConfig) {
                for (String mat : row) {
                    Material material = Material.matchMaterial(mat);
                    if (material != null) {
                        recipe.setIngredient(slots[idx], material);
                    }
                    idx++;
                }
            }
        }

        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("MiniMace crafting recipe registered.");
    }
}
