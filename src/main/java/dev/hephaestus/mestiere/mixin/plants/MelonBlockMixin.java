package dev.hephaestus.mestiere.mixin.plants;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MelonBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MelonBlock.class)
public class MelonBlockMixin extends Block {
    // TODO: Make it so you can get XP from GourdBlocks *not* placed by the player
    // This Mixin is enabled, but should be for GourdBlocks once above is fixed.
    // Technically you can craft MelonBlocks and place and break them to get more
    // xp also, but just throwing the melon slices into a Composter is more effeicient,
    // so I'm just gonna allow this.


    public MelonBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        super.afterBreak(world, player, pos, state, blockEntity, stack);
        Mestiere.COMPONENT.get(player).addXp(Skill.FARMING, 1, false);
    }
}
