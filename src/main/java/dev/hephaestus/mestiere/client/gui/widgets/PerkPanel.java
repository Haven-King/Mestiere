package dev.hephaestus.mestiere.client.gui.widgets;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.MestiereClient;
import dev.hephaestus.mestiere.skills.SkillPerk;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.data.Alignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

@Environment(EnvType.CLIENT)
public class PerkPanel extends WPanel {
    private SkillPerk perk;

    public void init(Identifier skill) {
        this.perk = Mestiere.PERKS.get(skill);
    }

    @Override
    public void addTooltip(List<Text> tooltips) {
        super.addTooltip(tooltips);
        tooltips.add(this.perk.getText(Mestiere.KEY_TYPE.NAME).formatted(this.perk.skill.format));
        tooltips.add(this.perk.getText(Mestiere.KEY_TYPE.DESCRIPTION));
        if (this.perk.scalesWithLevel) {
            tooltips.add(new TranslatableText("mestiere.scales"));
        }
    }

    @Override
    public void paintBackground(int x, int y, int mouseX, int mouseY) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        assert player != null;
        boolean unlocked = Mestiere.COMPONENT.get(player).getLevel(this.perk.skill) >= this.perk.level;

        if (unlocked) {
            ScreenDrawing.drawStringWithShadow(this.perk.getText(Mestiere.KEY_TYPE.NAME),
                Alignment.LEFT,
                x + this.height,
                y + ((20 - 8) / 2),
                this.width,
                0xFFFFFFFF);
        } else {
            ScreenDrawing.drawString(this.perk.getText(Mestiere.KEY_TYPE.NAME).formatted(Formatting.DARK_GRAY),
                    Alignment.LEFT,
                    x + this.height,
                    y + ((20 - 8) / 2),
                    this.width,
                    0xFFFFFFFF);
        }

        ScreenDrawing.drawStringWithShadow(new LiteralText(this.perk.level + "").formatted(unlocked ? Formatting.WHITE : Formatting.RED),
                Alignment.RIGHT,
                x,
                y + ((20 - 8) / 2),
                this.width - 4,
                0xFFFFFFFF);

        MinecraftClient.getInstance().getItemRenderer().renderGuiItem(perk.icon, x+2, y+2);
    }
}
