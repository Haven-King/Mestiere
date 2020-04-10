package dev.hephaestus.mestiere.skills;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.mestiere.Mestiere;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Optional;

public class SkillRecipeLoader {
    public static final Identifier ID = Mestiere.newID("skill_recipe");

    static class SkillRecipeJsonFormat {
        JsonObject firstIngredient;
        JsonObject secondIngredient;
        JsonObject outputItem;
        String skill;
        int value;
        int level_requirement;
    }

    public static void load() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Mestiere.newID("loader");
            }

            @Override
            public void apply(ResourceManager manager) {
                Collection<Identifier> resources = manager.findResources("skill_recipes", (string) -> string.endsWith(".json"));

                for (Identifier id : resources) {
                    try {
                        JsonParser JsonParser = new JsonParser();
                        JsonObject json = (JsonObject)JsonParser.parse(new InputStreamReader(manager.getResource(id).getInputStream()));

                        SkillRecipe recipe = read(id, json);
                        Mestiere.RECIPES.register(recipe.skill, recipe);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                for (Skill s : Mestiere.SKILLS) {
                    if (Mestiere.RECIPES.recipeCount(s) > 0)
                        Mestiere.log("Registered " + Mestiere.RECIPES.recipeCount(s) + " " + s.name + " recipes");
                }
            }
        });
    }

    private static ItemStack fromJson(JsonObject json) {
        if (json == null) return ItemStack.EMPTY;

        Optional<Item> ingredient = Registry.ITEM.getOrEmpty(new Identifier(json.get("item").getAsString()));
        if (ingredient.isPresent()) {
            int amount = json.get("amount").getAsInt();
            return new ItemStack(ingredient.get(), amount == 0 ? 1 : amount); // Amount is optional, default to 1
        } else {
            Mestiere.log("Problem registering recipe. Could not find item '" + json.get("item").getAsString() + "'");
            return ItemStack.EMPTY;
        }
    }

    private static SkillRecipe read(Identifier id, JsonObject json) {
        SkillRecipeJsonFormat recipeJson = new Gson().fromJson(json, SkillRecipeJsonFormat.class);

        ItemStack firstIngredient = fromJson(recipeJson.firstIngredient);
        ItemStack secondIngredient = fromJson(recipeJson.secondIngredient);
        ItemStack outputItem = fromJson(recipeJson.outputItem);

        SkillRecipe recipe = new SkillRecipe(
                recipeJson.value,   // This *can* be zero, so we don't need to do any validation
                Mestiere.SKILLS.get(Mestiere.newID(recipeJson.skill)),
                firstIngredient == null ? secondIngredient : firstIngredient,   // If the first ingredient doesn't exist, use the second
                firstIngredient == null ? ItemStack.EMPTY: secondIngredient,    // If the first ingredient doesn't exist, return EMPTY
                outputItem,
                id,
                recipeJson.level_requirement);

        Mestiere.log("Registered a new " + recipe.skill.name + " recipe: " + recipe.id);

        return recipe;
    }
}
