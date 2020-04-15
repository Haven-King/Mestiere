package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.MestiereConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

import static net.minecraft.util.Util.createTranslationKey;

public class MaterialSmithingPerk extends SkillPerk {
    private final Text stext;
    private final Item material;

    public MaterialSmithingPerk(int level, Item material) {
        super(Skills.SMITHING, "material." + Registry.ITEM.getId(material).getPath(),
                level,
                true,
                false, 1, new ItemStack(material));

        this.material = material;
        stext = new TranslatableText(material.getTranslationKey()).setStyle(new Style().setColor(MestiereConfig.messageFormatting.getOrDefault(Registry.ITEM.getId(material), Formatting.WHITE)).setBold(true));
    }

    @Override
    public String getOrCreateTranslationKey(Mestiere.KEY_TYPE type) {
        if (messages.get(type) == null) {
            if (type == Mestiere.KEY_TYPE.NAME)
                messages.put(type, createTranslationKey("perk", Mestiere.newID("smithing.material." + Registry.ITEM.getId(material).getPath() +"." + type.toString().toLowerCase())));
            else
                messages.put(type, createTranslationKey("perk", Mestiere.newID("smithing.material." + type.toString().toLowerCase())));
        }

        return messages.get(type);
    }

    @Override
    public TranslatableText getText(Mestiere.KEY_TYPE type) {
        return new TranslatableText(getOrCreateTranslationKey(type), stext);
    }
}
