package me.grishkka.plcfun;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.handlers.ItemUseHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PLCWireCutter extends SlimefunItem {

    public PLCWireCutter(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        ItemUseHandler itemUseHandler = this::onUse;
        addItemHandler(itemUseHandler);
    }

    private void onUse(io.github.thebusybiscuit.slimefun5.api.events.PlayerRightClickEvent event) {
        Optional<Block> clickedBlockOpt = event.getClickedBlock();
        if (clickedBlockOpt.isEmpty()) {
            return;
        }

        event.cancel();

        Block block = clickedBlockOpt.get();
        Player player = event.getPlayer();
        String sfId = BlockStorage.checkID(block);

        if (sfId == null || !sfId.startsWith("PLC_")) {
            player.sendMessage(ChatColor.RED + "[PLCFun] That is not a PLC component!");
            return;
        }

        // Shift + Right-Click: Clear all connections
        if (player.isSneaking()) {
            boolean cleared = false;

            if (BlockStorage.hasBlockInfo(block)) {
                if (BlockStorage.getLocationInfo(block.getLocation(), "linked_bulbs") != null) {
                    BlockStorage.addBlockInfo(block, "linked_bulbs", null);
                    cleared = true;
                }
                if (BlockStorage.getLocationInfo(block.getLocation(), "linked_outputs") != null) {
                    BlockStorage.addBlockInfo(block, "linked_outputs", null);
                    cleared = true;
                }
                if (BlockStorage.getLocationInfo(block.getLocation(), "gate_inputs") != null) {
                    BlockStorage.addBlockInfo(block, "gate_inputs", null);
                    cleared = true;
                }
            }

            if (cleared) {
                player.playSound(block.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.2f);
                player.sendMessage(ChatColor.GOLD + "[PLCFun] Severed all connections for " + sfId + "!");
            } else {
                player.sendMessage(ChatColor.YELLOW + "[PLCFun] " + sfId + " had no active connections to clear.");
            }
            return;
        }

        // Normal Right-Click: Inspect connections
        String linkedBulbs = BlockStorage.getLocationInfo(block.getLocation(), "linked_bulbs");
        String linkedOutputs = BlockStorage.getLocationInfo(block.getLocation(), "linked_outputs");
        String gateInputs = BlockStorage.getLocationInfo(block.getLocation(), "gate_inputs");

        player.sendMessage(ChatColor.AQUA + "=== [PLC Inspector: " + sfId + "] ===");

        int count = 0;
        if (linkedBulbs != null && !linkedBulbs.isEmpty()) {
            String[] entries = linkedBulbs.split("\\|");
            count += entries.length;
            player.sendMessage(ChatColor.GRAY + "Connected Bulbs: " + ChatColor.GREEN + entries.length);
        }
        if (linkedOutputs != null && !linkedOutputs.isEmpty()) {
            String[] entries = linkedOutputs.split("\\|");
            count += entries.length;
            player.sendMessage(ChatColor.GRAY + "Connected Outputs: " + ChatColor.GREEN + entries.length);
        }
        if (gateInputs != null && !gateInputs.isEmpty()) {
            String[] entries = gateInputs.split("\\|");
            player.sendMessage(ChatColor.GRAY + "Gate Inputs: " + ChatColor.YELLOW + entries.length);
        }

        if (count == 0 && (gateInputs == null || gateInputs.isEmpty())) {
            player.sendMessage(ChatColor.DARK_GRAY + "No connected wiring found. (Shift + Right-Click to wipe if stuck)");
        } else {
            player.sendMessage(ChatColor.GRAY + "Tip: " + ChatColor.YELLOW + "Shift + Right-Click with Wire Cutter to sever all links.");
        }
    }
}