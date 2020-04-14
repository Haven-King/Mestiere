package dev.hephaestus.mestiere.skills;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SkillPerk implements Comparable<SkillPerk> {
    public static final SkillPerk NONE = new SkillPerk(Mestiere.newID("none"), Skills.NONE, 0, null, false, null);

    public final Identifier id;
    public final Skill skill;
    public final int level;
    public final Text message;
    public final boolean hardcore;
    public final ItemStack icon;

    public SkillPerk(Identifier id, Skill skill, int level, Text message, boolean hardcore, ItemStack icon) {
        this.id = id;
        this.skill = skill;
        this.level = level;
        this.message = message;
        this.hardcore = hardcore;
        this.icon = icon;
    }

    public SkillPerk(Identifier id, Skill skill, int level, Text message, ItemStack icon) {
        this(id, skill, level, message, false, icon);
    }

    @Override
    public int compareTo(SkillPerk perk) {
        return Integer.compare(this.level, perk.level);
    }

    public void gained(ServerPlayerEntity player) {}
}
