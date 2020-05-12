package dev.hephaestus.mestiere.crafting;

import net.minecraft.container.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeFinder;

public class SkillCraftingContainer extends CraftingContainer<CraftingInventory> {
    private final CraftingInventory craftingInventory;
    private final CraftingResultInventory resultInventory;
    private final PlayerEntity player;

    public SkillCraftingContainer(int syncId, PlayerInventory playerInventory) {
        super(ContainerType.CRAFTING, syncId);
        this.craftingInventory = new CraftingInventory(this, 2, 1);
        this.resultInventory = new CraftingResultInventory();
        this.player = playerInventory.player;
        this.addSlot(new CraftingResultSlot(player, this.craftingInventory, this.resultInventory, 0, 124, 35));

        // Our slots!
        for (int input = 0; input < craftingInventory.getWidth(); ++input)
            this.addSlot(new Slot(this.craftingInventory, input, 30 + input * 18, 35));


        // Player's 9x3 inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player's hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void populateRecipeFinder(RecipeFinder recipeFinder) {
        this.craftingInventory.provideRecipeInputs(recipeFinder);
    }

    @Override
    public void clearCraftingSlots() {
        this.craftingInventory.clear();
        this.resultInventory.clear();
    }

    @Override
    public boolean matches(Recipe<? super CraftingInventory> recipe) {
        return false;
    }

    @Override
    public int getCraftingResultSlotIndex() {
        return 0;
    }

    @Override
    public int getCraftingWidth() {
        return 0;
    }

    @Override
    public int getCraftingHeight() {
        return 0;
    }

    @Override
    public int getCraftingSlotCount() {
        return 0;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return false;
    }
}
