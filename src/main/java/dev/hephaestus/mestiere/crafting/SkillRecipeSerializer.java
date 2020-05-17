package dev.hephaestus.mestiere.crafting;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.skills.SkillPerk;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public class SkillRecipeSerializer implements RecipeSerializer<SkillRecipe> {
    public static final SkillRecipeSerializer INSTANCE = new SkillRecipeSerializer();

    public static final Identifier ID = Mestiere.newID("skill_recipe");

    static class OutputItem {
        String item;
        int amount;
    }

    static class SkillRecipeJsonFormat {
        JsonObject firstIngredient;
        JsonObject secondIngredient;
        OutputItem outputItem;
        String skill;
        int value;
        String perk_required;
    }

    @Override
    public SkillRecipe read(Identifier id, JsonObject json) {
        SkillRecipeJsonFormat recipeJson = new Gson().fromJson(json, SkillRecipeJsonFormat.class);

        Ingredient firstIngredient = Ingredient.fromJson(recipeJson.firstIngredient);
        int firstIngredientCount = recipeJson.firstIngredient.get("amount").getAsInt();

        Ingredient secondIngredient = recipeJson.secondIngredient == null ? Ingredient.EMPTY : Ingredient.fromJson(recipeJson.secondIngredient);
        int secondIngredientCount = recipeJson.secondIngredient == null ? 0 : recipeJson.secondIngredient.get("amount").getAsInt();

        Item outputItem = Registry.ITEM.getOrEmpty(new Identifier(recipeJson.outputItem.item)).get();
        ItemStack output = new ItemStack(outputItem, recipeJson.outputItem.amount);

        return new SkillRecipe(
                id,
                Mestiere.SKILLS.get(Mestiere.newID(recipeJson.skill)),
                recipeJson.perk_required == null ? SkillPerk.NONE : Mestiere.PERKS.get(new Identifier(recipeJson.perk_required)),
                output,
                recipeJson.value,   // This *can* be zero, so we don't need to do any validation,
                new SkillRecipe.Component[] {
                    new SkillRecipe.Component(firstIngredient, firstIngredientCount),
                    new SkillRecipe.Component(secondIngredient, secondIngredientCount)
                });

    }

    @Override
    public SkillRecipe read(Identifier id, PacketByteBuf buf) {
        Skill skill = Mestiere.SKILLS.get(buf.readIdentifier());
        SkillPerk perk = Mestiere.PERKS.get(buf.readIdentifier());
        ItemStack output = buf.readItemStack();
        int value = buf.readInt();

        SkillRecipe.Component[] components = new SkillRecipe.Component[buf.readInt()];
        for (int i = 0; i < components.length; ++i) {
            components[i] = new SkillRecipe.Component(Ingredient.fromPacket(buf), buf.readInt());
        }

        return new SkillRecipe(
            id,
            skill,
            perk,
            output,
            value,
            components
        );
    }

    @Override
    public void write(PacketByteBuf buf, SkillRecipe recipe) {
        buf.writeIdentifier(recipe.getSkill().id);
        buf.writeIdentifier(recipe.getPerk().id);
        buf.writeItemStack(recipe.getOutput());
        buf.writeInt(recipe.getValue());
        buf.writeInt(recipe.numberOfComponents());

        for(SkillRecipe.Component component : recipe.getComponents()) {
            component.ingredient.write(buf);
            buf.writeInt(component.count);
        }
    }
}
