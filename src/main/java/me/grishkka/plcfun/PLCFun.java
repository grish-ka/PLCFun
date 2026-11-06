package me.grishkka.plcfun;

import io.github.thebusybiscuit.slimefun5.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class PLCFun extends JavaPlugin implements SlimefunAddon {

    @Override
    public void onEnable() {
        Config cfg = new Config(this);

        printLogo();

        // Use Slimefun 5's NamespacedKey
        NamespacedKey categoryId = new NamespacedKey(this, "cool_category");

        // Use standard Bukkit ItemStack with ItemMeta for compatibility
        ItemStack categoryItem = new ItemStack(Material.DIAMOND);
        ItemMeta categoryMeta = categoryItem.getItemMeta();
        if (categoryMeta != null) {
            categoryMeta.setDisplayName("§4Our very cool Category");
            categoryItem.setItemMeta(categoryMeta);
        }

        // Our custom Category
        ItemGroup itemGroup = new ItemGroup(categoryId, categoryItem);

        PluginItems.setup(this);
    }

    private void printLogo() {
        // Each getLogger().info() prints a clean line to the server console
        getLogger().info("  _____  _      _____ ______          ");
        getLogger().info(" |  __ \\| |    / ____|  ____|         ");
        getLogger().info(" | |__) | |   | |    | |__ _   _ _ __ ");
        getLogger().info(" |  ___/| |   | |    |  __| | | | '_ \\");
        getLogger().info(" | |    | |___| |____| |  | |_| | | | |");
        getLogger().info(" |_|    |______\\_____|_|   \\__,_|_| |_|");
        getLogger().info(" >> PLCFun Automation Addon Loaded Successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().warning("I AM BEING DISABLED AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH!");
    }

    @Override
    public String getBugTrackerURL() {
        return null;
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }
}