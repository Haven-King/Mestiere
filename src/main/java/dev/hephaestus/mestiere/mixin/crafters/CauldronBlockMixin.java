package dev.hephaestus.mestiere.mixin.crafters;

import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
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
    public void activateInjection(BlockState blockState, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> ci) {
        if (blockState.get(Properties.LEVEL_3) > 0 && !world.isClient()) {
            if (!world.isClient)
                ContainerProviderRegistry.INSTANCE.openContainer(
                        Registry.BLOCK.getId(Blocks.CAULDRON), player, (packetByteBuf -> packetByteBuf.writeBlockPos(blockPos))
                );

            ci.setReturnValue(ActionResult.SUCCESS);
        }
    }
}