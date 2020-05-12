package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.skills.SkillPerk;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class SkillRecipe implements Recipe<SkillCrafterInventory> {
    private final Ingredient firstIngredient;
    private final int firstIngredientCount;
    private final Ingredient secondIngredient;
    private final int secondIngredientCount;
    private final ItemStack outputItem;
    private final Skill skill;
    private final int value;
    private final SkillPerk perk;
    private final Identifier id;

    public SkillRecipe(Ingredient firstIngredient, int firstIngredientCount, Ingredient secondIngredient, int secondIngredientCount, ItemStack outputItem, Skill skill, int value, SkillPerk perk, Identifier id) {
        this.firstIngredient = firstIngredient;
        this.firstIngredientCount = firstIngredientCount;
        this.secondIngredient = secondIngredient;
        this.secondIngredientCount = secondIngredientCount;
        this.outputItem = outputItem;
        this.skill = skill;
        this.value = value;
        this.perk = perk;
        this.id = id;
    }

    public Ingredient getFirstIngredient() {
        return firstIngredient;
    }
    public int getFirstIngredientCount() { return firstIngredientCount; }

    public Ingredient getSecondIngredient() {
        return secondIngredient;
    }
    public int getSecondIngredientCount() { return secondIngredientCount; }


    @Override
    public boolean matches(SkillCrafterInventory inv, World world) {
        if (inv.getInvSize() < 2) return false;
        return  firstIngredient.test(inv.getInvStack(1)) && secondIngredient.test(inv.getInvStack(2)) &&
                inv.getInvStack(1).getCount() > firstIngredientCount &&
                inv.getInvStack(2).getCount() > secondIngredientCount;
    }

    @Override
    public ItemStack craft(SkillCrafterInventory inv) {
        inv.getInvStack(1).decrement(firstIngredientCount);
        inv.getInvStack(2).decrement(secondIngredientCount);
        return this.getOutput().copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput() {
        return this.outputItem.copy();
    }

    @Override
    public Identifier getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SkillRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public int getValue() {
        return value;
    }

    public SkillPerk getPerk() {
        return perk;
    }

    public Skill getSkill() {
        return skill;
    }

    public boolean canCraft(PlayerEntity player) {
        if (!Mestiere.COMPONENT.get(player).hasPerk(perk) && Mestiere.CONFIG.hardcoreProgression && this.perk.hardcore)
            return false;

        boolean has1 = false;
        boolean has2 = false;
        for (ItemStack stack : player.inventory.main) {
            if (firstIngredient.test(stack) && stack.getCount() >= firstIngredientCount) has1 = true;
            if (secondIngredient.test(stack) && stack.getCount() >= secondIngredientCount) has2 = true;
        }

        return has1 && has2;
    }

    public static class Type implements RecipeType<SkillRecipe> {
        public static final Type INSTANCE = new Type();
        public static final Identifier ID = Mestiere.newID("skill_recipe");
    }
}
