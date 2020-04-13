package dev.hephaestus.mestiere.mixin.animals;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skills;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SheepEntity.class)
public class SheepEntityMixin {
    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    public void addXP(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> cir) {
        Mestiere.COMPONENT.get(player).addXp(Skills.FARMING, 2);
    }
}
