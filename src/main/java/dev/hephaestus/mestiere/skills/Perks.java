package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Perks {
    private HashMap<Skill, SortedSet<SkillPerk>> perksBySkill = new HashMap<>();
    private HashMap<Identifier, SkillPerk> perksById = new HashMap<>();

    public static Perks init() {
        Perks instance = new Perks();

        instance.register(new SmithingPerk(10, Items.GOLD_INGOT));
        instance.register(new SmithingPerk(20, Items.DIAMOND));
        instance.register(new SkillPerk(Mestiere.newID("sex_guru"), Skills.FARMING, 10, new LiteralText("You can now determine the sex of animals!")));
        instance.register(new SkillPerk(Mestiere.newID("green_thumb"), Skills.FARMING, 5, new LiteralText("You now know how to harvest crops without damaging them.")));

        return instance;
    }

    public void register(SkillPerk perk) {
        perksBySkill.putIfAbsent(perk.skill, new TreeSet<>());
        perksBySkill.get(perk.skill).add(perk);
        perksById.put(perk.id, perk);
        Mestiere.debug("Registered new %s skill: %s", perk.skill.name, perk.id);
    }

    public Collection<SkillPerk> get(Skill skill) {
        perksBySkill.putIfAbsent(skill, new TreeSet<>());
        return perksBySkill.get(skill);
    }

    public SkillPerk get(Identifier id) {
        return perksById.getOrDefault(id, SkillPerk.NONE);
    }
}
