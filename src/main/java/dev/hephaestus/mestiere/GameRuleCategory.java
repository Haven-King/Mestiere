package dev.hephaestus.mestiere;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class GameRuleCategory implements Runnable {
    @Override
    public void run() {
        MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();
        String ruleCategory = remapper.mapClassName("intermediary", "net.minecraft.class_1928$class_5198");

        ClassTinkerers.enumBuilder(ruleCategory, String.class).addEnum("mestiere", "gamerule.category.mestiere").build();
    }
}
