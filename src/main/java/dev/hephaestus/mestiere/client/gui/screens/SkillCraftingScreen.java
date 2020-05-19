package dev.hephaestus.mestiere.client.gui.screens;

import dev.hephaestus.mestiere.client.gui.widgets.ScrollingGui;
import dev.hephaestus.mestiere.crafting.SkillCrafter;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkillCraftingScreen extends CottonInventoryScreen<SkillCrafter> {
    public SkillCraftingScreen(SkillCrafter container) {
        super(container, container.getPlayer());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        ((ScrollingGui)description).scroll((int)mouseX, (int)mouseY, amount);
        return true;
    }
}