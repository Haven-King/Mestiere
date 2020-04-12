package dev.hephaestus.mestiere.mixin.animals;

import dev.hephaestus.mestiere.util.SexedEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MooshroomEntity.class)
public class MooshroomEntityMixin {
    // TODO: Make client not have buggy soup bowl when milking a male cow.
    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"), cancellable = true)
    public void milkOnlyFemales(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> cir) {
        if (((SexedEntity)this).getSex() == SexedEntity.Sex.MALE)
            cir.setReturnValue(false);
    }
}
