package dev.hephaestus.mestiere.mixin.animals;

import dev.hephaestus.mestiere.SkilledExperienceOrbEntity;
import dev.hephaestus.mestiere.skills.Skills;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalMateGoal.class)
public class AnimalMateGoalMixin {
    @Final @Shadow protected AnimalEntity animal;
    @Final @Shadow protected World world;

    @Redirect(method = "breed", at = @At(value = "NEW", target = "net/minecraft/entity/ExperienceOrbEntity"))
    public ExperienceOrbEntity thing(World world, double x, double y, double z, int amount) {
        return new SkilledExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1, Skills.FARMING);
    }
}
