package dev.hephaestus.mestiere.mixin.animals;

import dev.hephaestus.mestiere.util.SexedEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CowEntity.class)
public class CowEntityMixin {
    // TODO: Make client not have buggy milk bucket when milking a male cow.
    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"), cancellable = true)
    public void milkOnlyFemales(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> cir) {
        if (((SexedEntity)this).getSex() == SexedEntity.Sex.MALE)
            player.inventory.updateItems();
            cir.setReturnValue(false);
    }
}
