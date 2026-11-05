package me.grishkka.plcfun;

import io.github.thebusybiscuit.slimefun5.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.implementation.SlimefunItems;
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

        // The custom item for our SlimefunItem
        SlimefunItemStack itemStack = new SlimefunItemStack("LAPEACE", Material.LAPIS_LAZULI, "&a NAHH ITS LAPEACEEEEE", "", "&7This is awesome");

        // A 3x3 shape representing our recipe (use .getItem() for Slimefun items)
        ItemStack[] recipe = {
                new ItemStack(Material.DIAMOND),    null,                               new ItemStack(Material.DIAMOND),
                null,                               new ItemStack(Material.DIAMOND),  null,
                new ItemStack(Material.DIAMOND),    null,                               new ItemStack(Material.DIAMOND)
        };

        SlimefunItem sfItem = new SlimefunItem(itemGroup, itemStack, RecipeType.ENHANCED_CRAFTING_TABLE, recipe);
        sfItem.register(this);
    }

    @Override
    public void onDisable() {
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