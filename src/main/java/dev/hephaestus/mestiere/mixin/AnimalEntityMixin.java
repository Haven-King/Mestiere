package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AnimalEntity.class)
public class AnimalEntityMixin extends PassiveEntity {
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
            Mestiere.COMPONENT.get(source.getAttacker()).addXp(Mestiere.HUNTING, 3 * this.getCurrentExperience((PlayerEntity) source.getAttacker()));
        }
    }
}
