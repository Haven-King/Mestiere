package dev.hephaestus.mestiere.crafting;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.skills.SkillPerk;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class SkillRecipe implements Recipe<BasicInventory>, Comparable {
    private final Component[] components;
    private final ItemStack outputItem;
    private final Skill skill;
    private final int value;
    private final SkillPerk perk;
    private final Identifier id;

    private PlayerEntity player;

    private final IntList[] stacks;

    public SkillRecipe(Identifier id, Skill skill, SkillPerk perk, ItemStack outputItem, int value, Component[] components) {
        this.components = components;
        this.outputItem = outputItem;
        this.skill = skill;
        this.value = value;
        this.perk = perk;
        this.id = id;

        this.stacks = new IntList[components.length];
    }

    private SkillRecipe(SkillRecipe recipe, PlayerEntity player) {
        this.components = recipe.components;
        this.outputItem = recipe.outputItem;
        this.skill = recipe.skill;
        this.value = recipe.value;
        this.perk = recipe.perk;
        this.id = recipe.id;
        this.player = player;

        this.stacks = new IntList[components.length];
    }

    public SkillRecipe withPlayer(PlayerEntity player) {
        return new SkillRecipe(this, player);
    }

    @Override
    public boolean matches(BasicInventory inv, World world) {
        if (inv.getInvSize() <= components.length) return false;

        for (int i = 0; i < components.length; ++i) {
            ItemStack stack = inv.getInvStack(i+1);
            if (!components[i].ingredient.test(stack) ||
                stack.getCount() < components[i].count)
                return false;
        }

        return true;
    }

    @Override
    public ItemStack craft(BasicInventory inv) {
        for (int i = 0; i < components.length; ++i) {
            inv.getInvStack(i).decrement(components[i].count);
        }

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

    public int numberOfComponents() {
        return components.length;
    }

    public Component[] getComponents() {
        return components;
    }

    public boolean canCraft(PlayerEntity player) {
        if (!Mestiere.COMPONENT.get(player).hasPerk(perk) && Mestiere.CONFIG.hardcoreProgression && this.perk.hardcore)
            return false;

        boolean[] canCraft = new boolean[components.length];

        for (ItemStack stack : player.inventory.main) {
            for (int i = 0; i < components.length; ++i) {
                if (components[i].ingredient.test(stack) && stack.getCount() >= components[i].count)
                    canCraft[i] = true;
            }
        }

        for (boolean b : canCraft)
            if (!b) return false;

        return true;
    }

    @Override
    public int compareTo(Object o) {
        int result = 0;

        if (o instanceof SkillRecipe) {
            result = -Boolean.compare(canCraft(player), ((SkillRecipe) o).canCraft(player));
            result = result == 0 ? Integer.compare(this.value, ((SkillRecipe)o).getValue()) : result;
            result = result == 0 ? id.compareTo(((SkillRecipe) o).id) : result;
        }
        return result;
    }

    public static class Type implements RecipeType<SkillRecipe> {
        public static final Type INSTANCE = new Type();
        public static final Identifier ID = Mestiere.newID("skill_recipe");
    }

    @Environment(EnvType.CLIENT)
    public ItemStack getItem(int i, float deltaTick) {
        if (stacks[i] == null && !components[i].ingredient.isEmpty())
            this.stacks[i] = components[i].ingredient.getIds();

        return new ItemStack(Registry.ITEM.get(stacks[i].getInt((int) ((deltaTick / 20) % stacks[i].size()))),
                components[i].count);
    }

    public static class Component {
        public final Ingredient ingredient;
        public final int count;

        public Component(Ingredient ingredient, int count) {
            this.ingredient = ingredient;
            this.count = count;
        }
    }
}
