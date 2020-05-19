package dev.hephaestus.mestiere;

import dev.hephaestus.mestiere.crafting.*;
import dev.hephaestus.mestiere.skills.MaterialSmithingPerk;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.skills.Skills;
import dev.hephaestus.mestiere.util.Commands;
import dev.hephaestus.mestiere.util.MestiereComponent;
import dev.hephaestus.mestiere.util.MestiereConfig;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mestiere implements ModInitializer {
	public static final String MOD_ID = "mestiere";
	public static final String MOD_NAME = "Mestiere";
	public static final Logger LOGGER = LogManager.getLogger();

	public static final Skills SKILLS = Skills.init();
	public static final MestiereConfig CONFIG = MestiereConfig.init();

	public static final Identifier SELECT_RECIPE_ID = newID("select_recipe");

	public static RecipeTypes TYPES;

	public static final ComponentType<MestiereComponent> COMPONENT =
		ComponentRegistry.INSTANCE.registerIfAbsent(newID("component"), MestiereComponent.class);

	public static Skill.Perk HUNTER;
	public static Skill.Perk SHARP_SHOOTER;
	public static Skill.Perk GATHERER;
	public static Skill.Perk SLAYER;
	public static Skill.Perk SNIPER;

	static {
		EntityComponents.setRespawnCopyStrategy(COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
	}


	@Override
	public void onInitialize() {
		CommandRegistry.INSTANCE.register(false, Commands::register);

		TYPES = RecipeTypes.init();

		EntityComponentCallback.event(ServerPlayerEntity.class).register((player, components) ->
				components.put(COMPONENT, new MestiereComponent(player)));

		ServerSidePacketRegistry.INSTANCE.register(SELECT_RECIPE_ID, (packetContext, packetByteBuf) -> {
			int syncId = packetByteBuf.readByte();
			Identifier recipeId = packetByteBuf.readIdentifier();
			PlayerEntity player = packetContext.getPlayer();

			if (player instanceof ServerPlayerEntity)
				packetContext.getTaskQueue().execute(() -> {
					dev.hephaestus.mestiere.crafting.SkillCrafter controller = dev.hephaestus.mestiere.crafting.SkillCrafter.getInstance(syncId);
					if (controller.setRecipe(recipeId) == ActionResult.PASS)
						controller.fillInputSlots();
				});
		});

		// Register Skill.Perks
		Skill.Perk.register(new MaterialSmithingPerk(10, Items.GOLD_INGOT));
		Skill.Perk.register(new MaterialSmithingPerk(20, Items.DIAMOND));
		Skill.Perk.register(new MaterialSmithingPerk(25, Items.NETHERITE_INGOT));

		HUNTER = Skill.Perk.register(new Skill.Perk(Skills.HUNTING, "hunter", 5, Items.PORKCHOP)).scales(30);
		Skill.Perk.register(new Skill.Perk(Skills.HUNTING, "sharp_shooter", 15, Items.ARROW)).scales(30);

		GATHERER = Skill.Perk.register(new Skill.Perk(Skills.FARMING, "gatherer", 5, Items.GRASS).scales(15));
		Skill.Perk.register(new Skill.Perk(Skills.FARMING, "sex_guru", 10, Items.WHEAT));
		Skill.Perk.register(new Skill.Perk(Skills.FARMING, "green_thumb", 15, Items.WHEAT_SEEDS));

		SLAYER = Skill.Perk.register(new Skill.Perk(Skills.SLAYING, "slayer", 15, Items.ROTTEN_FLESH)).scales(30);
		Skill.Perk.register(new Skill.Perk(Skills.SLAYING, "sniper", 20, Items.ARROW)).scales(30);

		// Register SkillCrafter providers
		SkillCrafter.Builder.registerContainer(Blocks.SMITHING_TABLE, Skills.SMITHING).addTypes(TYPES.netherite, TYPES.tools);
		SkillCrafter.Builder.registerAllContainers();
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
