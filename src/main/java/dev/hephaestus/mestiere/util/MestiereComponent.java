package dev.hephaestus.mestiere.util;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.skills.Skill;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;

public class MestiereComponent implements XpComponent, EntitySyncedComponent {
    private final PlayerEntity player;

    private final HashMap<Skill, Integer> skills = new HashMap<>();

    public MestiereComponent(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        CompoundTag skillTag = tag.getCompound(Mestiere.MOD_ID);

        Skill.forEach(skill -> this.skills.put(skill, skillTag.getInt(skill.id.toString())));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        CompoundTag skillTag = new CompoundTag();

        Skill.forEach(skill ->
            skillTag.putInt(skill.id.toString(), skills.getOrDefault(skill, 0))
        );

        tag.put(Mestiere.MOD_ID, skillTag);

        return tag;
    }

    @Override
    public int getLevel(Skill skill) {
        return (int) MathHelper.sqrt((float)getXp(skill)/1.375 + 1);
    }

    @Override
    public int getXp(Skill skill) {
        return this.skills.getOrDefault(skill, 0);
    }

    public int getXp(int level) {
        return (int) (1.375 * ((level * level) - 1));
    }

    @Override
    public void setXp(Skill skill, int xp) {
        this.skills.put(skill, xp);

        this.sync();
    }

    @Override
    public void addXp(Skill skill, int xp, boolean addEnchantmentXp) {
        if (player instanceof ServerPlayerEntity) {
            if (addEnchantmentXp)
                this.player.addExperience(xp);

            int oldLevel = getLevel(skill);
            this.skills.put(skill, this.skills.getOrDefault(skill, 0) + xp);
            int newLevel = getLevel(skill);
            if (newLevel > oldLevel) {
                MutableText sText = skill.getName();
                sText.styled((style) -> style.withBold(true));

                LiteralText lText = new LiteralText(Integer.toString(newLevel));
                lText.styled((style) -> style.withColor(Formatting.GREEN));

                ((ServerPlayerEntity)player).sendMessage(
                        new TranslatableText("mestiere.level_up", lText, sText),
                        MessageType.CHAT);

                for (Skill.Perk perk : Skill.Perk.list(skill)) {
                    if (perk.level > oldLevel && perk.level <= newLevel) {
                        ((ServerPlayerEntity)player).sendMessage(perk.getMessage(), MessageType.CHAT);
                    }
                }
            }

            this.sync();

            Mestiere.debug("%s has %dXP in %s. They are level %d", this.player.getName().asString(), this.skills.getOrDefault(skill, 0), skill.id, getLevel(skill));
        }
    }

    public boolean hasPerk(Skill.Perk perk) {
        return getLevel(perk.skill) >= perk.level;
    }

    public boolean hasPerk(Identifier perk) {
        return hasPerk(Skill.Perk.get(perk));
    }

    public float getScale(Skill.Perk perk) {
        return MathHelper.clamp((getLevel(perk.skill)-perk.level) / ((float)perk.getMaxLevel()-perk.level), 0, 1);
    }

    @Override
    public Entity getEntity() {
        return this.player;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return Mestiere.COMPONENT;
    }
}
