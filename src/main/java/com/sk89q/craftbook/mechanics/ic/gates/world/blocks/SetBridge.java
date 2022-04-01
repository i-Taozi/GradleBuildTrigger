package com.sk89q.craftbook.mechanics.ic.gates.world.blocks;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.craftbook.util.SignUtil;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;

/**
 * @author Silthus
 */
public class SetBridge extends AbstractIC {

    private BlockData onBlock;
    private BlockData offBlock;

    private int width;
    private int depth;

    private int offsetX = 0;
    private int offsetY = 1;
    private int offsetZ = 0;

    private Block center;
    private BlockFace faceing;

    public SetBridge(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {
        center = getBackBlock();
        faceing = SignUtil.getFacing(CraftBookBukkitUtil.toSign(getSign()).getBlock());
        String line = getSign().getLine(2);
        if (!line.isEmpty()) {
            String[] split = RegexUtil.MINUS_PATTERN.split(line);
            // parse the material data
            if (split.length > 1) {
                offBlock = BlockSyntax.getBukkitBlock(split[1]);
            }
            // parse the material and data for toggle on
            onBlock = BlockSyntax.getBukkitBlock(split[0]);
        }
        // parse the coordinates
        line = getSign().getLine(3);
        if (!line.trim().isEmpty()) {
            boolean relativeOffset = !line.contains("!");
            if (!relativeOffset) {
                line = line.trim().replace("!", "");
            }
            String[] split = RegexUtil.COLON_PATTERN.split(line);
            try {
                // parse the offset
                String[] offsetSplit = RegexUtil.COMMA_PATTERN.split(split[0]);
                offsetX = Integer.parseInt(offsetSplit[0]);
                offsetY = Integer.parseInt(offsetSplit[1]);
                offsetZ = Integer.parseInt(offsetSplit[2]);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                // ignore and use defaults
            }
            try {
                // parse the size of the door
                String[] sizeSplit = RegexUtil.COMMA_PATTERN.split(split[1]);
                width = Integer.parseInt(sizeSplit[0]);
                depth = Integer.parseInt(sizeSplit[1]);
            } catch (NumberFormatException e) {
                width = 1;
                depth = 1;
            } catch (ArrayIndexOutOfBoundsException e) {
                depth = 1;
            }
            if (relativeOffset) {
                center = LocationUtil.getRelativeOffset(getSign(), offsetX, offsetY, offsetZ);
            } else {
                center = LocationUtil.getOffset(center, offsetX, offsetY, offsetZ);
            }
        } else {
            center = center.getRelative(BlockFace.UP);
        }
    }

    @Override
    public String getTitle() {

        return "Set P-Bridge";
    }

    @Override
    public String getSignTitle() {

        return "SET P-Bridge";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            setDoor(true);
        } else {
            setDoor(false);
        }

        chip.setOutput(0, chip.getInput(0));
    }

    private void setDoor(boolean open) {

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                Block block = LocationUtil.getRelativeOffset(center, faceing, x, 0, z);
                // do not replace the block the sign is on
                boolean isSource = block.equals(getBackBlock());

                if (open) {
                    if (isSource && BlockUtil.isBlockReplacable(onBlock.getMaterial())) continue;
                    block.setBlockData(onBlock);
                } else {
                    if (isSource && BlockUtil.isBlockReplacable(offBlock.getMaterial())) continue;
                    block.setBlockData(offBlock);
                }
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String getShortDescription() {

            return "Generates a bridge out of the set materials with set size.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"onID{:onData-offID:offData}", "offset x,y,z:width,depth"};
        }

        @Override
        public IC create(ChangedSign sign) {

            return new SetBridge(getServer(), sign, this);
        }
    }
}
