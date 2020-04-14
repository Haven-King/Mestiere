package dev.hephaestus.mestiere.client;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.MestiereClient;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.util.XpComponent;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;

public class MestiereClientComponent implements XpComponent, EntitySyncedComponent {
    private final ClientPlayerEntity player;

    private HashMap<Skill, Integer> skills = new HashMap<>();

    public MestiereClientComponent(ClientPlayerEntity player) {
        this.player = player;
    }

    @Override
    public int getLevel(Skill skill) {
        return (int) MathHelper.sqrt((float)getXp(skill)/1.375 + 1);
    }

    @Override
    public int getXp(Skill skill) {
        return this.skills.getOrDefault(skill, 0);
    }

    @Override
    public void setXp(Skill skill, int xp) {

    }

    @Override
    public void addXp(Skill skill, int xp) {

    }

    @Override
    public void fromTag(CompoundTag compoundTag) {
        CompoundTag skillTag = compoundTag.getCompound(Mestiere.MOD_ID);

        for (Skill s : Mestiere.SKILLS) {
            this.skills.put(s, skillTag.getInt(s.id.toString()));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag compoundTag) {
        return null;
    }

    @Override
    public Entity getEntity() {
        return this.player;
    }

    @Override
    public ComponentType<?> getComponentType() {
        return MestiereClient.COMPONENT;
    }
}
