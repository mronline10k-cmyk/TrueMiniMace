package com.minimace.mace;

import org.bukkit.plugin.java.JavaPlugin;

public class TrueMiniMace extends JavaPlugin {

    private static TrueMiniMace instance;
    private MiniMaceItem itemManager;
    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        itemManager = new MiniMaceItem(this);
        recipeManager = new RecipeManager(this);

        getServer().getPluginManager().registerEvents(new MaceListener(this), this);
        getCommand("minimace").setExecutor(new MiniMaceCommand(this));

        recipeManager.registerRecipe();

        getLogger().info("TrueMiniMace enabled! Real mace mechanics, half the size.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TrueMiniMace disabled.");
    }

    public MiniMaceItem getItemManager() {
        return itemManager;
    }

    public static TrueMiniMace getInstance() {
        return instance;
    }
}
