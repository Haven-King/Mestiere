package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.HashMap;

import static net.minecraft.util.Util.createTranslationKey;

public class SkillPerk implements Comparable<SkillPerk> {
    public static final SkillPerk NONE = new SkillPerk(Skills.NONE, "none", Integer.MIN_VALUE, false, false, 1, null);

    public final Identifier id;
    public final Skill skill;
    public final int level;
    public final boolean hardcore;
    public final boolean scalesWithLevel;
    public final int maxLevel;
    public final ItemStack icon;

    public SkillPerk(Skill skill, String id, int level, boolean hardcore, boolean scalesWithLevel, int maxLevel, ItemStack icon) {
        this.scalesWithLevel = scalesWithLevel;
        this.maxLevel = maxLevel;
        this.id = Mestiere.newID(skill.id.getPath() + "." + id);
        this.skill = skill;
        this.level = level;
        this.hardcore = hardcore;
        this.icon = icon;
    }

    public SkillPerk(Skill skill, String id, int level, int maxLevel, boolean scalesWithLevel, ItemStack icon) {
        this(skill, id, level, false, scalesWithLevel, maxLevel, icon);
    }

    protected HashMap<Mestiere.KEY_TYPE, String> messages = new HashMap<>();

    public String getOrCreateTranslationKey(Mestiere.KEY_TYPE type) {
        if (messages.get(type) == null) {
            messages.put(type, createTranslationKey("perk", Mestiere.newID(this.id.getPath() + "." + type.toString().toLowerCase())));
        }

        return messages.get(type);
    }

    public TranslatableText getText(Mestiere.KEY_TYPE type) {
        return new TranslatableText(getOrCreateTranslationKey(type));
    }

    @Override
    public int compareTo(SkillPerk perk) {
        return Integer.compare(this.level, perk.level);
    }
}
