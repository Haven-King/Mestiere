package dev.hephaestus.mestiere.skills;

import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.fiblib.blocks.BlockFib;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.MestiereConfig;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import static net.minecraft.util.Util.createTranslationKey;

public class OreVisibilityPerk extends Skill.Perk {
    public OreVisibilityPerk(int level, Block block) {
        super(Skill.MINING, "ore." + Registry.BLOCK.getId(block).getPath(), level, block.asItem());
        this.isHardcore(false);

        MutableText blockText = new TranslatableText(block.getTranslationKey()).styled((style) -> style.withColor(MestiereConfig.messageFormatting.getOrDefault(Registry.BLOCK.getId(block), Formatting.WHITE)));

        setName(new TranslatableText(createTranslationKey("perk", Mestiere.newID("mining." + Registry.BLOCK.getId(block).getPath() + ".name")), blockText));
        setDescription(new TranslatableText(createTranslationKey("perk", Mestiere.newID("mining.ore.description")), blockText));
        setMessage(new TranslatableText("perk.mestiere.mining.ore.message", blockText));

        FibLib.Blocks.register(
                new BlockFib(block, Blocks.STONE) {
                    @Override
                    protected boolean condition(ServerPlayerEntity player) {
                        return Mestiere.COMPONENT.get(player).getLevel(Skill.MINING) < level && Mestiere.CONFIG.hardcoreProgression;
                    }
                }
        );
    }
}
