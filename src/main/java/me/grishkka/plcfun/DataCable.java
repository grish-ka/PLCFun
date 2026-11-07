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
    private final NamespacedKey targetTypeKey;

    public DataCable(JavaPlugin plugin, ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.targetKey = new NamespacedKey(plugin, "linked_target_loc");
        this.targetTypeKey = new NamespacedKey(plugin, "linked_target_type");
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

        // STEP 1: Right-Click to select a source / output block
        if (!player.isSneaking()) {
            ItemMeta meta = cable.getItemMeta();
            if (meta == null) return;

            String locString = serializeLocation(block.getLocation());
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(targetKey, PersistentDataType.STRING, locString);
            pdc.set(targetTypeKey, PersistentDataType.STRING, sfId);
            cable.setItemMeta(meta);

            if (sfId.equals("PLC_LIGHTBULB") || sfId.equals("PLC_CHIME")) {
                player.sendMessage(ChatColor.AQUA + "[PLCFun] Selected Output (" + sfId + "). Shift + Right-Click a Switch, Cabinet, or AND Gate to link!");
            } else if (sfId.equals("PLC_AND_GATE")) {
                player.sendMessage(ChatColor.AQUA + "[PLCFun] Selected AND Gate. Shift + Right-Click a Switch or Doorbell to add it as an INPUT!");
            } else {
                player.sendMessage(ChatColor.AQUA + "[PLCFun] Selected " + sfId + " at (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ").");
            }
            return;
        }

        // STEP 2: Shift + Right-Click to connect the saved item to the clicked block
        ItemMeta meta = cable.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(targetKey, PersistentDataType.STRING) || !pdc.has(targetTypeKey, PersistentDataType.STRING)) {
            player.sendMessage(ChatColor.YELLOW + "[PLCFun] Right-click a component without sneaking first to select it!");
            return;
        }

        String sourceLoc = pdc.get(targetKey, PersistentDataType.STRING);
        String sourceType = pdc.get(targetTypeKey, PersistentDataType.STRING);

        // Case A: Link selected Output (Bulb / Chime) into AND Gate
        if (sfId.equals("PLC_AND_GATE") && (sourceType.equals("PLC_LIGHTBULB") || sourceType.equals("PLC_CHIME"))) {
            addEntryToStorage(block, "linked_outputs", sourceLoc);
            player.sendMessage(ChatColor.GREEN + "[PLCFun] Linked " + sourceType + " as an OUTPUT of the AND Gate!");
            return;
        }

        // Case B: Link selected AND Gate into an Input Switch / Doorbell
        if ((sfId.equals("PLC_SWITCH") || sfId.equals("PLC_DOORBELL")) && sourceType.equals("PLC_AND_GATE")) {
            // Tell the AND Gate about this new input switch
            Location gateLoc = parseLocation(sourceLoc);
            if (gateLoc != null && gateLoc.getBlock() != null) {
                String thisSwitchLoc = serializeLocation(block.getLocation());
                addEntryToStorage(gateLoc.getBlock(), "gate_inputs", thisSwitchLoc);
            }
            // Tell the switch where its target gate is
            addEntryToStorage(block, "linked_gates", sourceLoc);
            player.sendMessage(ChatColor.GREEN + "[PLCFun] Linked " + sfId + " as an INPUT to the AND Gate!");
            return;
        }

        // Case C: Standard linking to Switches, Cabinets, or Doorbells
        if (sfId.equals("PLC_SWITCH") || sfId.equals("PLC_DOORBELL") || sfId.equals("PLC_CABINET")) {
            String storageKey = sfId.equals("PLC_SWITCH") ? "linked_bulbs" : "linked_outputs";
            addEntryToStorage(block, storageKey, sourceLoc);
            player.sendMessage(ChatColor.GREEN + "[PLCFun] Linked " + sourceType + " to " + sfId + "!");
            return;
        }

        player.sendMessage(ChatColor.RED + "[PLCFun] Cannot link " + sourceType + " to " + sfId + ".");
    }

    private void addEntryToStorage(Block block, String key, String newEntry) {
        String existingData = BlockStorage.getLocationInfo(block.getLocation(), key);
        List<String> list = new ArrayList<>();
        if (existingData != null && !existingData.isEmpty()) {
            list.addAll(Arrays.asList(existingData.split("\\|")));
        }
        if (!list.contains(newEntry)) {
            list.add(newEntry);
            BlockStorage.addBlockInfo(block, key, String.join("|", list));
        }
    }

    private String serializeLocation(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }

    private Location parseLocation(String str) {
        String[] parts = str.split(";");
        if (parts.length != 4) return null;
        org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        return new Location(world, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }
}