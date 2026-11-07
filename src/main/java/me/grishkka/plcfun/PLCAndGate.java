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

import java.util.Optional;

public class PLCAndGate extends SlimefunItem {

    public PLCAndGate(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        BlockUseHandler blockUseHandler = this::onInteract;
        addItemHandler(blockUseHandler);
    }

    private void onInteract(io.github.thebusybiscuit.slimefun5.api.events.PlayerRightClickEvent event) {
        Optional<Block> clickedBlockOpt = event.getClickedBlock();
        if (clickedBlockOpt.isEmpty()) return;

        Player player = event.getPlayer();
        SlimefunItem held = SlimefunItem.getByItem(event.getItem());

        if (held != null && (held.getId().equals("PLC_DATA_CABLE") || held.getId().equals("PLC_WIRE_CUTTER"))) {
            return;
        }

        Block block = clickedBlockOpt.get();
        String outputs = BlockStorage.getLocationInfo(block.getLocation(), "linked_outputs");
        int outputCount = (outputs != null && !outputs.isEmpty()) ? outputs.split("\\|").length : 0;

        String inputs = BlockStorage.getLocationInfo(block.getLocation(), "gate_inputs");
        int inputCount = (inputs != null && !inputs.isEmpty()) ? inputs.split("\\|").length : 0;

        player.sendMessage(ChatColor.AQUA + "[PLC AND Gate] Status: "
                + ChatColor.YELLOW + inputCount + " inputs registered, "
                + ChatColor.GREEN + outputCount + " outputs linked.");
    }

    /**
     * Called when an input device (Switch/Button) changes state.
     */
    public static void updateGate(Block gateBlock, String inputSourceLoc, boolean isActive) {
        String inputsData = BlockStorage.getLocationInfo(gateBlock.getLocation(), "gate_inputs");
        if (inputsData == null || inputsData.isEmpty()) return;

        // Active inputs stored as active_inputs in BlockStorage
        String activeData = BlockStorage.getLocationInfo(gateBlock.getLocation(), "active_inputs");
        java.util.Set<String> activeSet = new java.util.HashSet<>();
        if (activeData != null && !activeData.isEmpty()) {
            activeSet.addAll(java.util.Arrays.asList(activeData.split("\\|")));
        }

        if (isActive) {
            activeSet.add(inputSourceLoc);
        } else {
            activeSet.remove(inputSourceLoc);
        }

        BlockStorage.addBlockInfo(gateBlock, "active_inputs", String.join("|", activeSet));

        String[] allInputs = inputsData.split("\\|");
        boolean allSatisfied = allInputs.length > 0 && activeSet.size() >= allInputs.length;

        triggerOutputs(gateBlock, allSatisfied);
    }

    private static void triggerOutputs(Block gateBlock, boolean powerOn) {
        String outputsData = BlockStorage.getLocationInfo(gateBlock.getLocation(), "linked_outputs");
        if (outputsData == null || outputsData.isEmpty()) return;

        for (String entry : outputsData.split("\\|")) {
            String[] parts = entry.split(";");
            if (parts.length != 4) continue;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;

            Block target = world.getBlockAt(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            String sfId = BlockStorage.checkID(target);
            if (sfId == null) continue;

            if (sfId.equals("PLC_LIGHTBULB")) {
                BlockData bd = target.getBlockData();
                if (bd instanceof Lightable lightable) {
                    lightable.setLit(powerOn);
                    target.setBlockData(lightable, true);
                }
            } else if (sfId.equals("PLC_CHIME") && powerOn) {
                world.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.5f, 1.2f);
            }
        }
    }
}