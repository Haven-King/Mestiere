package dev.hephaestus.mestiere.mixin.crafters;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.SkillCrafter;
import dev.hephaestus.mestiere.skills.SkillRecipe;
import dev.hephaestus.mestiere.skills.Skills;
import net.minecraft.block.BlastFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BlastFurnaceBlock.class)
public class BlastFurnaceMixin extends Block {

    public BlastFurnaceMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    @Inject(method="openContainer", at=@At("HEAD"), cancellable = true)
    public void openContainer(World world, BlockPos pos, PlayerEntity player, CallbackInfo ci) {
        Item mainHand = player.getMainHandStack().getItem();
        if ( mainHand == Items.LEATHER_HELMET || mainHand == Items.LEATHER_CHESTPLATE || mainHand == Items.LEATHER_LEGGINGS || mainHand == Items.LEATHER_BOOTS || mainHand == Items.LEATHER_HORSE_ARMOR) {

            SkillCrafter crafter = new SkillCrafter(player, Skills.SMITHING, this, SoundEvents.ENTITY_VILLAGER_WORK_ARMORER);

            for (SkillRecipe r : Mestiere.RECIPES.getRegistered(Skills.SMITHING)) {
                if (r.getSecondBuyItem().getItem() != Items.STICK && r.getOriginalFirstBuyItem().getItem() != Items.STICK)
                    crafter.addRecipe(r);
            }

            crafter.show();
            ci.cancel();
        }
    }
}