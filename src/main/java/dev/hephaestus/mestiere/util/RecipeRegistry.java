package dev.hephaestus.mestiere.util;

import com.google.common.collect.ImmutableList;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.skills.SkillRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeRegistry {
    private HashMap<Skill, ArrayList<SkillRecipe>> recipes = new HashMap<>();

    public SkillRecipe register(Skill skill, SkillRecipe recipe) {
        recipes.putIfAbsent(skill, new ArrayList<>());
        recipes.get(skill).add(recipe);

        return recipe;
    }

    public ImmutableList<SkillRecipe> getRegistered(Skill skill) {
        return ImmutableList.copyOf(recipes.getOrDefault(skill, new ArrayList<>()));
    }

    public Iterator<SkillRecipe> getRegistered() {
        return ImmutableList.copyOf(
                recipes.values().stream().flatMap(List::stream).collect(Collectors.toList())
        ).iterator();
    }

    public int recipeCount() {
        return (int) recipes.values().stream().mapToLong(List::size).sum();
    }

    public int recipeCount(Skill skill) {
        return recipes.getOrDefault(skill, new ArrayList<>()).size();
    }
}
