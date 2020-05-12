package dev.hephaestus.mestiere.crafting;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.hephaestus.mestiere.Mestiere;
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
                firstIngredient,
                firstIngredientCount, secondIngredient,
                secondIngredientCount, output,
                Mestiere.SKILLS.get(Mestiere.newID(recipeJson.skill)),
                recipeJson.value,   // This *can* be zero, so we don't need to do any validation
                recipeJson.perk_required == null ? SkillPerk.NONE : Mestiere.PERKS.get(new Identifier(recipeJson.perk_required)),
                id);

    }

    @Override
    public SkillRecipe read(Identifier id, PacketByteBuf buf) {
        return new SkillRecipe(
                Ingredient.fromPacket(buf),
                buf.readInt(), Ingredient.fromPacket(buf),
                buf.readInt(), buf.readItemStack(),
                Mestiere.SKILLS.get(buf.readIdentifier()),
                buf.readInt(),
                Mestiere.PERKS.get(buf.readIdentifier()),
                id
        );
    }

    @Override
    public void write(PacketByteBuf buf, SkillRecipe recipe) {
        recipe.getFirstIngredient().write(buf);
        buf.writeInt(recipe.getFirstIngredientCount());
        recipe.getSecondIngredient().write(buf);
        buf.writeInt(recipe.getSecondIngredientCount());
        buf.writeItemStack(recipe.getOutput());
        buf.writeIdentifier(recipe.getSkill().id);
        buf.writeInt(recipe.getValue());
        buf.writeIdentifier(recipe.getPerk().id);
    }
}
