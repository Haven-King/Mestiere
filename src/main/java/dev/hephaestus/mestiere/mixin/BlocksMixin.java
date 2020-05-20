package dev.hephaestus.mestiere.mixin;

import net.minecraft.block.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Blocks.class)
public class BlocksMixin {
	@SuppressWarnings("UnresolvedMixinReference")
	@Redirect(method = "<clinit>", at = @At(value = "NEW", target = "net/minecraft/block/OreBlock"))
	private static OreBlock redirOreBlock(Block.Settings settings) {
		return new OreBlock(settings.strength(1.5F, 3F));
	}
}
