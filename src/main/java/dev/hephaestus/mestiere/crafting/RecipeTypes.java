package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RecipeTypes {
    public final RecipeType netherite;// = Registry.register(Registry.RECIPE_TYPE, IDENTIFIERS.NETHERITE, new RecipeType<NetheriteRecipe>() {});
    public final RecipeType leatherworking;// = Registry.register(Registry.RECIPE_TYPE, IDENTIFIERS.LEATHERWORKING, new RecipeType<SimpleSkillRecipe>() {});
    public final RecipeType armor;// = Registry.register(Registry.RECIPE_TYPE, IDENTIFIERS.ARMOR, new RecipeType<SimpleSkillRecipe>() {});
    public final RecipeType tools;// = Registry.register(Registry.RECIPE_TYPE, IDENTIFIERS.TOOLS, new RecipeType<SimpleSkillRecipe>() {});

    private RecipeTypes() {
        netherite = register(Mestiere.newID("netherite"), new RecipeType<NetheriteRecipe>() {}, NetheriteRecipe.SERIALIZER);
        leatherworking = register(Mestiere.newID("leatherworking"), new RecipeType<SimpleSkillRecipe>() {}, new SimpleSkillRecipe.Serializer());
        armor = register(Mestiere.newID("smithing.armor"), new RecipeType<SimpleSkillRecipe>() {}, new SimpleSkillRecipe.Serializer());
        tools = register(Mestiere.newID("smithing.tools"), new RecipeType<SimpleSkillRecipe>() {}, new SimpleSkillRecipe.Serializer());
    }

    public static RecipeTypes init() {
        return new RecipeTypes();
    }

    private RecipeType register(Identifier id, RecipeType<?> type, RecipeSerializer<?> serializer) {
        Registry.register(Registry.RECIPE_SERIALIZER, id, serializer);
        return Registry.register(Registry.RECIPE_TYPE, id, type);
    }
}
