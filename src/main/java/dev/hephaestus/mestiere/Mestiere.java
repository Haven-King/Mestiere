package dev.hephaestus.mestiere;

import com.chocohead.mm.api.ClassTinkerers;
import dev.hephaestus.mestiere.crafting.SkillCrafter;
import dev.hephaestus.mestiere.crafting.recipes.NetheriteRecipe;
import dev.hephaestus.mestiere.crafting.recipes.SimpleSkillRecipe;
import dev.hephaestus.mestiere.skills.MaterialCraftingPerk;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.util.Commands;
import dev.hephaestus.mestiere.util.MestiereComponent;
import dev.hephaestus.mestiere.util.MestiereConfig;
import dev.hephaestus.mestiere.util.NoRecipe;
import io.github.fablabsmc.fablabs.api.gamerule.v1.RuleFactory;
import io.github.fablabsmc.fablabs.mixin.gamerule.GameRulesAccessor;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class Mestiere implements ModInitializer {
	public static final String MOD_ID = "mestiere";
	public static final String MOD_NAME = "Mestiere";
	public static final Logger LOGGER = LogManager.getLogger();

	public static final GameRules.RuleKey<GameRules.BooleanRule> HARDCORE = GameRulesAccessor.invokeRegister("mestiere.hardcoreMode", ClassTinkerers.getEnum(GameRules.RuleCategory.class, "mestiere"), RuleFactory.createBooleanRule(true));

	public static final UUID[] ARMOR_MODIFIERS = new UUID[]{UUID.fromString("CAFECAFE-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("CAFECAFE-0E66-4726-AB29-64469D734E0D"), UUID.fromString("CAFECAFE-C118-4544-8365-64846904B48E"), UUID.fromString("CAFECAFE-FEE1-4E67-B886-69FD380BB150")};
	public static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.fromString("CAFECAFE-645C-4F38-A497-9C13A33DB5CF");
	public static final UUID ATTACK_SPEED_MODIFIER_UUID = UUID.fromString("CAFECAFE-4180-4865-B01B-BCCE9785ACA3");

	public static MestiereConfig CONFIG;

	public static final Identifier SELECT_RECIPE_ID = newID("select_recipe");
	public static final Identifier OPEN_ALT_SCREEN = newID("open_alt_screen");
	public static final Identifier OPEN_CRAFT_SCREEN = newID("open_craft_screen");

	public static final ComponentType<MestiereComponent> COMPONENT =
		ComponentRegistry.INSTANCE.registerIfAbsent(newID("component"), MestiereComponent.class);

	static {
		EntityComponents.setRespawnCopyStrategy(COMPONENT, RespawnCopyStrategy.ALWAYS_COPY);
	}

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(Commands::register);

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

		ServerSidePacketRegistry.INSTANCE.register(OPEN_ALT_SCREEN, (packetContext, packetByteBuf) -> {
			int syncId = packetByteBuf.readInt();

			packetContext.getTaskQueue().execute(() -> {
				dev.hephaestus.mestiere.crafting.SkillCrafter controller = dev.hephaestus.mestiere.crafting.SkillCrafter.getInstance(syncId);
				controller.openAltScreen();
			});
		});

		ServerSidePacketRegistry.INSTANCE.register(OPEN_CRAFT_SCREEN, (packetContext, packetByteBuf) -> {
			Identifier blockId = packetByteBuf.readIdentifier();
			BlockPos pos = packetByteBuf.readBlockPos();
			PlayerEntity player = packetContext.getPlayer();

			packetContext.getTaskQueue().execute(() -> ContainerProviderRegistry.INSTANCE.openContainer(
					blockId, player, (buf -> buf.writeBlockPos(pos))
			));
		});

		Registry.register(Registry.RECIPE_SERIALIZER, newID("no_recipe"), NoRecipe.SERIALIZER);
		Registry.register(Registry.RECIPE_TYPE, newID("no_recipe"), NoRecipe.TYPE);

		Registry.register(Registry.ITEM, newID("iron_chunk"), new Item(new Item.Settings().group(ItemGroup.MATERIALS)));
		Registry.register(Registry.ITEM, newID("gold_chunk"), new Item(new Item.Settings().group(ItemGroup.MATERIALS)));

		// Register Skills
		Skill.NONE = new Skill(Mestiere.newID("none"), Formatting.BLACK, ItemStack.EMPTY);
//		Skill.PRAYER = Skill.register(new Skill(newID("prayer"), Formatting.LIGHT_PURPLE, new ItemStack(Items.NETHER_STAR)));
		Skill.FARMING = Skill.register(new Skill(newID("farming"), Formatting.DARK_GREEN, new ItemStack(Items.IRON_HOE)));
		Skill.HUNTING = Skill.register(new Skill(newID("hunting"), Formatting.GREEN, new ItemStack(Items.BOW)));
		Skill.LEATHERWORKING = Skill.register(new Skill(newID("leatherworking"), Formatting.GOLD, new ItemStack(Items.LEATHER))).craftSound(SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER);
		Skill.MINING = Skill.register(new Skill(newID("mining"), Formatting.DARK_GRAY, new ItemStack(Items.IRON_PICKAXE)));
		Skill.SMITHING = Skill.register(new Skill(newID("smithing"), Formatting.DARK_RED, new ItemStack(Items.SMITHING_TABLE))).craftSound(SoundEvents.BLOCK_ANVIL_USE);
		Skill.SLAYING = Skill.register(new Skill(newID("slaying"), Formatting.RED, new ItemStack(Items.ZOMBIE_HEAD)));

		// Register Skill.Perks
		Skill.Perk.NONE = new Skill.Perk(Skill.NONE, "none", Integer.MIN_VALUE, null);
		Skill.Perk.INVALID = new Skill.Perk(Skill.NONE, "invalid", Integer.MAX_VALUE, null);

		Skill.Perk.IRON_INGOT_SMITH = Skill.Perk.register(new MaterialCraftingPerk(Skill.SMITHING, Items.IRON_INGOT, 10)
				.setMessage(new TranslatableText("perk.mestiere.smithing.material.iron_ingot.message"))
				.setDescription(new TranslatableText("perk.mestiere.smithing.material.iron_ingot.description"))
				.scales(20));

		Skill.Perk.GOLD_INGOT_SMITH = Skill.Perk.register(new MaterialCraftingPerk(Skill.SMITHING, Items.GOLD_INGOT, 20).scales(30));
		Skill.Perk.DIAMOND_SMITH = Skill.Perk.register(new MaterialCraftingPerk(Skill.SMITHING, Items.DIAMOND, 30));
		Skill.Perk.NETHERITE_SMITH = Skill.Perk.register(new MaterialCraftingPerk(Skill.SMITHING, Items.NETHERITE_INGOT, 35));

		Skill.Perk.LEATHERWORKING = Skill.Perk.register(new MaterialCraftingPerk(Skill.LEATHERWORKING, Items.LEATHER, 10)
				.setMessage(new TranslatableText("perk.mestiere.leatherworking.material.leather.message"))
				.setDescription(new TranslatableText("perk.mestiere.leatherworking.material.leather.description"))
				.scales(20));

		Skill.Perk.HUNTER = Skill.Perk.register(new Skill.Perk(Skill.HUNTING, "hunter", 5, Items.PORKCHOP)).scales(30);
		Skill.Perk.SHARP_SHOOTER = Skill.Perk.register(new Skill.Perk(Skill.HUNTING, "sharp_shooter", 15, Items.ARROW)).scales(30);

		Skill.Perk.GATHERER = Skill.Perk.register(new Skill.Perk(Skill.FARMING, "gatherer", 5, Items.GRASS).scales(15));
		Skill.Perk.SEX_GURU = Skill.Perk.register(new Skill.Perk(Skill.FARMING, "sex_guru", 10, Items.WHEAT));
		Skill.Perk.GREEN_THUMB = Skill.Perk.register(new Skill.Perk(Skill.FARMING, "green_thumb", 15, Items.WHEAT_SEEDS));

		Skill.Perk.SLAYER = Skill.Perk.register(new Skill.Perk(Skill.SLAYING, "slayer", 15, Items.ROTTEN_FLESH)).scales(30);
		Skill.Perk.SNIPER = Skill.Perk.register(new Skill.Perk(Skill.SLAYING, "sniper", 20, Items.ARROW)).scales(30);

		// SkillCrafter recipe types
		Skill.Recipe.Type.NETHERITE = Skill.Recipe.Type.register(Mestiere.newID("netherite"), new RecipeType<NetheriteRecipe>() {}, NetheriteRecipe.SERIALIZER);
		Skill.Recipe.Type.LEATHERWORKING = Skill.Recipe.Type.register(Mestiere.newID("leatherworking"), new RecipeType<SimpleSkillRecipe>() {}, new SimpleSkillRecipe.Serializer());
		Skill.Recipe.Type.ARMOR = Skill.Recipe.Type.register(Mestiere.newID("smithing.armor"), new RecipeType<SimpleSkillRecipe>() {}, new SimpleSkillRecipe.Serializer());
		Skill.Recipe.Type.TOOLS = Skill.Recipe.Type.register(Mestiere.newID("smithing.tools"), new RecipeType<SimpleSkillRecipe>() {}, new SimpleSkillRecipe.Serializer());

		CONFIG = MestiereConfig.init();

		// Register SkillCrafter providers
		SkillCrafter.Builder.registerContainer(Blocks.SMITHING_TABLE, Skill.SMITHING).addTypes(Skill.Recipe.Type.TOOLS, Skill.Recipe.Type.NETHERITE);
		SkillCrafter.Builder.registerContainer(Blocks.CAULDRON, Skill.LEATHERWORKING).addTypes(Skill.Recipe.Type.LEATHERWORKING);
		SkillCrafter.Builder.registerContainer(Blocks.BLAST_FURNACE, Skill.SMITHING).addTypes(Skill.Recipe.Type.ARMOR)
			.withAlt((world, pos) -> (NamedScreenHandlerFactory)world.getBlockEntity(pos), Items.BLAST_FURNACE);

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
