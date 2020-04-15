package dev.hephaestus.mestiere.client.gui.widgets;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.MestiereClient;
import dev.hephaestus.mestiere.client.gui.MestiereScreen;
import dev.hephaestus.mestiere.client.gui.ScrollingGuiDescription;
import dev.hephaestus.mestiere.skills.Skill;
import dev.hephaestus.mestiere.skills.SkillPerk;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.data.Alignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SkillButton extends WButton {
    private Skill skill;

    public SkillButton() {
        setOnClick(() -> {
            List<SkillPerk> data = Mestiere.PERKS.get(this.skill);
            data.sort(Comparator.comparing((p) -> p.level));
            MestiereScreen.open(new MestiereScreen(this.skill.getText(Mestiere.KEY_TYPE.NAME),
                new ScrollingGuiDescription<>(
                        data.stream().map((p) -> p.id).collect(Collectors.toList()),
                        PerkPanel::new,
                        (i, panel) -> panel.init(i)
                )));
            }
        );
    }

    public void init(Identifier skill) {
        this.skill = Mestiere.SKILLS.get(skill);
    }

    @Override
    public void paintBackground(int x, int y, int mouseX, int mouseY) {
        super.paintBackground(x, y, mouseX, mouseY);

        MinecraftClient.getInstance().getItemRenderer().renderGuiItem(skill.icon, x+2, y+2);

        ScreenDrawing.drawStringWithShadow(this.skill.getText(Mestiere.KEY_TYPE.NAME).asFormattedString(), Alignment.LEFT, x + this.height, y + ((20 - 8) / 2), this.width, 0xFFFFFFFF);

        assert MinecraftClient.getInstance().player != null;
        ScreenDrawing.drawStringWithShadow(MestiereClient.COMPONENT.get(MinecraftClient.getInstance().player).getLevel(skill) + "", Alignment.RIGHT, x, y + ((20 - 8) / 2), this.width - 4, 0xFFFFFFFF);
    }
}
