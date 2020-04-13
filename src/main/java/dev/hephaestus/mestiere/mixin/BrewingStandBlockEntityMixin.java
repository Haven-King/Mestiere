package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.SkilledExperienceOrbEntity;
import dev.hephaestus.mestiere.skills.Skills;
import dev.hephaestus.mestiere.util.MestiereConfig;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin extends LockableContainerBlockEntity {
    @Shadow private DefaultedList<ItemStack> inventory;

    int value = 0;

    protected BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Inject(method = "craft", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/DefaultedList;get(I)Ljava/lang/Object;", ordinal = 0))
    public void setValue(CallbackInfo ci) {
        this.value += MestiereConfig.alchemicalReagentValues.getOrDefault(this.inventory.get(3).getItem(), 0);
    }

    @Inject(method = "craft", at = @At("TAIL"))
    public void dropXP(CallbackInfo ci) {
        if (this.world != null)
            this.world.spawnEntity(new SkilledExperienceOrbEntity(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.value, Skills.ALCHEMY));
    }

    @Shadow @Override
    protected Text getContainerName() {
        return null;
    }

    @Shadow @Override
    protected Container createContainer(int i, PlayerInventory playerInventory) {
        return null;
    }

    @Shadow @Override
    public int getInvSize() {
        return 0;
    }

    @Shadow @Override
    public boolean isInvEmpty() {
        return false;
    }

    @Shadow @Override
    public ItemStack getInvStack(int slot) {
        return null;
    }

    @Shadow @Override
    public ItemStack takeInvStack(int slot, int amount) {
        return null;
    }

    @Shadow @Override
    public ItemStack removeInvStack(int slot) {
        return null;
    }

    @Shadow @Override
    public void setInvStack(int slot, ItemStack stack) {

    }

    @Shadow @Override
    public boolean canPlayerUseInv(PlayerEntity player) {
        return false;
    }

    @Shadow @Override
    public void clear() {

    }
}
