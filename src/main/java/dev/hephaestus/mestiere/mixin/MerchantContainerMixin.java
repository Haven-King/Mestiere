package dev.hephaestus.mestiere.mixin;

import net.minecraft.container.MerchantContainer;
import net.minecraft.entity.Entity;
import net.minecraft.village.Trader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantContainer.class)
public class MerchantContainerMixin {
    private final Trader trader;

    public MerchantContainerMixin(Trader trader) {
        this.trader = trader;
    }

    @Inject(method="playYesSound()V", at=@At("HEAD"), cancellable = true)
    private void playYesSound(CallbackInfo info) {
        if (! (this.trader instanceof Entity)) info.cancel();
    }
}
