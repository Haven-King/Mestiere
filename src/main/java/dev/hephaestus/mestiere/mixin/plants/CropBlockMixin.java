package dev.hephaestus.mestiere.mixin.plants;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.PlantBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin extends PlantBlock {

    protected CropBlockMixin(Settings settings) {
        super(settings);
    }

    @Shadow public boolean isMature(BlockState blockState) {return false;}


    @Shadow public abstract BlockState withAge(int age);

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (this.isMature(state) && !world.isClient) {
            if (Mestiere.COMPONENT.get(player).hasPerk(Mestiere.newID("green_thumb"))) {
                Block.getDroppedStacks(state, (ServerWorld)world, pos, null).forEach((stack) -> {
                    if (stack.getItem().toString().contains("seeds")) { // This is hacky as fuck, but so is Minecraft
                        stack.setCount(stack.getCount() - 1);
                    }

                    Block.dropStack(world, pos, stack);
                });

                Mestiere.COMPONENT.get(player).addXp(Skill.FARMING, 1, true);
                world.setBlockState(pos, this.withAge(0));
            }

            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);
        Mestiere.COMPONENT.get(player).addXp(Skill.FARMING, 1, true);
    }
}
