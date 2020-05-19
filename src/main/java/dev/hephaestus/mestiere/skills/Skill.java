package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;

import static net.minecraft.util.Util.createTranslationKey;

public class Skill {
    public final Identifier id;
    public final Formatting format;
    public final SoundEvent sound;
    public final ItemStack icon;

    public Skill(Identifier id, Formatting format, SoundEvent sound, ItemStack icon) {
        this.id = id;
        this.format = format;
        this.sound = sound;
        this.icon = icon;
    }

    public Skill(Identifier id, Formatting format, ItemStack icon) {
        this(id, format, null, icon);
    }

    protected HashMap<Mestiere.KEY_TYPE, String> messages = new HashMap<>();

    public String getOrCreateTranslationKey(Mestiere.KEY_TYPE type) {
        if (messages.get(type) == null) {
            messages.put(type, createTranslationKey("skill", Mestiere.newID(this.id.getPath() + "." + type.toString().toLowerCase())));
        }

        return messages.get(type);
    }

    public TranslatableText getText(Mestiere.KEY_TYPE type) {
        return (TranslatableText) new TranslatableText(getOrCreateTranslationKey(type)).formatted(format);
    }

    public static class Perk implements Comparable<Perk> {
        public static final Perk NONE = new Perk(Skills.NONE, "none", Integer.MIN_VALUE, false, false, 1, null);

        public final Identifier id;
        public final Skill skill;
        public final int level;
        public final boolean hardcore;
        public final boolean scalesWithLevel;
        public final int maxLevel;
        public final ItemStack icon;

        private Text name;
        private Text description;
        private Text message;

        public Perk(Skill skill, String id, int level, boolean hardcore, boolean scalesWithLevel, int maxLevel, ItemStack icon) {
            this.scalesWithLevel = scalesWithLevel;
            this.maxLevel = maxLevel;
            this.id = Mestiere.newID(skill.id.getPath() + "." + id);
            this.skill = skill;
            this.level = level;
            this.hardcore = hardcore;
            this.icon = icon;

            this.name = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".name")));
            this.description = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".description")));
            this.message = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".message")));
        }

        public Perk(Skill skill, String id, int level, int maxLevel, boolean scalesWithLevel, ItemStack icon) {
            this(skill, id, level, false, scalesWithLevel, maxLevel, icon);
        }

        public void setName(Text text) {
            this.name = text;
        }

        public void setDescription(Text text) {
            this.description = text;
        }

        public MutableText getName() {
            return this.name.copy();
        }

        public MutableText getDescription() {
            return this.description.copy();
        }

        public MutableText getMessage() {
            return this.message.copy();
        }

        @Override
        public int compareTo(Perk perk) {
            return Integer.compare(this.level, perk.level);
        }
    }
}
