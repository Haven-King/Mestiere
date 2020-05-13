package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DefaultedList;

public class SkillCrafterInventory implements Inventory {
    private final DefaultedList<ItemStack> stacks;
    private final int width;
    private Container container;

    public SkillCrafterInventory(int numberOfInputs) {
        this.width = numberOfInputs;
        this.stacks = DefaultedList.ofSize(width, ItemStack.EMPTY);
    }

    @Override
    public int getInvSize() {
        return width;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public boolean isInvEmpty() {
        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty())
                return false;
        }

        return true;
    }

    @Override
    public ItemStack getInvStack(int slot) {
        return slot >= this.getInvSize() ? ItemStack.EMPTY : this.stacks.get(slot);
    }

    @Override
    public ItemStack takeInvStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.stacks, slot, amount);
        if (!itemStack.isEmpty()) {
            this.container.onContentChanged(this);
        }

        return itemStack;
    }

    @Override
    public ItemStack removeInvStack(int slot) {
        return Inventories.removeStack(this.stacks, slot);
    }

    @Override
    public void setInvStack(int slot, ItemStack stack) {
        Mestiere.debug("Set slot %d to %s", slot, stack.toString());
        this.stacks.set(slot, stack);
        this.container.onContentChanged(this);
    }

    @Override
    public void markDirty() {

    }

    @Override
    public boolean canPlayerUseInv(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.stacks.clear();
    }
}
