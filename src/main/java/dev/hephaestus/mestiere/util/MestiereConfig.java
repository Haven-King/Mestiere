package dev.hephaestus.mestiere.util;

import com.google.gson.*;
import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.MiningPerk;
import dev.hephaestus.mestiere.skills.Skills;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MestiereConfig {
    float levelModifier = 1.0f;
    public boolean hardcoreProgression = true;

    public static HashMap<Block, Integer> miningValues = new HashMap<Block, Integer>() {{
        put(Blocks.STONE, 1);
        put(Blocks.COAL_ORE, 4);
        put(Blocks.REDSTONE_ORE, 8);
        put(Blocks.IRON_ORE, 16);
        put(Blocks.LAPIS_ORE, 32);
        put(Blocks.GOLD_ORE, 64);
        put(Blocks.DIAMOND_ORE, 256);
        put(Blocks.EMERALD_ORE, 384);
    }};

    public static HashMap<Identifier, Formatting> messageFormatting = new HashMap<Identifier, Formatting>() {{
        put(Registry.BLOCK.getId(Blocks.COAL_ORE), Formatting.DARK_GRAY);
        put(Registry.BLOCK.getId(Blocks.REDSTONE_ORE), Formatting.DARK_RED);
        put(Registry.BLOCK.getId(Blocks.IRON_ORE), Formatting.YELLOW);
        put(Registry.BLOCK.getId(Blocks.LAPIS_ORE), Formatting.DARK_BLUE);
        put(Registry.BLOCK.getId(Blocks.GOLD_ORE), Formatting.GOLD);
        put(Registry.BLOCK.getId(Blocks.DIAMOND_ORE), Formatting.AQUA);
        put(Registry.BLOCK.getId(Blocks.EMERALD_ORE), Formatting.GREEN);

        put(Registry.ITEM.getId(Items.GOLD_INGOT), Formatting.GOLD);
        put(Registry.ITEM.getId(Items.DIAMOND), Formatting.AQUA);

    }};

    public static HashMap<Block, Integer> levelRequiredToDetect = new HashMap<Block, Integer>() {{
        put(Blocks.COAL_ORE, 10);
        put(Blocks.REDSTONE_ORE, 13);
        put(Blocks.IRON_ORE, 16);
        put(Blocks.LAPIS_ORE, 20);
        put(Blocks.GOLD_ORE, 25);
        put(Blocks.DIAMOND_ORE, 30);
        put(Blocks.EMERALD_ORE, 33);
    }};

    public static HashMap<Item, Integer> alchemicalReagentValues = new HashMap<Item, Integer>() {{
        put(Items.GUNPOWDER, 1);
        put(Items.NETHER_WART, 1);
        put(Items.REDSTONE, 2);
        put(Items.GLOWSTONE, 3);

        put(Items.SUGAR, 1);
        put(Items.SPIDER_EYE, 1);
        put(Items.FERMENTED_SPIDER_EYE, 2);
        put(Items.RABBIT_FOOT, 2);
        put(Items.BLAZE_POWDER, 3);
        put(Items.GLISTERING_MELON_SLICE, 2);
        put(Items.GHAST_TEAR, 5);
        put(Items.MAGMA_CREAM, 5);
        put(Items.PUFFERFISH, 3);
        put(Items.GOLDEN_CARROT, 2);
        put(Items.TURTLE_HELMET, 5);
        put(Items.PHANTOM_MEMBRANE, 4);

    }};

    private static File CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDirectory();

    public static MestiereConfig init() {
        MestiereConfig instance = new MestiereConfig();

        try {
            InputStream fi = new FileInputStream(new File(CONFIG_DIRECTORY + File.separator + Mestiere.MOD_ID + ".json"));
            JsonParser jsonParser = new JsonParser();

            JsonObject json = (JsonObject)jsonParser.parse(new InputStreamReader(fi));

            instance.levelModifier = json.get("levelModifier").getAsFloat();
            instance.hardcoreProgression = json.get("hardcoreProgression").getAsBoolean();

            JsonObject miningValuesJson = json.getAsJsonObject("mining_values");

            if (miningValuesJson != null) {
                for (Map.Entry<String, JsonElement> element : miningValuesJson.entrySet()) {
                    Identifier blockId = new Identifier(element.getKey());
                    if (Registry.BLOCK.getOrEmpty(blockId).isPresent()) {
                        miningValues.put(Registry.BLOCK.get(blockId), element.getValue().getAsInt());
                    }
                }
            }

            JsonObject blockFormattingJson = json.getAsJsonObject("block_formatting");

            if (blockFormattingJson != null) {
                for (Map.Entry<String, JsonElement> element : blockFormattingJson.entrySet()) {
                    Identifier blockId = new Identifier(element.getKey());
                    if (Registry.BLOCK.getOrEmpty(blockId).isPresent()) {
                        String value = element.getValue().getAsString();
                        messageFormatting.put(blockId, Formatting.byName(value) == null ? Formatting.WHITE : Formatting.byName(value));
                    }
                }
            }

            fi.close();
        } catch (FileNotFoundException e) {
            Mestiere.log("No user config found; creating default user config");
            instance.writeConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Block, Integer> e : levelRequiredToDetect.entrySet()) {
            FibLib.Blocks.register(DimensionType.OVERWORLD, e.getKey(), (state, player) ->
                    Mestiere.COMPONENT.get(player).getLevel(Skills.MINING) >= e.getValue() || !instance.hardcoreProgression || player.isCreative() ?
                            state :
                            Blocks.STONE.getDefaultState()
            );

            Mestiere.PERKS.register(new MiningPerk(e.getValue(), e.getKey()));
        }

        return instance;
    }

    public void writeConfig() {
        File configFile = new File("config" + File.separator + Mestiere.MOD_ID + ".json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            JsonObject config = new JsonObject();
            config.addProperty("levelModifier", levelModifier);
            config.addProperty("hardcoreProgression", hardcoreProgression);

            JsonObject miningValuesJson = new JsonObject();
            for (Map.Entry<Block, Integer> entry : miningValues.entrySet()) {
                miningValuesJson.addProperty(Registry.BLOCK.getId(entry.getKey()).toString(), entry.getValue());
            }

            config.add("mining_values", miningValuesJson);

            JsonObject blockFormattingJson = new JsonObject();
            for (Map.Entry<Identifier, Formatting> entry : messageFormatting.entrySet()) {
                blockFormattingJson.addProperty(entry.getKey().toString(), entry.getValue().name());
            }

            config.add("block_formatting", blockFormattingJson);

            writer.write(gson.toJson(config));
            writer.close();
        } catch (IOException e) {
            Mestiere.log("Failed to save config to file");
        }
    }

    @Environment(EnvType.CLIENT)
    public static ConfigBuilder getConfigScreen() {
        ConfigBuilder builder = ConfigBuilder.create().setTitle("Mestiere");
        builder.setParentScreen(MinecraftClient.getInstance().currentScreen);
        builder.setDefaultBackgroundTexture(new Identifier("minecraft:textures/block/oak_planks.png"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory generalCategory = builder.getOrCreateCategory("category.mestiere.general");
        generalCategory.addEntry(
            entryBuilder.startFloatField("mestiere.levelModifier", Mestiere.CONFIG.levelModifier)
            .setSaveConsumer((modifier) -> Mestiere.CONFIG.levelModifier = modifier)
            .build()
        );

        generalCategory.addEntry(
            entryBuilder.startBooleanToggle("mestiere.hardcoreProgression", Mestiere.CONFIG.hardcoreProgression)
            .setSaveConsumer((bool) -> {
                Mestiere.CONFIG.hardcoreProgression = bool;
                if (MinecraftClient.getInstance().getServer() != null)
                    FibLib.Blocks.update(MinecraftClient.getInstance().getServer().getWorld(DimensionType.OVERWORLD),
                        MestiereConfig.levelRequiredToDetect.keySet()
                    );
            })
            .build()
        );

        ConfigCategory miningValuesCategory = builder.getOrCreateCategory("category.mestiere.miningLevels");
        for (Map.Entry<Block, Integer> entry: MestiereConfig.miningValues.entrySet()) {
            miningValuesCategory.addEntry(
                entryBuilder.startIntField("block.minecraft." + Registry.BLOCK.getId(entry.getKey()).getPath(), entry.getValue())
                .setSaveConsumer((value) -> MestiereConfig.miningValues.put(entry.getKey(), value))
                .build()
            );
        }

        ConfigCategory oreColorsCategory = builder.getOrCreateCategory("category.mestiere.oreColors");
        for (Map.Entry<Identifier, Formatting> entry: MestiereConfig.messageFormatting.entrySet()) {
            oreColorsCategory.addEntry(
                entryBuilder.startStringDropdownMenu("block.minecraft." + entry.getKey().getPath(), entry.getValue().name().toLowerCase())
                    .setSelections(Formatting.getNames(true, false))
                    .setSaveConsumer((value) -> MestiereConfig.messageFormatting.put(entry.getKey(), Formatting.byName(value) == null ? Formatting.WHITE : Formatting.byName(value)))
                    .build()
            );
        }


        builder.setSavingRunnable(Mestiere.CONFIG::writeConfig);

        return builder;
    }

    public int getMiningValue(Block block) {
        return miningValues.getOrDefault(block, 0);
    }
}
