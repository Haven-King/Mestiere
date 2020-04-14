package dev.hephaestus.mestiere.client.gui.widgets;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.MestiereClient;
import dev.hephaestus.mestiere.skills.SkillPerk;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.data.Alignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class PerkPanel extends WPanel {
    private SkillPerk perk;

    public void init(Identifier skill) {
        this.perk = Mestiere.PERKS.get(skill);
    }

    @Override
    public void addInformation(List<String> information) {
        super.addInformation(information);
        information.add(this.perk.message.asFormattedString());
    }

    @Override
    public void paintBackground(int x, int y, int mouseX, int mouseY) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        assert player != null;
        boolean unlocked = MestiereClient.COMPONENT.get(player).getLevel(this.perk.skill) >= this.perk.level;

        ScreenDrawing.drawStringWithShadow(new LiteralText(this.perk.id.getPath()).formatted(unlocked ? Formatting.WHITE : Formatting.DARK_GRAY).asFormattedString(),
                Alignment.LEFT,
                x + this.height,
                y + ((20 - 8) / 2),
                this.width,
                0xFFFFFFFF);

        ScreenDrawing.drawStringWithShadow(new LiteralText(this.perk.level + "").formatted(unlocked ? Formatting.WHITE : Formatting.RED).asFormattedString(),
                Alignment.RIGHT,
                x,
                y + ((20 - 8) / 2),
                this.width - 4,
                0xFFFFFFFF);

        MinecraftClient.getInstance().getItemRenderer().renderGuiItem(perk.icon, x+2, y+2);
    }
}
