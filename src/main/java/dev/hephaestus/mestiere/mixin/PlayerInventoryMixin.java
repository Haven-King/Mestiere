package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.util.InventoryGetter;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements InventoryGetter {
    @Final @Shadow private List<DefaultedList<ItemStack>> combinedInventory;

    @Override
    public List<DefaultedList<ItemStack>> getInventory() {
        return this.combinedInventory;
    }
}
