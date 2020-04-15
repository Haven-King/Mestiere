package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Perks {
    private final HashMap<Skill, ArrayList<SkillPerk>> perksBySkill = new HashMap<>();
    public final HashMap<Identifier, SkillPerk> perksById = new HashMap<>();

    public static Perks init() {
        Perks instance = new Perks();

        instance.register(new MaterialSmithingPerk(10, Items.GOLD_INGOT));
        instance.register(new MaterialSmithingPerk(20, Items.DIAMOND));

        instance.register(new SkillPerk(Skills.FARMING, "sex_guru",
                10, 1, false,
                new ItemStack(Items.WHEAT)));

        instance.register(new SkillPerk(Skills.FARMING, "gatherer",
                5, 15, true,
                new ItemStack(Items.GRASS)));

        instance.register(new SkillPerk(Skills.FARMING, "green_thumb",
                15, 1, false,
                new ItemStack(Items.WHEAT_SEEDS)));

        instance.register(new SkillPerk(Skills.HUNTING, "hunter",
                5, 30, true,
                new ItemStack(Items.PORKCHOP)));

        instance.register(new SkillPerk(Skills.HUNTING, "sharp_shooter",
                15,  30, true,
                new ItemStack(Items.ARROW)));

        instance.register(new SkillPerk(Skills.SLAYING, "slayer",
                15, 30, true,
                new ItemStack(Items.ROTTEN_FLESH)));

        instance.register(new SkillPerk(Skills.SLAYING, "sniper",
                15, 30, true,
                new ItemStack(Items.ARROW)));

        return instance;
    }

    public void register(SkillPerk perk) {
        perksBySkill.putIfAbsent(perk.skill, new ArrayList<>());
        perksBySkill.get(perk.skill).add(perk);
        perksById.put(perk.id, perk);
        Mestiere.debug("Registered new %s perk: %s %d", perk.skill.id, perk.id, perksBySkill.get(perk.skill).size());
    }

    public List<SkillPerk> get(Skill skill) {
        perksBySkill.putIfAbsent(skill, new ArrayList<>());
        return perksBySkill.get(skill);
    }

    public SkillPerk get(Identifier id) {
        return perksById.getOrDefault(id, SkillPerk.NONE);
    }

    public SkillPerk get(String id) {
        return get(Mestiere.newID(id));
    }
}
