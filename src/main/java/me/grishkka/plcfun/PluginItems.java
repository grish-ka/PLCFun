package me.grishkka.plcfun;

import io.github.thebusybiscuit.slimefun5.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginItems {

    public static ItemGroup plcCategory;

    public static void setup(JavaPlugin plugin) {
        SlimefunAddon addon = (SlimefunAddon) plugin;

        // 1. Create the Custom Category / ItemGroup
        NamespacedKey categoryId = new NamespacedKey(plugin, "plc_category");

        ItemStack categoryItem = new ItemStack(Material.DIAMOND);
        ItemMeta meta = categoryItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§bPLCFun Automation");
            categoryItem.setItemMeta(meta);
        }

        plcCategory = new ItemGroup(categoryId, categoryItem);

        // --- 2. Lapeace Item ---
        SlimefunItemStack lapeaceStack = new SlimefunItemStack(
                "LAPEACE",
                Material.LAPIS_LAZULI,
                "&a NAHH ITS LAPEACEEEEE",
                "",
                "&7This is awesome"
        );
        ItemStack[] lapeaceRecipe = {
                new ItemStack(Material.DIAMOND),    null,                           new ItemStack(Material.DIAMOND),
                null,                               new ItemStack(Material.DIAMOND),null,
                new ItemStack(Material.DIAMOND),    null,                           new ItemStack(Material.DIAMOND)
        };
        new SlimefunItem(plcCategory, lapeaceStack, RecipeType.ENHANCED_CRAFTING_TABLE, lapeaceRecipe).register(addon);


        // --- 3. Data Cable Item ---
        SlimefunItemStack cableStack = new SlimefunItemStack(
                "PLC_DATA_CABLE",
                Material.STRING,
                "&bData Cable",
                "",
                "&7Used to link PLC components together."
        );
        ItemStack[] cableRecipe = {
                null,                               new ItemStack(Material.IRON_NUGGET), null,
                new ItemStack(Material.IRON_NUGGET),new ItemStack(Material.LAPIS_LAZULI),new ItemStack(Material.IRON_NUGGET),
                null,                               new ItemStack(Material.IRON_NUGGET), null
        };
        SlimefunItem cableItem = new SlimefunItem(plcCategory, cableStack, RecipeType.ENHANCED_CRAFTING_TABLE, cableRecipe);
        cableItem.register(addon);


        // --- 4. Switch Item (Using a Lever) ---
        SlimefunItemStack switchStack = new SlimefunItemStack(
                "PLC_SWITCH",
                Material.LEVER,
                "&ePLC Switch",
                "",
                "&7Toggles connected circuits on and off."
        );
        ItemStack[] switchRecipe = {
                new ItemStack(Material.STONE),      new ItemStack(Material.REDSTONE),   new ItemStack(Material.STONE),
                null,                               cableItem.getItem(),                null, // Use cableItem.getItem() here!
                null,                               new ItemStack(Material.IRON_INGOT), null
        };
        new SlimefunItem(plcCategory, switchStack, RecipeType.ENHANCED_CRAFTING_TABLE, switchRecipe).register(addon);


        // --- 5. Lightbulb Item (Using a Copper Bulb) ---
        SlimefunItemStack lightbulbStack = new SlimefunItemStack(
                "PLC_LIGHTBULB",
                Material.COPPER_BULB,
                "&fPLC Lightbulb",
                "",
                "&7Lights up when connected to an active switch."
        );
        ItemStack[] lightbulbRecipe = {
                null,                               new ItemStack(Material.GLASS),      null,
                null,                               cableItem.getItem(),                null, // Use cableItem.getItem() here too!
                null,                               new ItemStack(Material.REDSTONE),   null
        };
        new SlimefunItem(plcCategory, lightbulbStack, RecipeType.ENHANCED_CRAFTING_TABLE, lightbulbRecipe).register(addon);

        plugin.getLogger().info("Registered all PLCFun items including Copper Bulb and Lever switch via PluginItems registry!");
    }
}