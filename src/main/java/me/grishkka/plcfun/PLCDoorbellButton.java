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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PLCDoorbellButton extends SlimefunItem {

    private final JavaPlugin plugin;

    public PLCDoorbellButton(JavaPlugin plugin, ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.plugin = plugin;
    }

    @Override
    public void preRegister() {
        BlockUseHandler blockUseHandler = this::onButtonPress;
        addItemHandler(blockUseHandler);
    }

    private void onButtonPress(io.github.thebusybiscuit.slimefun5.api.events.PlayerRightClickEvent event) {
        Optional<Block> clickedBlockOpt = event.getClickedBlock();
        if (clickedBlockOpt.isEmpty()) {
            return;
        }

        Player player = event.getPlayer();

        // Let DataCable handle Shift + Right-Click linking
        SlimefunItem heldSfItem = SlimefunItem.getByItem(event.getItem());
        if (player.isSneaking() && heldSfItem != null && heldSfItem.getId().equals("PLC_DATA_CABLE")) {
            event.cancel();
            return;
        }

        Block buttonBlock = clickedBlockOpt.get();
        String linkedData = BlockStorage.getLocationInfo(buttonBlock.getLocation(), "linked_outputs");

        if (linkedData == null || linkedData.isEmpty()) {
            player.sendMessage(ChatColor.RED + "[PLCFun] This doorbell isn't linked! Use a Data Cable to link it to a PLC Chime or Lightbulb.");
            return;
        }

        String[] entries = linkedData.split("\\|");
        List<String> validEntries = new ArrayList<>();

        for (String entry : entries) {
            String[] parts = entry.split(";");
            if (parts.length != 4) continue;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            Block targetBlock = world.getBlockAt(x, y, z);
            String targetId = BlockStorage.checkID(targetBlock);

            if (targetId == null) continue;

            // Output 1: Ring the PLC Chime ("Ding - Dong")
            if (targetId.equals("PLC_CHIME")) {
                validEntries.add(entry);
                world.playSound(targetBlock.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2.0f, 1.2f);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    world.playSound(targetBlock.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 2.0f, 0.9f);
                }, 6L);
            }
            // Output 2: Flash a linked PLC Lightbulb for 1 second (20 ticks)
            else if (targetId.equals("PLC_LIGHTBULB")) {
                validEntries.add(entry);
                BlockData blockData = targetBlock.getBlockData();
                if (blockData instanceof Lightable lightable) {
                    lightable.setLit(true);
                    targetBlock.setBlockData(lightable, true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Block