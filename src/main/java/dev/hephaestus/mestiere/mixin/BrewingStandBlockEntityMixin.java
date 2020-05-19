package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.SkilledExperienceOrbEntity;
import dev.hephaestus.mestiere.skills.Skills;
import dev.hephaestus.mestiere.util.MestiereConfig;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends LockableContainerBlockEntity {
    @Shadow private DefaultedList<ItemStack> inventory;

    int value = 0;

    protected BrewingStandBlockEntityMixin(BlockEntityType<?> blockEntityType) {
        super(blockEntityType);
    }

    @Inject(method = "craft", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/collection/DefaultedList;set(ILjava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
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
    public void clear() {

    }
}
