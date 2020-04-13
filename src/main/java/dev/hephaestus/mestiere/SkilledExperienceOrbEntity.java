package dev.hephaestus.mestiere;

import dev.hephaestus.mestiere.skills.Skill;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.world.World;

public class SkilledExperienceOrbEntity extends ExperienceOrbEntity {
    public final Skill skill;
    public SkilledExperienceOrbEntity(World world, double x, double y, double z, int amount, Skill skill) {
        super(world, x, y, z, amount);
        this.skill = skill;
    }
    
    public SkilledExperienceOrbEntity(EntityType<? extends ExperienceOrbEntity> entityType, World world, Skill skill) {
        super(entityType, world);
        this.skill = skill;
    }


}
