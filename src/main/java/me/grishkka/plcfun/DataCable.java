package me.grishkka.plcfun;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.handlers.ItemUseHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class DataCable extends SlimefunItem {

    private final NamespacedKey targetKey;

    public DataCable(JavaPlugin plugin, ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.targetKey = new NamespacedKey(plugin, "linked_bulb_loc");
    }

    @Override
    public void preRegister() {
        ItemUseHandler itemUseHandler = this::onCableRightClick;
        addItemHandler(itemUseHandler);
    }

    private void onCableRightClick( io.github.thebusybiscuit.slimefun5.api.events.PlayerRightClickEvent event) {
        Optional<Block> clickedBlockOpt = event.getClickedBlock();
        if (clickedBlockOpt.isEmpty()) {
            return;
        }

        // Cancel default string placement when clicking blocks
        event.cancel();

        Block block = clickedBlockOpt.get();
        Player player = event.getPlayer();
        ItemStack cable = event.getItem();

        String sfId = BlockStorage.checkID(block);
        if (sfId == null) {
            player.sendMessage(ChatColor.RED + "[PLCFun] That block is not a PLC component!");
            return;
        }

        // Step 1: Right-click a PLC Lightbulb to select it
        if (sfId.equals("PLC_LIGHTBULB")) {
            ItemMeta meta = cable.getItemMeta();
            if (meta == null) return;

            String locString = serializeLocation(block.getLocation());
            meta.getPersistentDataContainer().set(targetKey, PersistentDataType.STRING, locString);
            cable.setItemMeta(meta);

            player.sendMessage(ChatColor.AQUA + "[PLCFun] Lightbulb selected at ("
                    + block.getX() + ", " + block.getY() + ", " + block.getZ()
                    + "). Now Shift + Right-Click a PLC Switch to link!");
            return;
        }

        // Step 2: Shift + Right-click a PLC Switch to bind the saved Lightbulb
        if (sfId.equals("PLC_SWITCH")) {
            ItemMeta meta = cable.getItemMeta();
            if (meta == null) return;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (!pdc.has(targetKey, PersistentDataType.STRING)) {
                player.sendMessage(ChatColor.YELLOW + "[PLCFun] Right-click a PLC Lightbulb with this cable first!");
                return;
            }

            String locString = pdc.get(targetKey, PersistentDataType.STRING);
            BlockStorage.addBlockInfo(block, "linked_bulb", locString);

            player.sendMessage(ChatColor.GREEN + "[PLCFun] Successfully linked PLC Switch to the Lightbulb!");
        }
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }
}