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

public class PLCCabinet extends SlimefunItem {

    public PLCCabinet(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    public void preRegister() {
        BlockUseHandler blockUseHandler = this::onCabinetInteract;
        addItemHandler(blockUseHandler);
    }

    private void onCabinetInteract(io.github.thebusybiscuit.slimefun5.api.events.PlayerRightClickEvent event) {
        Optional<Block> clickedBlockOpt = event.getClickedBlock();
        if (clickedBlockOpt.isEmpty()) {
            return;
        }

        event.cancel();
        Player player = event.getPlayer();

        // Let DataCable handle Shift + Right-Click linking
        SlimefunItem heldSfItem = SlimefunItem.getByItem(event.getItem());
        if (player.isSneaking() && heldSfItem != null && heldSfItem.getId().equals("PLC_DATA_CABLE")) {
            return;
        }

        Block cabinetBlock = clickedBlockOpt.get();
        String linkedData = BlockStorage.getLocationInfo(cabinetBlock.getLocation(), "linked_outputs");

        if (linkedData == null || linkedData.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "[PLC Cabinet] No devices wired. Select a Bulb or Chime with a Data Cable and Shift + Right-Click this Cabinet!");
            return;
        }

        String[] entries = linkedData.split("\\|");
        player.sendMessage(ChatColor.AQUA + "=== [PLC Cabinet Diagnostic] ===");
        player.sendMessage(ChatColor.GRAY + "Connected Outputs: " + ChatColor.GREEN + entries.length);

        for (String entry : entries) {
            String[] parts = entry.split(";");
            if (parts.length != 4) continue;

            World world = Bukkit.getWorld(parts[0]);
            if (world == null) continue;

            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int z = Integer.parseInt(parts[3]);

            Block target = world.getBlockAt(x, y, z);
            String id = BlockStorage.checkID(target);

            player.sendMessage(ChatColor.GRAY + " - " + ChatColor.WHITE + (id != null ? id : "MISSING")
                    + ChatColor.DARK_GRAY + " @ (" + x + ", " + y + ", " + z + ")");

            // Master Test Toggle on all connected devices when right-clicking Cabinet
            if ("PLC_LIGHTBULB".equals(id)) {
                BlockData bd = target.getBlockData();
                if (bd instanceof Lightable lightable) {
                    lightable.setLit(!lightable.isLit());
                    target.setBlockData(lightable, true);
                }
            } else if ("PLC_CHIME".equals(id)) {
                world.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.5f, 1.0f);
            }
        }
    }
}