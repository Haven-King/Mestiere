package dev.hephaestus.mestiere.skills;

import com.google.common.collect.ImmutableList;
import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static dev.hephaestus.mestiere.Mestiere.newID;

public class Skills implements Iterable<Skill> {
    public static Skill NONE;
    public static Skill ALCHEMY;
    public static Skill SMITHING;
    public static Skill LEATHERWORKING;
    public static Skill FARMING;
    public static Skill SLAYING;
    public static Skill HUNTING;
    public static Skill MINING;

    public static Skills init() {
        Skills instance = new Skills();

        NONE = instance.register(new Skill(Mestiere.newID("none"), Formatting.BLACK));
        ALCHEMY = instance.register(new Skill(newID("alchemy"), Formatting.LIGHT_PURPLE));
        SMITHING = instance.register(new Skill(newID("smithing"), Formatting.DARK_RED, SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH));
        LEATHERWORKING = instance.register(new Skill(newID("leatherworking"), Formatting.GOLD, SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER));
        FARMING = instance.register(new Skill(newID("farming"), Formatting.DARK_GREEN));
        SLAYING = instance.register(new Skill(newID("slaying"), Formatting.RED));
        HUNTING = instance.register(new Skill(newID("hunting"), Formatting.GREEN));
        MINING = instance.register(new Skill(newID("mining"), Formatting.GRAY));

        return instance;
    }

    private HashMap<Identifier, Skill> skills = new HashMap<>();

    public Skill register(Skill skill) {
        skills.put(skill.id, skill);
        return skill;
    }

    public Skill get(Identifier id) {
        return skills.getOrDefault(id, Skills.NONE);
    }

    @Override
    public Iterator<Skill> iterator() {
        return ImmutableList.copyOf(skills.values()).iterator();
    }

    @Override
    public void forEach(Consumer<? super Skill> action) {
        for (Skill s : skills.values()) {
            action.accept(s);
        }
    }

    @Override
    public Spliterator<Skill> spliterator() {
        return skills.values().spliterator();
    }
}