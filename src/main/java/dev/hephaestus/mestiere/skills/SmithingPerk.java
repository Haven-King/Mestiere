package dev.hephaestus.mestiere.skills;

import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.MestiereConfig;
import dev.hephaestus.mestiere.util.Skills;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

public class SmithingPerk extends SkillPerk {
    public SmithingPerk(int level, Item material) {
        super(Mestiere.newID("smithing." + Registry.ITEM.getId(material).getPath()), Skills.SMITHING, level, new LiteralText("You can now craft smithing recipes using ")
            .append(new TranslatableText(material.getTranslationKey()).setStyle(new Style().setColor(MestiereConfig.messageFormatting.getOrDefault(Registry.ITEM.getId(material), Formatting.WHITE)).setBold(true)))
            .append(new LiteralText("s").setStyle(new Style().setColor(MestiereConfig.messageFormatting.getOrDefault(Registry.ITEM.getId(material), Formatting.WHITE)).setBold(true)))
            .append(new LiteralText("!")),
            true);
    }
}
