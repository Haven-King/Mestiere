package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Perks {
    private final HashMap<Skill, ArrayList<Skill.Perk>> perksBySkill = new HashMap<>();
    public final HashMap<Identifier, Skill.Perk> perksById = new HashMap<>();

    public static Perks init() {
        Perks instance = new Perks();

        instance.register(new MaterialSmithingPerk(10, Items.GOLD_INGOT));
        instance.register(new MaterialSmithingPerk(20, Items.DIAMOND));

        instance.register(new Skill.Perk(Skills.FARMING, "sex_guru",
                10, 1, false,
                new ItemStack(Items.WHEAT)));

        instance.register(new Skill.Perk(Skills.FARMING, "gatherer",
                5, 15, true,
                new ItemStack(Items.GRASS)));

        instance.register(new Skill.Perk(Skills.FARMING, "green_thumb",
                15, 1, false,
                new ItemStack(Items.WHEAT_SEEDS)));

        instance.register(new Skill.Perk(Skills.HUNTING, "hunter",
                5, 30, true,
                new ItemStack(Items.PORKCHOP)));

        instance.register(new Skill.Perk(Skills.HUNTING, "sharp_shooter",
                15,  30, true,
                new ItemStack(Items.ARROW)));

        instance.register(new Skill.Perk(Skills.SLAYING, "slayer",
                15, 30, true,
                new ItemStack(Items.ROTTEN_FLESH)));

        instance.register(new Skill.Perk(Skills.SLAYING, "sniper",
                15, 30, true,
                new ItemStack(Items.ARROW)));

        return instance;
    }

    public void register(Skill.Perk perk) {
        perksBySkill.putIfAbsent(perk.skill, new ArrayList<>());
        perksBySkill.get(perk.skill).add(perk);
        perksById.put(perk.id, perk);
        Mestiere.debug("Registered new %s perk: %s", perk.skill.id, perk.id, perksBySkill.get(perk.skill).size());
    }

    public List<Skill.Perk> get(Skill skill) {
        perksBySkill.putIfAbsent(skill, new ArrayList<>());
        return perksBySkill.get(skill);
    }

    public Skill.Perk get(Identifier id) {
        return perksById.getOrDefault(id, Skill.Perk.NONE);
    }

    public Skill.Perk get(String id) {
        return get(Mestiere.newID(id));
    }
}
