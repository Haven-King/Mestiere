package dev.hephaestus.mestiere.mixin.plants;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.block.BlockState;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MushroomPlantBlock.class)
public class MushroomPlantBlockMixin extends PlantBlock {
    // TODO: Make it so you can get XP from MushroomPlantBlock's *not* placed by the player
    // This Mixin is not currently enabled..

    protected MushroomPlantBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        super.afterBreak(world, player, pos, state, blockEntity, stack);
        Mestiere.COMPONENT.get(player).addXp(Mestiere.FARMING, 1);
    }
}
