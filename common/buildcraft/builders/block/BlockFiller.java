/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileFiller;

public class BlockFiller extends BlockBCTile_Neptune implements IBlockWithFacing {
    // public static final IProperty<EnumFillerPattern> PATTERN = BuildCraftProperties.FILLER_PATTERN;

    public BlockFiller(Material material, String id) {
        super(material, id);
        // setDefaultState(getDefaultState().withProperty(PATTERN, EnumFillerPattern.NONE));
    }

    // BlockState

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        // properties.add(PATTERN);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileFiller) {
            TileFiller filler = (TileFiller) tile;
            // return state.withProperty(PATTERN, EnumFillerPattern.NONE); // FIXME
        }
        return state;
    }

    // Others

    @Override
    public TileBC_Neptune createTileEntity(World world, IBlockState state) {
        return new TileFiller();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
        EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileFiller) {
            if (!((TileFiller) tile).hasBox()) {
                return false;
            }
        }
        if (!world.isRemote) {
            BCBuildersGuis.FILLER.openGUI(player, pos);
        }
        return true;
    }

    @Override
    public boolean canBeRotated(World world, BlockPos pos, IBlockState state) {
        return false;
    }
}
