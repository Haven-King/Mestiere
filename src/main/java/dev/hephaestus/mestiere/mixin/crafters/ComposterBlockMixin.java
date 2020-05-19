package dev.hephaestus.mestiere.mixin.crafters;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.SkilledExperienceOrbEntity;
import dev.hephaestus.mestiere.skills.Skill;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {
    @Inject(method = "emptyFullComposter", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ItemEntity;setToDefaultPickupDelay()V"))
    private static void addXP(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (world != null)
            world.spawnEntity(new SkilledExperienceOrbEntity(world, pos.getX(), pos.getY(), pos.getZ(), 1, Skill.FARMING));
    }
}
