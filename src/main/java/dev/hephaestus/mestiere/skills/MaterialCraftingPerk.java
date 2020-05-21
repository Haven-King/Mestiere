package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.MestiereConfig;
import net.minecraft.item.Item;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.util.Util.createTranslationKey;

public class MaterialCraftingPerk extends Skill.Perk {
    private static Map<Item, MaterialCraftingPerk> MAP = new HashMap<>();

    public static MaterialCraftingPerk get(Item item) {
        return MAP.get(item);
    }

    public MaterialCraftingPerk(Skill skill, Item material, int level) {
        super(skill, "material." + Registry.ITEM.getId(material).getPath(), level, material);
        this.isHardcore(true);

        MutableText materialText = new TranslatableText(material.getTranslationKey()).styled((style) -> style.withColor(MestiereConfig.messageFormatting.getOrDefault(Registry.ITEM.getId(material), Formatting.WHITE)).withBold(true));
        setName(new TranslatableText(createTranslationKey("perk", Mestiere.newID(skill.id.getPath() + ".material." + Registry.ITEM.getId(material).getPath() + ".name")), materialText));
        setDescription(new TranslatableText(createTranslationKey("perk", Mestiere.newID(skill.id.getPath() + ".material.description")), materialText));
        setMessage(new TranslatableText(createTranslationKey("perk", Mestiere.newID(skill.id.getPath() + ".material.message")), materialText));

        MAP.put(material, this);
    }
}