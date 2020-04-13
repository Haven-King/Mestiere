package dev.hephaestus.mestiere.mixin.animals;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.SexedEntity;
import dev.hephaestus.mestiere.skills.Skills;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
            Mestiere.COMPONENT.get(source.getAttacker()).addXp(Skills.HUNTING, 3 * this.getCurrentExperience((PlayerEntity) source.getAttacker()));
        }
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
