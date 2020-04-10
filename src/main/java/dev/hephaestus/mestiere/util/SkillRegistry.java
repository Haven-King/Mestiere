package dev.hephaestus.mestiere.util;

import com.google.common.collect.ImmutableList;
import dev.hephaestus.mestiere.skills.Skill;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class SkillRegistry implements Iterable<Skill> {
    private HashMap<Identifier, Skill> skills = new HashMap<>();

    public Skill register(Skill skill) {
        skills.put(skill.id, skill);
        return skill;
    }

    public Skill get(Identifier id) {
        return skills.getOrDefault(id, Skill.NONE);
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
