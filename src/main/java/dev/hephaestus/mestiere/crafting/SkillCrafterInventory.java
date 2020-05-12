package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ContainerSlotUpdateS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DefaultedList;

public class SkillCrafterInventory implements Inventory {
    private final DefaultedList<ItemStack> slots;
    private Container container;

    private SkillRecipe recipe;

    public SkillCrafterInventory(int numberOfInputs) {
        this.slots = DefaultedList.ofSize(numberOfInputs + 1, ItemStack.EMPTY);
    }

    @Override
    public int getInvSize() {
        return slots.size() - 1;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public boolean isInvEmpty() {
        for (int i = 1; i < slots.size(); ++i) {
            if (!slots.get(i).isEmpty())
                return false;
        }

        return true;
    }

    @Override
    public ItemStack getInvStack(int slot) {
        return slot >= this.getInvSize() ? ItemStack.EMPTY : this.slots.get(slot);
    }

    @Override
    public ItemStack takeInvStack(int slot, int amount) {
        if (slot == 0) {
            takeInvStack(1, this.recipe.getFirstIngredientCount());
            takeInvStack(2, this.recipe.getSecondIngredientCount());
            return Inventories.removeStack(this.slots, 0);
        } else {
            ItemStack itemStack = Inventories.splitStack(this.slots, slot, amount);
            if (!itemStack.isEmpty()) {
                this.container.onContentChanged(this);
            }

            return itemStack;
        }
    }

    @Override
    public ItemStack removeInvStack(int slot) {
        return Inventories.removeStack(this.slots, slot);
    }

    @Override
    public void setInvStack(int slot, ItemStack stack) {
        this.slots.set(slot, stack);
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
        this.slots.clear();
    }

    public ActionResult validateRecipe(SkillRecipe recipe) {
        if (recipe.matches(this, null)) {
            this.recipe = recipe;
            ItemStack stack = recipe.getOutput();
            setInvStack(0, stack);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.PASS;
        }
    }
}
