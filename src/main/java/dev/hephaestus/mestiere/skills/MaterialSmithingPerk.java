package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.MestiereConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import static net.minecraft.util.Util.createTranslationKey;

public class MaterialSmithingPerk extends Skill.Perk {
    public MaterialSmithingPerk(int level, Item material) {
        super(Skills.SMITHING, "material." + Registry.ITEM.getId(material).getPath(),
                level,
                true,
                false, 1, new ItemStack(material));

        MutableText materialText = new TranslatableText(material.getTranslationKey()).styled((style) -> style.withColor(MestiereConfig.messageFormatting.getOrDefault(Registry.ITEM.getId(material), Formatting.WHITE)).withBold(true));
        setName(new TranslatableText(createTranslationKey("perk", Mestiere.newID("smithing.material." + Registry.ITEM.getId(material).getPath() + ".name")), materialText));
        setDescription(new TranslatableText(createTranslationKey("perk", Mestiere.newID("smithing.material.description")), materialText));
    }
}
