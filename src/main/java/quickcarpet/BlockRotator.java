package quickcarpet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockEndRod;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockGlazedTerracotta;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRotator {

    @SuppressWarnings("deprecation")
    public static boolean cactusRotate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND)
            return false;
        if (!player.abilities.allowEdit)
            return false;
        if (player.getHeldItemMainhand().getItem() != Item.getItemFromBlock(Blocks.CACTUS))
            return false;
        Block block = state.getBlock();

        IBlockState newState = null;
        
        if (block instanceof BlockGlazedTerracotta || block instanceof BlockRedstoneDiode || block instanceof BlockRailBase
                || block instanceof BlockTrapDoor || block instanceof BlockLever || block instanceof BlockFenceGate) {
            newState = state.rotate(Rotation.CLOCKWISE_90);
        } else if (block instanceof BlockObserver || block instanceof BlockEndRod || block instanceof BlockDispenser) {
            newState = state.with(BlockDirectional.FACING, state.get(BlockDirectional.FACING).getOpposite());
        } else if (block instanceof BlockPistonBase) {
            if (!state.get(BlockPistonBase.EXTENDED))
                newState = state.with(BlockDirectional.FACING, state.get(BlockDirectional.FACING).getOpposite());
        } else if (block instanceof BlockSlab) {
            if (state.get(BlockSlab.TYPE) == SlabType.BOTTOM)
                newState = state.with(BlockSlab.TYPE, SlabType.TOP);
            else if (state.get(BlockSlab.TYPE) == SlabType.TOP)
                newState = state.with(BlockSlab.TYPE, SlabType.BOTTOM);
        } else if (block instanceof BlockHopper) {
            if (state.get(BlockHopper.FACING) != EnumFacing.DOWN)
                newState = state.with(BlockHopper.FACING, state.get(BlockHopper.FACING).rotateY());
        } else if (block instanceof BlockStairs) {
            if ((facing == EnumFacing.UP && hitY == 1) || (facing == EnumFacing.DOWN && hitY == 0)) {
                if (state.get(BlockStairs.HALF) == Half.BOTTOM)
                    newState = state.with(BlockStairs.HALF, Half.TOP);
                else
                    newState = state.with(BlockStairs.HALF, Half.BOTTOM);
            } else {
                boolean turnRight = true;
                if (facing == EnumFacing.NORTH) {
                    turnRight = hitX <= 0.5;
                } else if (facing == EnumFacing.SOUTH) {
                    turnRight = hitX > 0.5;
                } else if (facing == EnumFacing.EAST) {
                    turnRight = hitZ <= 0.5;
                } else if (facing == EnumFacing.WEST) {
                    turnRight = hitZ > 0.5;
                } else {
                    return false;
                }
                
                if (turnRight) {
                    newState = state.rotate(Rotation.COUNTERCLOCKWISE_90);
                } else {
                    newState = state.rotate(Rotation.CLOCKWISE_90);
                }
            }
        }
        
        if (newState == null)
            return false;
        
        FillUpdatesCommand.updatesEnabled = false;
        world.setBlockState(pos, newState, 2);
        FillUpdatesCommand.updatesEnabled = true;
        world.markBlockRangeForRenderUpdate(pos, pos);
        return true;
    }
    
}
