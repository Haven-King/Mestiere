package dev.hephaestus.mestiere.mixin.animals;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.SexedEntity;
import dev.hephaestus.mestiere.skills.Skills;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.HeldItemChangeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CowEntity.class)
public class CowEntityMixin extends AnimalEntity {
    private int timeToMilk = 0;

    protected CowEntityMixin(EntityType<? extends AnimalEntity> type, World world) {
        super(type, world);
    }

    // TODO: Make client not have buggy milk bucket when milking a male cow.
    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V"), cancellable = true)
    public void milkOnlyFemales(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> cir) {
        if (((SexedEntity)this).getSex() == SexedEntity.Sex.MALE || this.timeToMilk > 0) {
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
                int s = serverPlayerEntity.inventory.main.size();
                for (int i = 0; i < s; ++i) {
                    serverPlayerEntity.networkHandler.sendPacket(new ContainerSlotUpdateS2CPacket(-2, i, serverPlayerEntity.inventory.getInvStack(i)));
                }

                serverPlayerEntity.networkHandler.sendPacket(new HeldItemChangeS2CPacket(serverPlayerEntity.inventory.selectedSlot));
            }
            cir.setReturnValue(false);
        } else {
            if (player instanceof ServerPlayerEntity)
                Mestiere.COMPONENT.get(player).addXp(Skills.FARMING, 2);

            this.timeToMilk = 14000;
        }
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        this.timeToMilk = Math.max(0, this.timeToMilk - 1);
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        this.timeToMilk = tag.getInt(Mestiere.MOD_ID + ".time_to_milk");
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);
        tag.putInt(Mestiere.MOD_ID + ".time_to_milk", this.timeToMilk);
    }

    @Shadow
    @Override
    public CowEntity createChild(PassiveEntity mate) {
        return null;
    }
}
