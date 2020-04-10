package dev.hephaestus.mestiere;

import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.skills.SkillRecipeLoader;
import dev.hephaestus.mestiere.util.*;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

import static org.apache.commons.lang3.reflect.FieldUtils.getField;

public class Mestiere implements ModInitializer {
	public static String MOD_ID = "mestiere";
	public static final Logger LOGGER = LogManager.getLogger();
	public static final boolean DEBUG = true;

	public static MestiereConfig CONFIG = new MestiereConfig();

	public static SkillRegistry SKILLS = new SkillRegistry();
	public static Skill SMITHING = SKILLS.register(new Skill(newID("smithing"), Formatting.DARK_RED, SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH));
	public static Skill LEATHERWORKING = SKILLS.register(new Skill(newID("leatherworking"), Formatting.GOLD, SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER));
	public static Skill FARMING = SKILLS.register(new Skill(newID("farming"), Formatting.DARK_GREEN));
	public static Skill SLAYING = SKILLS.register(new Skill(newID("slaying"), Formatting.RED));
	public static Skill HUNTING = SKILLS.register(new Skill(newID("hunting"), Formatting.GREEN));
	public static Skill MINING = SKILLS.register(new Skill(newID("mining"), Formatting.GRAY));

	public static RecipeRegistry RECIPES = new RecipeRegistry();

	public static final ComponentType<MestiereComponent> COMPONENT =
		ComponentRegistry.INSTANCE.registerIfAbsent(newID("component"), MestiereComponent.class);

	static {
		EntityComponents.setRespawnCopyStrategy(COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
	}


	@Override
	public void onInitialize() {
		SkillRecipeLoader.load();
		CommandRegistry.INSTANCE.register(false, Commands::register);

		EntityComponentCallback.event(ServerPlayerEntity.class).register((player, components) ->
				components.put(COMPONENT, new MestiereComponent(player)));

		try {
			Field hardnessField = getField(Block.class, "hardness");
			hardnessField.setAccessible(true);
			hardnessField.set(Blocks.COAL_ORE, 1.5F);
			hardnessField.set(Blocks.REDSTONE_ORE, 1.5F);
			hardnessField.set(Blocks.IRON_ORE, 1.5F);
			hardnessField.set(Blocks.LAPIS_ORE, 1.5F);
			hardnessField.set(Blocks.GOLD_ORE, 1.5F);
			hardnessField.set(Blocks.DIAMOND_ORE, 1.5F);
			hardnessField.set(Blocks.EMERALD_ORE, 1.5F);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

//		FibLib.register(DimensionType.OVERWORLD, Blocks.GRASS_BLOCK, (state, player) -> Blocks.STONE.getDefaultState());
	}

	public static void log(String msg) {
		LOGGER.info(String.format("[%s] %s", MOD_ID.substring(0, 1).toUpperCase() + MOD_ID.substring(1), msg));
	}

	public static void debug(String msg) {
		if (DEBUG) LOGGER.info(String.format("[%s] %s", MOD_ID.substring(0, 1).toUpperCase() + MOD_ID.substring(1), msg));
	}

	public static void debug(String format, Object ... args) {
		if (DEBUG) LOGGER.info(String.format("[%s] %s", MOD_ID.substring(0, 1).toUpperCase() + MOD_ID.substring(1), String.format(format, args)));
	}

	public static Identifier newID(String id) {
		return new Identifier(MOD_ID, id);
	}
}
