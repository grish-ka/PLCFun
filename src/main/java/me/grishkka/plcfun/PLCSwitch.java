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

        // If the player is sneaking with a Data Cable to link it, let DataCable handle the click
        SlimefunItem heldSfItem = SlimefunItem.getByItem(event.getItem());
        if (player.isSneaking() && heldSfItem != null && heldSfItem.getId().equals("PLC_DATA_CABLE")) {
            event.cancel();
            return;
        }

        Block switchBlock = clickedBlockOpt.get();
        String linkedData = BlockStorage.getLocationInfo(switchBlock.getLocation(), "linked_bulb");

        if (linkedData == null) {
            player.sendMessage(ChatColor.RED + "[PLCFun] This switch isn't linked! Use a Data Cable to link it to a PLC Lightbulb.");
            return;
        }

        // Parse "world;x;y;z"
        String[] parts = linkedData.split(";");
        if (parts.length != 4) return;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return;

        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);

        Block targetBulb = world.getBlockAt(x, y, z);
        String targetId = BlockStorage.checkID(targetBulb);

        if (targetId == null || !targetId.equals("PLC_LIGHTBULB")) {
            player.sendMessage(ChatColor.RED + "[PLCFun] Linked PLC Lightbulb is missing or was broken!");
            return;
        }

        // Toggle the Copper Bulb's lit state silently (no chat spam)
        BlockData blockData = targetBulb.getBlockData();
        if (blockData instanceof Lightable lightable) {
            boolean newState = !lightable.isLit();
            lightable.setLit(newState);
            targetBulb.setBlockData(lightable, true);

            world.playSound(targetBulb.getLocation(), Sound.BLOCK_COPPER_BULB_TURN_ON, 1.0f, 1.0f);
        }
    }
}