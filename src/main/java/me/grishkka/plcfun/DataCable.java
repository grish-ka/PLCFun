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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private void onCableRightClick(io.github.thebusybiscuit.slimefun5.api.events.PlayerRightClickEvent event) {
        Optional<Block> clickedBlockOpt = event.getClickedBlock();
        if (clickedBlockOpt.isEmpty()) {
            return;
        }

        event.cancel();

        Block block = clickedBlockOpt.get();
        Player player = event.getPlayer();
        ItemStack cable = event.getItem();

        String sfId = BlockStorage.checkID(block);
        if (sfId == null) {
            player.sendMessage(ChatColor.RED + "[PLCFun] That block is not a PLC component!");
            return;
        }

        // Step 1: Select an Output (Lightbulb or Chime)
        if (sfId.equals("PLC_LIGHTBULB") || sfId.equals("PLC_CHIME")) {
            ItemMeta meta = cable.getItemMeta();
            if (meta == null) return;

            String locString = serializeLocation(block.getLocation());
            meta.getPersistentDataContainer().set(targetKey, PersistentDataType.STRING, locString);
            cable.setItemMeta(meta);

            player.sendMessage(ChatColor.AQUA + "[PLCFun] Selected " + sfId + " at ("
                    + block.getX() + ", " + block.getY() + ", " + block.getZ()
                    + "). Shift + Right-Click a Switch, Doorbell, or Cabinet to link!");
            return;
        }

        // Step 2: Link into a Controller/Input (Switch, Doorbell Button, or Cabinet)
        if (sfId.equals("PLC_SWITCH") || sfId.equals("PLC_DOORBELL") || sfId.equals("PLC_CABINET")) {
            ItemMeta meta = cable.getItemMeta();
            if (meta == null) return;

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (!pdc.has(targetKey, PersistentDataType.STRING)) {
                player.sendMessage(ChatColor.YELLOW + "[PLCFun] Right-click a PLC Lightbulb or Chime with this cable first!");
                return;
            }

            String storageKey = sfId.equals("PLC_SWITCH") ? "linked_bulbs" : "linked_outputs";
            String newLoc = pdc.get(targetKey, PersistentDataType.STRING);
            String existingData = BlockStorage.getLocationInfo(block.getLocation(), storageKey);

            List<String> outputs = new ArrayList<>();
            if (existingData != null && !existingData.isEmpty()) {
                outputs.addAll(Arrays.asList(existingData.split("\\|")));
            }

            if (outputs.contains(newLoc)) {
                player.sendMessage(ChatColor.YELLOW + "[PLCFun] That device is already linked to this " + sfId + "!");
                return;
            }

            outputs.add(newLoc);
            BlockStorage.addBlockInfo(block, storageKey, String.join("|", outputs));

            player.sendMessage(ChatColor.GREEN + "[PLCFun] Linked output to " + sfId + "! (Total connected: " + outputs.size() + ")");
        }
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }
}