package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skills;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntityWithAi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

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
    protected void dropXp() {
        // We give XP right to the player when we die, so no orbs necessary!
    }
}
