package dev.hephaestus.mestiere.crafting;

import net.minecraft.util.registry.DefaultedRegistry;

public class SkillCrafterRegistry extends DefaultedRegistry<SkillCrafter.Builder> {
    public SkillCrafterRegistry(String defaultId) {
        super(defaultId);
    }
}
