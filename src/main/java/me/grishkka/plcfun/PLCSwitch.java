package me.grishkka.plcfun;

import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun5.core.handlers.BlockUseHandler;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PLCSwitch extends SlimefunItem {

    public PLCSwitch(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        BlockUseHandler blockUseHandler = this::onSwitchFlip;
        addItemHandler(blockUseHandler);
    }

    private void onSwitchFlip(io.github.thebusybiscuit.slimefun5.api.events.PlayerRightClickEvent event) {
        Optional<Block> clickedBlockOpt = event.getClickedBlock();
        if (clickedBlockOpt.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();

        // Let DataCable handle Shift + Right-Click linking without triggering the switch
        SlimefunItem heldSfItem = SlimefunItem.getByItem(event.getItem());
        if (player.isSneaking() && heldSfItem != null && heldSfItem.getId().equals("PLC_DATA_CABLE")) {
            event.cancel();
            return;
        }

        Block switchBlock = clickedBlockOpt.get();
        String linkedBulbs = BlockStorage.getLocationInfo(switchBlock.getLocation(), "linked_bulbs");
        String linkedGates = BlockStorage.getLocationInfo(switchBlock.getLocation(), "linked_gates");

        boolean hasBulbs = linkedBulbs != null && !linkedBulbs.isEmpty();
        boolean hasGates = linkedGates != null && !linkedGates.isEmpty();

        if (!hasBulbs && !hasGates) {
            player.sendMessage(ChatColor.RED + "[PLCFun] This switch isn't linked! Use a Data Cable to link it to a Lightbulb or AND Gate.");
            return;
        }

        // Toggle state for this switch in BlockStorage
        String stateStr = BlockStorage.getLocationInfo(switchBlock.getLocation(), "is_on");
        boolean newState = stateStr == null || !Boolean.parseBoolean(stateStr);
        BlockStorage.addBlockInfo(switchBlock, "is_on", String.valueOf(newState));

        // 1. Toggle directly linked Lightbulbs
        if (hasBulbs) {
            String[] entries = linkedBulbs.split("\\|");
            List<String> validEntries = new ArrayList<>();

            for (String entry : entries) {
                String[] parts = entry.split(";");
                if (parts.length != 4) continue;

                World world = Bukkit.getWorld(parts[0]);
                if (world == null) continue;

                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);

                Block targetBulb = world.getBlockAt(x, y, z);
                String targetId = BlockStorage.checkID(targetBulb);

                // Auto-cleanup for broken or missing bulbs
                if (targetId == null || !targetId.equals("PLC_LIGHTBULB")) {
                    continue;
                }

                validEntries.add(entry);

                BlockData blockData = targetBulb.getBlockData();
                if (blockData instanceof Lightable lightable) {
                    lightable.setLit(newState);
                    targetBulb.setBlockData(lightable, true);
                    world.playSound(targetBulb.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_ON, 1.0f, 1.0f);
                }
            }

            // Save cleaned list back to BlockStorage if any missing bulbs were pruned
            if (validEntries.size() != entries.length) {
                BlockStorage.addBlockInfo(switchBlock, "linked_bulbs", String.join("|", validEntries));
            }
        }

        // 2. Notify linked Logic Gates (e.g., AND Gates)
        if (hasGates) {
            String switchLoc = switchBlock.getWorld().getName() + ";" + switchBlock.getX() + ";" + switchBlock.getY() + ";" + switchBlock.getZ();
            String[] gateEntries = linkedGates.split("\\|");
            List<String> validGates = new ArrayList<>();

            for (String gateEntry : gateEntries) {
                String[] p = gateEntry.split(";");
                if (p.length != 4) continue;

                World w = Bukkit.getWorld(p[0]);
                if (w == null) continue;

                Block gateBlock = w.getBlockAt(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
                String gateId = BlockStorage.checkID(gateBlock);

                if ("PLC_AND_GATE".equals(gateId)) {
                    validGates.add(gateEntry);
                    PLCAndGate.updateGate(gateBlock, switchLoc, newState);
                }
            }

            // Prune broken gates
            if (validGates.size() != gateEntries.length) {
                BlockStorage.addBlockInfo(switchBlock, "linked_gates", String.join("|", validGates));
            }
        }
    }
}