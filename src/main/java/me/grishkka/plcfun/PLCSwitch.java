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

        // If sneaking with a Data Cable, let DataCable handle the linking
        SlimefunItem heldSfItem = SlimefunItem.getByItem(event.getItem());
        if (player.isSneaking() && heldSfItem != null && heldSfItem.getId().equals("PLC_DATA_CABLE")) {
            event.cancel();
            return;
        }

        Block switchBlock = clickedBlockOpt.get();
        String linkedData = BlockStorage.getLocationInfo(switchBlock.getLocation(), "linked_bulbs");

        if (linkedData == null || linkedData.isEmpty()) {
            player.sendMessage(ChatColor.RED + "[PLCFun] This switch isn't linked! Use a Data Cable to link it to PLC Lightbulbs.");
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

            Block targetBulb = world.getBlockAt(x, y, z);
            String targetId = BlockStorage.checkID(targetBulb);

            // Skip bulbs that were mined/broken
            if (targetId == null || !targetId.equals("PLC_LIGHTBULB")) {
                continue;
            }

            validEntries.add(entry);

            // Toggle the Copper Bulb's lit state silently
            BlockData blockData = targetBulb.getBlockData();
            if (blockData instanceof Lightable lightable) {
                boolean newState = !lightable.isLit();
                lightable.setLit(newState);
                targetBulb.setBlockData(lightable, true);

                world.playSound(targetBulb.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_ON, 1.0f, 1.0f);
            }
        }

        // Update storage if any broken bulbs were cleaned up
        if (validEntries.size() != entries.length) {
            BlockStorage.addBlockInfo(switchBlock, "linked_bulbs", String.join("|", validEntries));
        }

        if (validEntries.isEmpty()) {
            player.sendMessage(ChatColor.RED + "[PLCFun] All linked PLC Lightbulbs were broken or removed!");
        }
    }
}