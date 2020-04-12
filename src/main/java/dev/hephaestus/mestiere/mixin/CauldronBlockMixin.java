package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.SkillCrafter;
import dev.hephaestus.mestiere.skills.SkillRecipe;
import dev.hephaestus.mestiere.util.Skills;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CauldronBlock.class)
public class CauldronBlockMixin extends Block {

    public CauldronBlockMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    @Inject(at = @At("HEAD"), method = "onUse", cancellable = true)
    public void activateInjection(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> info) {
        ItemStack itemStack_1 = player.getStackInHand(hand);
        if (itemStack_1.getItem() == Items.LEATHER && blockState.get(Properties.LEVEL_3) == 3 && !world.isClient()) {

            SkillCrafter crafter = new SkillCrafter(player, Skills.LEATHERWORKING, this, SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER);

            for (SkillRecipe r : Mestiere.RECIPES.getRegistered(Skills.LEATHERWORKING)) {
                crafter.addRecipe(r);
            }

            crafter.show();
            info.setReturnValue(ActionResult.SUCCESS);
        }
    }
}