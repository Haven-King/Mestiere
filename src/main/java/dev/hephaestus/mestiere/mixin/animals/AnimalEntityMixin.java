package dev.hephaestus.mestiere.mixin.animals;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.SkilledExperienceOrbEntity;
import dev.hephaestus.mestiere.skills.Skills;
import dev.hephaestus.mestiere.util.MestiereComponent;
import dev.hephaestus.mestiere.util.SexedEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnimalEntity.class)
public class AnimalEntityMixin extends PassiveEntity implements SexedEntity {
    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> type, World world) {
        super(type, world);
    }

    @Override
    public PassiveEntity createChild(PassiveEntity mate) {
        return null;
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (source.getAttacker() instanceof ServerPlayerEntity && !this.isBaby()) {
            Mestiere.COMPONENT.get(source.getAttacker()).addXp(
                    Skills.HUNTING,
                    this.getCurrentExperience((PlayerEntity) source.getAttacker()) + (int)this.getPos().distanceTo(source.getAttacker().getPos())/10 + (int)(this.getVelocity().length()*2.5)
            );
        }
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);

        if (causedByPlayer && source.getAttacker() != null && source.getAttacker().getEntityWorld() instanceof ServerWorld) {
            ServerPlayerEntity player = (ServerPlayerEntity) source.getAttacker();
            float chance = Mestiere.COMPONENT.get(player).getScale(Mestiere.HUNTER) / 3.0f;
            if (Math.random() > chance) {
                super.dropLoot(source, true);
            }
        }
    }

    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PassiveEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    public boolean addDamage(PassiveEntity passiveEntity, DamageSource source, float amount) {
        float damage = amount;
        if (source.getAttacker() instanceof ServerPlayerEntity) {
            MestiereComponent component = Mestiere.COMPONENT.get(source.getAttacker());

            if (component.hasPerk(Mestiere.newID("hunting.sharp_shooter"))) {
                damage += (passiveEntity.getPos().distanceTo(source.getAttacker().getPos()) / 20) * (1.0 + component.getScale(Mestiere.SHARP_SHOOTER));
            }
        }

        return super.damage(source, damage);
    }

    @Override
    protected void dropXp() {
        // We give XP right to the player when we die, so no orbs necessary!
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void assignSex(EntityType<? extends AnimalEntity> type, World world, CallbackInfo ci) {
        this.setSex(this.random.nextBoolean() ? Sex.FEMALE : Sex.MALE);
    }

    @Shadow public boolean isInLove() {
        return false;
    }

    @Inject(method = "canBreedWith", at = @At("TAIL"), cancellable = true)
    public void checkSex(AnimalEntity other, CallbackInfoReturnable<Boolean> cir) {
        // Sidebar: the isInLove() function should *totally* be isHorny()
        // Also, bee breeding in Minecraft is biologically wack, so I'm not even gonna attempt to fix it. They can have
        // the same mamallian breeding routines as everyone else.
        cir.setReturnValue(this.isInLove() && other.isInLove() && this.getSex() != ((SexedEntity)other).getSex());
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    public void writeSex(CompoundTag tag, CallbackInfo ci) {
        tag.putString(Mestiere.MOD_ID + ".sex", this.getSex().toString());
    }

    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    public void readSex(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(Mestiere.MOD_ID + ".sex"))
            this.sex = Sex.valueOf(tag.getString(Mestiere.MOD_ID + ".sex"));
        else
            this.sex = this.random.nextBoolean() ? Sex.FEMALE : Sex.MALE;
    }

    @Redirect(method = "breed", at = @At(value = "NEW", target = "net/minecraft/entity/ExperienceOrbEntity"))
    public ExperienceOrbEntity thing(World world, double x, double y, double z, int amount) {
        return new SkilledExperienceOrbEntity(world, x, y, z, world.getRandom().nextInt(7) + 1, Skills.FARMING);
    }

    private Sex sex;
    @Override
    public void setSex(Sex sex) {
        this.sex = sex;
    }

    @Override
    public Sex getSex() {
        return this.sex;
    }
}
