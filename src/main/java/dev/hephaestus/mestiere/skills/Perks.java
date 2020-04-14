package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Perks {
    private HashMap<Skill, ArrayList<SkillPerk>> perksBySkill = new HashMap<>();
    public HashMap<Identifier, SkillPerk> perksById = new HashMap<>();

    public static Perks init() {
        Perks instance = new Perks();

        instance.register(new SmithingPerk(10, Items.GOLD_INGOT));
        instance.register(new SmithingPerk(20, Items.DIAMOND));

        instance.register(new SkillPerk(Mestiere.newID("farming.sex_guru"),
                Skills.FARMING,
                10,
                new TranslatableText("mestiere.perk.farming.sex_guru.message"),
                new TranslatableText("mestiere.perk.farming.sex_guru.description"),
                new ItemStack(Items.WHEAT)));

        instance.register(new SkillPerk(Mestiere.newID("farming.green_thumb"),
                Skills.FARMING,
                5,
                new TranslatableText("mestiere.perk.farming.green_thumb.message"),
                new TranslatableText("mestiere.perk.farming.green_thumb.description"),
                new ItemStack(Items.WHEAT_SEEDS)));

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
}
