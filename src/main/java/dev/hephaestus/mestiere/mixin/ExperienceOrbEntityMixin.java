package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.SkilledExperienceOrbEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @Shadow private int amount;

    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;addExperience(I)V"))
    public void addSkillXP(PlayerEntity player, CallbackInfo ci) {
        if ((Object)this instanceof SkilledExperienceOrbEntity) {
            Mestiere.COMPONENT.get(player).addXp(((SkilledExperienceOrbEntity)(Object)(this)).skill, this.amount);
        }
    }
}
