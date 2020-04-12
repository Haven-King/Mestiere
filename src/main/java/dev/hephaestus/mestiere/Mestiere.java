package dev.hephaestus.mestiere;

import dev.hephaestus.mestiere.skills.Perks;
import dev.hephaestus.mestiere.skills.Recipes;
import dev.hephaestus.mestiere.util.*;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

import static org.apache.commons.lang3.reflect.FieldUtils.getField;

public class Mestiere implements ModInitializer {
	public static final String MOD_ID = "mestiere";
	public static final String MOD_NAME = "Mestiere";
	public static final Logger LOGGER = LogManager.getLogger();
	public static final boolean DEBUG = true;

	public static final Skills SKILLS = Skills.init();
	public static final Perks PERKS = Perks.init();
	public static final MestiereConfig CONFIG = MestiereConfig.init();
	public static final Recipes RECIPES = Recipes.init();

	public static final ComponentType<MestiereComponent> COMPONENT =
		ComponentRegistry.INSTANCE.registerIfAbsent(newID("component"), MestiereComponent.class);

	static {
		EntityComponents.setRespawnCopyStrategy(COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
	}


	@Override
	public void onInitialize() {
		CommandRegistry.INSTANCE.register(false, Commands::register);

		EntityComponentCallback.event(ServerPlayerEntity.class).register((player, components) ->
				components.put(COMPONENT, new MestiereComponent(player)));
	}

	public static void log(String msg) {
		log("%s", msg);
	}

	public static void log(String format, Object... args) {
		LOGGER.info(String.format("[%s] %s", MOD_NAME, String.format(format, args)));
	}

	public static void debug(String msg) {
		debug("%s", msg);
	}

	public static void debug(String format, Object... args) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment())
			LOGGER.info(String.format("[%s] %s", MOD_NAME, String.format(format, args)));
	}

	public static Identifier newID(String id) {
		return new Identifier(MOD_ID, id);
	}
}
