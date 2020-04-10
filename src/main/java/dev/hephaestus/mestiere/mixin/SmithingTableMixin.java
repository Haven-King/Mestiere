package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.SkillCrafter;
import dev.hephaestus.mestiere.skills.SkillRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(SmithingTableBlock.class)
public class SmithingTableMixin extends Block {

    public SmithingTableMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
        if (world.isClient()) {
            return ActionResult.PASS;
        }

        SkillCrafter crafter = new SkillCrafter(player, Mestiere.SMITHING, this, SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH);

        for (SkillRecipe r : Mestiere.RECIPES.getRegistered(Mestiere.SMITHING)) {
            if (r.getSecondBuyItem().getItem() == Items.STICK || r.getOriginalFirstBuyItem().getItem() == Items.STICK)
                crafter.addRecipe(r);
        }

        crafter.show();

        return ActionResult.SUCCESS;
    }
}