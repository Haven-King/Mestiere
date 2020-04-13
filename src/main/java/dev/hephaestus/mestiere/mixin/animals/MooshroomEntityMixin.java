package dev.hephaestus.mestiere.mixin.animals;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.InventoryGetter;
import dev.hephaestus.mestiere.util.MestiereComponent;
import dev.hephaestus.mestiere.util.SexedEntity;
import dev.hephaestus.mestiere.util.Skills;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MooshroomEntity.class)
public class MooshroomEntityMixin extends CowEntity {
    int timeToMilk = 0;

    public MooshroomEntityMixin(EntityType<? extends CowEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"), cancellable = true)
    public void milkOnlyFemales(PlayerEntity player, Hand hand, CallbackInfoReturnable<Boolean> cir) {
        if (((SexedEntity)this).getSex() == SexedEntity.Sex.MALE || this.timeToMilk > 0) {
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
                int s = (int) ((InventoryGetter) serverPlayerEntity.inventory).getInventory().stream()
                        .mapToLong(List::size).sum();
                for (int i = 0; i < s; ++i) {
                    serverPlayerEntity.networkHandler.sendPacket(new ContainerSlotUpdateS2CPacket(-2, serverPlayerEntity.inventory.selectedSlot, serverPlayerEntity.inventory.getInvStack(serverPlayerEntity.inventory.selectedSlot)));
                    serverPlayerEntity.networkHandler.sendPacket(new ContainerSlotUpdateS2CPacket(-2, i, serverPlayerEntity.inventory.getInvStack(i)));
                    serverPlayerEntity.networkHandler.sendPacket(new HeldItemChangeS2CPacket(serverPlayerEntity.inventory.selectedSlot));
                }
            }
            cir.setReturnValue(false);
        } else {
            if (player instanceof ServerPlayerEntity)
                Mestiere.COMPONENT.get(player).addXp(Skills.FARMING, 4);

            this.timeToMilk = 14000;
        }
    }

    @Inject(method = "readCustomDataFromTag", at = @At("TAIL"))
    public void readCustomDataFromTag(CompoundTag tag, CallbackInfo ci) {
        this.timeToMilk = tag.getInt(Mestiere.MOD_ID + ".time_to_milk");
    }

    @Inject(method = "writeCustomDataToTag", at = @At("TAIL"))
    public void writeCustomDataToTag(CompoundTag tag, CallbackInfo ci) {
        tag.putInt(Mestiere.MOD_ID + ".time_to_milk", this.timeToMilk);
    }

    @Override
    protected void mobTick() {
        super.mobTick();
        this.timeToMilk = Math.max(0, this.timeToMilk - 1);
    }

    @Shadow
    @Override
    public MooshroomEntity createChild(PassiveEntity mate) {
        return null;
    }
}
