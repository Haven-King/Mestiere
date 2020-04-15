package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.HashMap;

import static net.minecraft.util.Util.createTranslationKey;

public class SkillPerk implements Comparable<SkillPerk> {
    public static final SkillPerk NONE = new SkillPerk(Mestiere.newID("none"), Skills.NONE, Integer.MAX_VALUE, false, null);

    public final Identifier id;
    public final Skill skill;
    public final int level;
    public final boolean hardcore;
    public final ItemStack icon;

    public SkillPerk(Identifier id, Skill skill, int level, boolean hardcore, ItemStack icon) {
        this.id = id;
        this.skill = skill;
        this.level = level;
        this.hardcore = hardcore;
        this.icon = icon;
    }

    public SkillPerk(Identifier id, Skill skill, int level, Text message, Text description, ItemStack icon) {
        this(id, skill, level, false, icon);
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

    public void gained(ServerPlayerEntity player) {}
}
