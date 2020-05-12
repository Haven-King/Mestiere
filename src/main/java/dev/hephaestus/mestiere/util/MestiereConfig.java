package dev.hephaestus.mestiere.util;

import com.google.gson.*;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.OreVisibilityPerk;
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

    private static final File CONFIG_DIRECTORY = FabricLoader.getInstance().getConfigDirectory();

    public static MestiereConfig init() {
        MestiereConfig instance = new MestiereConfig();

        try {
            InputStream fi = new FileInputStream(new File(CONFIG_DIRECTORY + File.separator + Mestiere.MOD_ID + ".json"));
            JsonParser jsonParser = new JsonParser();

            JsonObject json = (JsonObject)jsonParser.parse(new InputStreamReader(fi));
            fi.close();

            instance.levelModifier = json.get("levelModifier").getAsFloat();
            instance.hardcoreProgression = json.get("hardcoreProgression").getAsBoolean();

            JsonObject miningValuesJson = json.getAsJsonObject("mining_values");

            if (miningValuesJson != null) {
                miningValues.clear();
                for (Map.Entry<String, JsonElement> element : miningValuesJson.entrySet()) {
                    Identifier blockId = new Identifier(element.getKey());
                    if (Registry.BLOCK.getOrEmpty(blockId).isPresent()) {
                        miningValues.put(Registry.BLOCK.get(blockId), element.getValue().getAsInt());
                    }
                }
            }

            JsonObject messageFormattingJson = json.getAsJsonObject("formatting");

            if (messageFormattingJson != null) {
                messageFormatting.clear();
                for (Map.Entry<String, JsonElement> element : messageFormattingJson.entrySet()) {
                    Identifier blockId = new Identifier(element.getKey());
                    if (Registry.BLOCK.getOrEmpty(blockId).isPresent()) {
                        String value = element.getValue().getAsString();
                        messageFormatting.put(blockId, Formatting.byName(value) == null ? Formatting.WHITE : Formatting.byName(value));
                    }
                }
            }

            JsonObject alchemicalReagentsJson = json.getAsJsonObject("alchemical_reagents");

            if (alchemicalReagentsJson != null) {
                alchemicalReagentValues.clear();
                for (Map.Entry<String, JsonElement> element : alchemicalReagentsJson.entrySet()) {
                    Identifier itemId = new Identifier(element.getKey());
                    if (Registry.ITEM.getOrEmpty(itemId).isPresent()) {
                        alchemicalReagentValues.put(Registry.ITEM.get(itemId), element.getValue().getAsInt());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Mestiere.log("No user config found; creating default user config");
            instance.writeConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Block, Integer> e : levelRequiredToDetect.entrySet()) {
            Mestiere.PERKS.register(new OreVisibilityPerk(e.getValue(), e.getKey()));
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

            JsonObject messageFormattingJson = new JsonObject();
            for (Map.Entry<Identifier, Formatting> entry : messageFormatting.entrySet()) {
                messageFormattingJson.addProperty(entry.getKey().toString(), entry.getValue().name());
            }

            config.add("block_formatting", messageFormattingJson);

            JsonObject alchemicalReagentsJson = new JsonObject();
            for (Map.Entry<Item, Integer> entry : alchemicalReagentValues.entrySet()) {
                alchemicalReagentsJson.addProperty(Registry.ITEM.getId(entry.getKey()).toString(), entry.getValue());
            }

            config.add("alchemical_reagents", alchemicalReagentsJson);


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

        ConfigCategory generalCategory = builder.getOrCreateCategory("mestiere.category.general");
        generalCategory.addEntry(
            entryBuilder.startFloatField("mestiere.levelModifier", Mestiere.CONFIG.levelModifier)
            .setSaveConsumer((modifier) -> Mestiere.CONFIG.levelModifier = modifier)
            .build()
        );

        ConfigCategory miningValuesCategory = builder.getOrCreateCategory("mestiere.category.miningLevels");
        for (Map.Entry<Block, Integer> entry: MestiereConfig.miningValues.entrySet()) {
            miningValuesCategory.addEntry(
                entryBuilder.startIntField(entry.getKey().getTranslationKey(), entry.getValue())
                .setSaveConsumer((value) -> MestiereConfig.miningValues.put(entry.getKey(), value))
                .build()
            );
        }

        ConfigCategory oreColorsCategory = builder.getOrCreateCategory("mestiere.category.colors");
        for (Map.Entry<Identifier, Formatting> entry: MestiereConfig.messageFormatting.entrySet()) {
            oreColorsCategory.addEntry(
                entryBuilder.startStringDropdownMenu(
                        Registry.ITEM.containsId(entry.getKey()) ? Registry.ITEM.get(entry.getKey()).getTranslationKey() : Registry.BLOCK.get(entry.getKey()).getTranslationKey(),
                        entry.getValue().name().toLowerCase()
                ).setSelections(Formatting.getNames(true, false))
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
