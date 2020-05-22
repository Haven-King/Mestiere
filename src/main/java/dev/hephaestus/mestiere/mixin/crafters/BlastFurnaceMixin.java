package dev.hephaestus.mestiere.mixin.crafters;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.BlastFurnaceBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
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

    @Inject(method="openScreen", at=@At("HEAD"), cancellable = true)
    public void openScreen(World world, BlockPos blockPos, PlayerEntity player, CallbackInfo ci) {
        if (!world.isClient)
            ContainerProviderRegistry.INSTANCE.openContainer(
                    Registry.BLOCK.getId(Blocks.BLAST_FURNACE), player, (packetByteBuf -> packetByteBuf.writeBlockPos(blockPos))
            );

        ci.cancel();
    }
}