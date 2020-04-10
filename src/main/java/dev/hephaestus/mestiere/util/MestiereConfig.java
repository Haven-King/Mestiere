package dev.hephaestus.mestiere.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.mestiere.Mestiere;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Config(name = "mestiere")
public class MestiereConfig implements ConfigData {
    float levelModifier = 1.0f;
    public boolean hardcoreProgression = false;

    public HashMap<Block, Integer> miningValues = new HashMap<>();
    public HashMap<Block, Formatting> blockFormatting = new HashMap<>();
    public HashMap<Block, Integer> levelRequireToDetect = new HashMap<>();

    public MestiereConfig() {
        miningValues.put(Blocks.STONE, 1);
        miningValues.put(Blocks.COAL_ORE, 4);
        miningValues.put(Blocks.REDSTONE_ORE, 8);
        miningValues.put(Blocks.IRON_ORE, 16);
        miningValues.put(Blocks.LAPIS_ORE, 32);
        miningValues.put(Blocks.GOLD_ORE, 64);
        miningValues.put(Blocks.DIAMOND_ORE, 256);
        miningValues.put(Blocks.EMERALD_ORE, 384);

        blockFormatting.put(Blocks.COAL_ORE, Formatting.BLACK);
        blockFormatting.put(Blocks.REDSTONE_ORE, Formatting.DARK_RED);
        blockFormatting.put(Blocks.IRON_ORE, Formatting.YELLOW);
        blockFormatting.put(Blocks.LAPIS_ORE, Formatting.DARK_BLUE);
        blockFormatting.put(Blocks.GOLD_ORE, Formatting.GOLD);
        blockFormatting.put(Blocks.DIAMOND_ORE, Formatting.AQUA);
        blockFormatting.put(Blocks.EMERALD_ORE, Formatting.GREEN);

        levelRequireToDetect.put(Blocks.COAL_ORE, 10);
        levelRequireToDetect.put(Blocks.REDSTONE_ORE, 15);
        levelRequireToDetect.put(Blocks.IRON_ORE, 15);
        levelRequireToDetect.put(Blocks.LAPIS_ORE, 20);
        levelRequireToDetect.put(Blocks.GOLD_ORE, 20);
        levelRequireToDetect.put(Blocks.DIAMOND_ORE, 30);
        levelRequireToDetect.put(Blocks.EMERALD_ORE, 30);

        try {
            InputStream fi = new FileInputStream(new File("config" + File.separator + Mestiere.MOD_ID + ".json"));
            JsonParser jsonParser = new JsonParser();

            JsonObject json = (JsonObject)jsonParser.parse(new InputStreamReader(fi));

            levelModifier = json.get("levelModifier").getAsFloat();
            hardcoreProgression = json.get("hardcoreProgression").getAsBoolean();

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
                        blockFormatting.put(Registry.BLOCK.get(blockId), Formatting.byName(value) == null ? Formatting.WHITE : Formatting.byName(value));
                    }
                }
            }

            fi.close();
        } catch (FileNotFoundException e) {
            Mestiere.log("No user config found; creating default user config");
            writeConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Map.Entry<Block, Integer> e : levelRequireToDetect.entrySet()) {
            FibLib.register(DimensionType.OVERWORLD, e.getKey(), (state, player) ->
                Mestiere.COMPONENT.get(player).getLevel(Mestiere.MINING) >= e.getValue() || player.isCreative() ?
                    state :
                    Blocks.STONE.getDefaultState()
            );
        }
    }

    public void writeConfig() {
        File configFile = new File("config" + File.separator + Mestiere.MOD_ID + ".json");
        Gson gson = new Gson();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
            JsonObject config = new JsonObject();
            config.addProperty("levelModifier", levelModifier);
            config.addProperty("hardcoreProgression", hardcoreProgression);
//            config.add("mining_values", gson.toJsonTree(miningValues));

            JsonObject miningValuesJson = new JsonObject();
            for (Map.Entry<Block, Integer> entry : miningValues.entrySet()) {
                miningValuesJson.addProperty(Registry.BLOCK.getId(entry.getKey()).toString(), entry.getValue());
            }

            config.add("mining_values", miningValuesJson);

            JsonObject blockFormattingJson = new JsonObject();
            for (Map.Entry<Block, Formatting> entry : blockFormatting.entrySet()) {
                blockFormattingJson.addProperty(Registry.BLOCK.getId(entry.getKey()).toString(), entry.getValue().name());
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
            .setSaveConsumer((bool) -> Mestiere.CONFIG.hardcoreProgression = bool)
            .build()
        );

        ConfigCategory miningValuesCategory = builder.getOrCreateCategory("category.mestiere.miningLevels");
        for (Map.Entry<Block, Integer> entry: Mestiere.CONFIG.miningValues.entrySet()) {
            miningValuesCategory.addEntry(
                entryBuilder.startIntField("block.minecraft." + Registry.BLOCK.getId(entry.getKey()).getPath(), entry.getValue())
                .setSaveConsumer((value) -> Mestiere.CONFIG.miningValues.put(entry.getKey(), value))
                .build()
            );
        }

        ConfigCategory oreColorsCategory = builder.getOrCreateCategory("category.mestiere.oreColors");
        for (Map.Entry<Block, Formatting> entry: Mestiere.CONFIG.blockFormatting.entrySet()) {
            oreColorsCategory.addEntry(
                entryBuilder.startStringDropdownMenu("block.minecraft." + Registry.BLOCK.getId(entry.getKey()).getPath(), entry.getValue().name().toLowerCase())
                    .setSelections(Formatting.getNames(true, false))
                    .setSaveConsumer((value) -> Mestiere.CONFIG.blockFormatting.put(entry.getKey(), Formatting.byName(value) == null ? Formatting.WHITE : Formatting.byName(value)))
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
