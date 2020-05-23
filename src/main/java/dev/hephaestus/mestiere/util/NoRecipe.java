package dev.hephaestus.mestiere.util;

import com.google.gson.JsonObject;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.crafting.recipes.NetheriteRecipe;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Optional;

public class NoRecipe implements Recipe<Inventory> {
	public static class Serializer implements RecipeSerializer<NoRecipe> {
		@Override
		public NoRecipe read(Identifier id, JsonObject json) {
			return new NoRecipe(id);
		}

		@Override
		public NoRecipe read(Identifier id, PacketByteBuf buf) {
			return new NoRecipe(id);
		}

		@Override
		public void write(PacketByteBuf buf, NoRecipe recipe) {

		}
	}
	public static final RecipeSerializer<NoRecipe> SERIALIZER = new Serializer();

	public static final RecipeType<NoRecipe> TYPE = new RecipeType<NoRecipe>() {};

	private final Identifier id;
	public NoRecipe(Identifier id) {
		this.id = id;
	}

	@Override
	public boolean matches(Inventory inv, World world) {
		return false;
	}

	@Override
	public ItemStack craft(Inventory inv) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean fits(int width, int height) {
		return false;
	}

	@Override
	public ItemStack getOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<?> getType() {
		return TYPE;
	}
}
