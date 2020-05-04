package dev.hephaestus.mestiere.skills;

import com.google.common.collect.ImmutableList;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class Recipes {
    private final HashMap<Skill, ArrayList<SkillRecipe>> recipes = new HashMap<>();

    public void register(Skill skill, SkillRecipe recipe) {
        recipes.putIfAbsent(skill, new ArrayList<>());
        recipes.get(skill).add(recipe);

    }

    public static Recipes init() {
        Recipes instance = new Recipes();
        Loader.load(instance);
        return instance;
    }

    public ImmutableList<SkillRecipe> getRegistered(Skill skill) {
        return ImmutableList.copyOf(recipes.getOrDefault(skill, new ArrayList<>()));
    }

    public int recipeCount(Skill skill) {
        return recipes.getOrDefault(skill, new ArrayList<>()).size();
    }

    public static class Loader {

        static class SkillRecipeJsonFormat {
            JsonObject firstIngredient;
            JsonObject secondIngredient;
            JsonObject outputItem;
            String skill;
            int value;
            String perk_required;
        }

        public static void load(Recipes recipes) {
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
                            recipes.register(recipe.skill, recipe);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    for (Skill s : Mestiere.SKILLS) {
                        if (recipes.recipeCount(s) > 0)
                            Mestiere.log("Registered " + recipes.recipeCount(s) + " " +   s.id + " recipes");
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

            return new SkillRecipe(
                    recipeJson.value,   // This *can* be zero, so we don't need to do any validation
                    Mestiere.SKILLS.get(Mestiere.newID(recipeJson.skill)),
                    firstIngredient == null ? secondIngredient : firstIngredient,   // If the first ingredient doesn't exist, use the second
                    firstIngredient == null ? ItemStack.EMPTY: secondIngredient,    // If the first ingredient doesn't exist, return EMPTY
                    outputItem,
                    id,
                    recipeJson.perk_required == null ? SkillPerk.NONE : Mestiere.PERKS.get(new Identifier(recipeJson.perk_required)));
        }
    }
}
