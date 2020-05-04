package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skills;
import dev.hephaestus.mestiere.util.MestiereComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HostileEntity.class)
public class HostileEntityMixin extends MobEntityWithAi {
    protected HostileEntityMixin(EntityType<? extends MobEntityWithAi> type, World world) {
        super(type, world);
    }

    @Override
    public void onDeath(DamageSource source) {
        super.onDeath(source);
        if (source.getAttacker() instanceof ServerPlayerEntity) {
            Mestiere.COMPONENT.get(source.getAttacker()).addXp(
                Skills.SLAYING,
                this.getCurrentExperience((PlayerEntity) source.getAttacker()) + (int)this.getPos().distanceTo(source.getAttacker().getPos())/10 + (int)(this.getVelocity().length()*2.5)
            );
        }
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        super.dropLoot(source, causedByPlayer);

        if (causedByPlayer && source.getAttacker() != null && source.getAttacker().getEntityWorld() instanceof ServerWorld) {
            ServerPlayerEntity player = (ServerPlayerEntity) source.getAttacker();
            float chance = Mestiere.COMPONENT.get(player).getScale(Mestiere.PERKS.get("slaying.slayer")) / 2.0f;
            if (Math.random() > chance) {
                super.dropLoot(source, true);
            }
        }
    }

    @Redirect(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntityWithAi;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    public boolean addDamage(MobEntityWithAi mobEntityWithAi, DamageSource source, float amount) {
        float damage = amount;
        if (source.getAttacker() instanceof ServerPlayerEntity) {
            MestiereComponent component = Mestiere.COMPONENT.get(source.getAttacker());

            if (component.hasPerk(Mestiere.newID("slaying.sniper"))) {
                damage += (mobEntityWithAi.getPos().distanceTo(source.getAttacker().getPos()) / 20) * (1.0 + component.getScale(Mestiere.PERKS.get("slaying.sniper")));
            }
        }

        return super.damage(source, damage);
    }

    @Override
    protected void dropXp() {
        // We give XP right to the player when we die, so no orbs necessary!
    }
}
