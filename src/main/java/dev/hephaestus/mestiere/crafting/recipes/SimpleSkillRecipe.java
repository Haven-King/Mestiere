package dev.hephaestus.mestiere.crafting.recipes;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.hephaestus.mestiere.skills.Skill;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SimpleSkillRecipe extends Skill.Recipe {
    private final Identifier recipeType;

    private final ItemStack outputItem;
    private final IntList[] stacks;
    private final Component[] components;


    public SimpleSkillRecipe(Identifier id, Identifier recipeType, Skill skill, Skill.Perk perk, ItemStack outputItem, int value, Component[] components) {
        super(id, skill, perk, value);
        this.recipeType = recipeType;
        this.outputItem = outputItem;
        this.components = components;
        this.stacks = new IntList[components.length];
    }

    public SimpleSkillRecipe(Identifier id, PacketByteBuf buf) {
        super(id, buf);
        this.recipeType = buf.readIdentifier();
        this.outputItem = buf.readItemStack();
        components = new Component[buf.readInt()];
        stacks = new IntList[components.length];
        for (int i = 0; i < components.length; ++i) {
            components[i] = new Component(Ingredient.fromPacket(buf), buf.readInt());
            stacks[i] = components[i].getRawIds();
        }
    }

    public SimpleSkillRecipe(SimpleSkillRecipe recipe) {
        super(recipe);
        this.recipeType = recipe.recipeType;
        this.outputItem = recipe.getOutput();
        this.stacks = recipe.stacks;
        this.components = recipe.components;
    }

    @Override
    public boolean matches(BasicInventory blockInventory) {
        if (blockInventory.size() <= components.length) return false;

        for (int i = 0; i < components.length; ++i) {
            ItemStack stack = blockInventory.getStack(i+1);
            if (!components[i].matches(stack)) return false;
        }

        return true;
    }

    @Override
    public int numberOfInputs() {
        return components.length;
    }

    @Override
    public ItemStack getOutput(BasicInventory inv) {
        return getOutput();
    }

    @Override
    public ItemStack craft(BasicInventory inv) {
        for (int i = 0; i < components.length; ++i) {
            inv.getStack(i+1).decrement(components[i].count());
        }

        return this.getOutput().copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput() {
        return outputItem.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registry.RECIPE_SERIALIZER.get(recipeType);
    }

    @Override
    public RecipeType<?> getType() {
        return Registry.RECIPE_TYPE.get(recipeType);
    }

    public int numberOfComponents() {
        return components.length;
    }

    public Component[] getComponents() {
        return components;
    }

    @Override
    public void write(PacketByteBuf buf) {
        super.write(buf);
        buf.writeInt(numberOfComponents());
        for(Component component : getComponents()) {
            component.write(buf);
        }
    }

    @Override
    public boolean canCraft(PlayerEntity playerEntity) {
        boolean hasSkill = super.canCraft(playerEntity);
        if (!hasSkill) return false;

        boolean[] canCraft = new boolean[components.length];

        for (ItemStack stack : playerEntity.inventory.main) {
            for (int i = 0; i < components.length; ++i) {
                if (components[i].matches(stack))
                    canCraft[i] = true;
            }
        }

        for (boolean b : canCraft)
            if (!b) return false;

        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ItemStack getItem(int i, float deltaTick) {
        if (i < stacks.length && stacks[i] == null)
            this.stacks[i] = components[i].getRawIds();

        return new ItemStack(Registry.ITEM.get(stacks[i].getInt((int) ((deltaTick / 20) % stacks[i].size()))),
                components[i].count());
    }

    @Override
    public void fillInputSlots(PlayerInventory playerInventory, Inventory blockInventory) {
        if (blockInventory.size() <= components.length) return;

        IntList slotsToFill = new IntArrayList();
        for (int i = 0; i < components.length; ++i) {
            ItemStack stack = blockInventory.getStack(i+1);
            if (!components[i].matches(stack))
                slotsToFill.add(i+1);
        }

        for (int i = 0; i < playerInventory.main.size(); ++i) {
            ItemStack stack = playerInventory.getStack(i);
            for (Integer slot : slotsToFill) {
                if (components[slot-1].matches(stack)) {
                    ItemStack old = blockInventory.getStack(slot);
                    blockInventory.setStack(slot, stack);
                    playerInventory.setStack(i, old);
                    slotsToFill.remove(slot);
                }
            }
        }
    }

    public static class Serializer implements RecipeSerializer<SimpleSkillRecipe> {
        static class OutputItem {
            String item;
            int amount;
        }

        static class IngredientRecipeFormat {
            JsonArray ingredients;
            OutputItem outputItem;
            String type;
            int value;
            String perk_required;
        }

        @Override
        public SimpleSkillRecipe read(Identifier id, JsonObject json) {
            IngredientRecipeFormat ingredientRecipe = new Gson().fromJson(json, IngredientRecipeFormat.class);
            SimpleSkillRecipe.Component[] components = new SimpleSkillRecipe.Component[ingredientRecipe.ingredients.size()];
            for (int i = 0; i < ingredientRecipe.ingredients.size(); ++i) {
                Ingredient ingredient = Ingredient.fromJson(ingredientRecipe.ingredients.get(i).getAsJsonObject());
                int count = ingredientRecipe.ingredients.get(i).getAsJsonObject().get("amount").getAsInt();
                components[i] = new SimpleSkillRecipe.Component(ingredient, count);
            }

            Item outputItem = Registry.ITEM.getOrEmpty(Identifier.tryParse(ingredientRecipe.outputItem.item)).get();
            ItemStack output = new ItemStack(outputItem, ingredientRecipe.outputItem.amount);

            return new SimpleSkillRecipe(
                    id,
                    new Identifier(ingredientRecipe.type),
                    Skill.get(new Identifier(ingredientRecipe.type.split("\\.")[0])),
                    ingredientRecipe.perk_required == null ? Skill.Perk.NONE : Skill.Perk.get(new Identifier(ingredientRecipe.perk_required)),
                    output,
                    ingredientRecipe.value,   // This *can* be zero, so we don't need to do any validation,
                    components
            );
        }

        @Override
        public SimpleSkillRecipe read(Identifier id, PacketByteBuf buf) {
            return new SimpleSkillRecipe(id, buf);
        }

        @Override
        public void write(PacketByteBuf buf, SimpleSkillRecipe recipe) {
            recipe.write(buf);
        }
    }

    public static class Component extends Skill.Recipe.Component {
        public final Ingredient ingredient;
        public final int count;

        public Component(Ingredient ingredient, int count) {
            this.ingredient = ingredient;
            this.count = count;
        }

        @Override
        public IntList getRawIds() {
            return ingredient.getIds();
        }

        @Override
        public int count() {
            return count;
        }

        @Override
        public boolean matches(ItemStack stack) {
            return ingredient.test(stack) && stack.getCount() >= count;
        }

        @Override
        public void write(PacketByteBuf buf) {
            ingredient.write(buf);
            buf.writeInt(count);
        }
    }
}
