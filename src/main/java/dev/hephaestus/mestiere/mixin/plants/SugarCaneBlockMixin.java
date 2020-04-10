package dev.hephaestus.mestiere.mixin.plants;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin extends Block {
    // TODO: Make it so you can get XP from SugarCaneBlocks *not* placed by the player
    // This Mixin is not currently enabled..

    public SugarCaneBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        super.afterBreak(world, player, pos, state, blockEntity, stack);
        Mestiere.COMPONENT.get(player).addXp(Mestiere.FARMING, 1);
    }
}
