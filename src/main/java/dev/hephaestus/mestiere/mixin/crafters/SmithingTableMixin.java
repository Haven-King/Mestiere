package dev.hephaestus.mestiere.mixin.crafters;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SmithingTableBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(SmithingTableBlock.class)
public class SmithingTableMixin extends Block {
    public SmithingTableMixin(Settings block$Settings_1) {
        super(block$Settings_1);
    }

    public ActionResult onUse(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult) {
        if (!world.isClient)
            ContainerProviderRegistry.INSTANCE.openContainer(
                Registry.BLOCK.getId(Blocks.SMITHING_TABLE), player, (packetByteBuf -> packetByteBuf.writeBlockPos(blockPos))
            );

        return ActionResult.SUCCESS;
    }
}