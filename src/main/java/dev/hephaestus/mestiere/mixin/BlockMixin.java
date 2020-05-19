package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FernBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {
    @Shadow public static void dropStacks(BlockState blockState, World world, BlockPos blockPos, BlockEntity blockEntity) {}

    @Inject(method = "afterBreak", at = @At("HEAD"))
    public void addXP(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack, CallbackInfo ci) {
        if (!world.isClient() && EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack) == 0 && Mestiere.CONFIG.getMiningValue(state.getBlock()) > 0) {
            Mestiere.COMPONENT.get(player).addXp(Skill.MINING, Mestiere.CONFIG.getMiningValue(state.getBlock()));
        }

        if (state.getBlock() instanceof FernBlock || state.getBlock() instanceof TallPlantBlock) {
            float chance = Mestiere.COMPONENT.get(player).getScale(Skill.Perk.GATHERER) / 4.0f;
            if (Math.random() > chance) {
                dropStacks(state, world, pos, blockEntity);
            }
        }
    }
}
