package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.minecraft.util.Util.createTranslationKey;

public class Skill {
    public final Identifier id;
    public final Formatting format;
    public final SoundEvent sound;
    public final ItemStack icon;

    private final Text name;


    public Skill(Identifier id, Formatting format, SoundEvent sound, ItemStack icon) {
        this.id = id;
        this.format = format;
        this.sound = sound;
        this.icon = icon;

        this.name = new TranslatableText(createTranslationKey("skill", Mestiere.newID(this.id.getPath() + ".name"))).formatted(format);
    }

    public Skill(Identifier id, Formatting format, ItemStack icon) {
        this(id, format, null, icon);
    }

    public MutableText getName() {
        return this.name.copy();
    }

    public static class Perk implements Comparable<Perk> {
        public static final Perk NONE = new Perk(Skills.NONE, "none", Integer.MIN_VALUE, null);
        public static final Perk INVALID = new Perk(Skills.NONE, "invalid", Integer.MAX_VALUE, null);

        private static final SimpleRegistry<Perk> REGISTRY = new SimpleRegistry<>();
        private static final HashMap<Identifier, ArrayList<Perk>> SKILL_TO_PERK_MAP = new HashMap<>();

        public static Perk register(Perk perk) {
            Registry.register(REGISTRY, perk.id, perk);
            SKILL_TO_PERK_MAP.putIfAbsent(perk.skill.id, new ArrayList<>());
            SKILL_TO_PERK_MAP.get(perk.skill.id).add(perk);
            return perk;
        }

        public static Perk get(Identifier id) {
            return REGISTRY.get(id) == null ? INVALID : REGISTRY.get(id);
        }

        public static List<Perk> list(Skill skill) {
            SKILL_TO_PERK_MAP.putIfAbsent(skill.id, new ArrayList<>());
            return SKILL_TO_PERK_MAP.get(skill.id);
        }

        public final Identifier id;
        public final Skill skill;
        public final int level;
        public final ItemStack icon;
        private final Text message;

        private Text name;
        private Text description;

        private boolean hardcore = false;
        private boolean scalesWithLevel = false;
        private int maxLevel = -1;


        public Perk(Skill skill, String id, int level, Item icon) {
            this.skill = skill;
            this.id = Mestiere.newID(skill.id.getPath() + "." + id);
            this.level = level;
            this.icon = new ItemStack(icon, 1);

            this.name = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".name")));
            this.description = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".description")));
            this.message = new TranslatableText(createTranslationKey("perk", Mestiere.newID(this.id.getPath() + ".message")));
        }

        public Perk isHardcore(boolean bool) {
            this.hardcore = bool;
            return this;
        }

        public Perk scales(int maxLevel) {
            if (maxLevel > this.level) {
                this.scalesWithLevel = true;
                this.maxLevel = maxLevel;
            }
            return this;
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

        public boolean isHardcore() {
            return hardcore;
        }

        public boolean scales() {
            return scalesWithLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        @Override
        public int compareTo(Perk perk) {
            return Integer.compare(this.level, perk.level);
        }
    }
}
