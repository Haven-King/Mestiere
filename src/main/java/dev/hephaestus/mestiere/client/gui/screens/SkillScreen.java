package dev.hephaestus.mestiere.client.gui.screens;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.client.gui.widgets.ScrollingGui;
import dev.hephaestus.mestiere.client.gui.widgets.ScrollingGuiDescription;
import dev.hephaestus.mestiere.skills.Skill;
import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.data.Alignment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import static dev.hephaestus.mestiere.Mestiere.newID;

@Environment(EnvType.CLIENT)
public class SkillScreen extends CottonClientScreen {
    public static FabricKeyBinding OPEN_SKILLS_MENU;

    private static boolean isOpen = false;
    private static final Stack<SkillScreen> stack = new Stack<>();

    public static boolean isOpen() {
        return isOpen;
    }

    public SkillScreen(MutableText title, GuiDescription description) {
        super(title.formatted(Formatting.DARK_GRAY), description);
        isOpen = true;
    }

    public static void register() {
        OPEN_SKILLS_MENU = FabricKeyBinding.Builder.create(newID("open_skills_menu"), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P, Mestiere.MOD_NAME).build();

        KeyBindingRegistry.INSTANCE.register(OPEN_SKILLS_MENU);
        ClientTickCallback.EVENT.register(SkillScreen::register);
    }

    public static void register(MinecraftClient minecraftClient) {
        if (OPEN_SKILLS_MENU.isPressed() && MinecraftClient.getInstance().player != null && !SkillScreen.isOpen()) {
            List<Identifier> data = new ArrayList<>(Mestiere.SKILLS.skills.keySet());
            data.sort(Comparator.comparing(Identifier::getPath));
            SkillScreen.open(new SkillScreen(new TranslatableText("mestiere.skills"), new ScrollingGuiDescription<>(
                    data,
                    SkillButton::new,
                    (i, button) -> button.init(i)
            )));
        }
    }

    public static void open(SkillScreen screen) {
        stack.push(screen);
        MinecraftClient.getInstance().openScreen(screen);
    }

    @Override
    public void onClose() {
        isOpen = false;
        super.onClose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        ((ScrollingGui)description).scroll((int)mouseX, (int)mouseY, amount);
        return true;
    }

    @Override
    public boolean keyPressed(int ch, int keyCode, int modifiers) {
        if (ch == GLFW.GLFW_KEY_ESCAPE && stack.size() > 1) {
            stack.pop();
            MinecraftClient.getInstance().openScreen(
                stack.peek()
            );
            return false;
        } else {
            return super.keyPressed(ch, keyCode, modifiers);
        }
    }

    @Override
    public void paint(int mouseX, int mouseY) {
        super.renderBackground(ScreenDrawing.getMatrices());

        if (description!=null) {
            WPanel root = description.getRootPanel();
            if (root!=null) {
                root.paintBackground(left, top, mouseX-left, mouseY-top);
            }
        }

        if (getTitle() != null) {
            ScreenDrawing.drawString(getTitle(), Alignment.CENTER, left, top, containerWidth, description.getTitleColor());
        }
    }

    @Environment(EnvType.CLIENT)
    public static class PerkPanel extends WPanel {
        private Skill.Perk perk;

        public void init(Identifier perk) {
            this.perk = Skill.Perk.get(perk);
        }

        @Override
        public void addTooltip(List<Text> tooltips) {
            super.addTooltip(tooltips);
            tooltips.add(this.perk.getName().formatted(this.perk.skill.format));
            tooltips.add(this.perk.getDescription());
            if (this.perk.scales()) {
                tooltips.add(new TranslatableText("mestiere.scales"));
            }
        }

        @Override
        public void paintBackground(int x, int y, int mouseX, int mouseY) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;

            assert player != null;
            boolean unlocked = Mestiere.COMPONENT.get(player).getLevel(this.perk.skill) >= this.perk.level;

            if (unlocked) {
                ScreenDrawing.drawStringWithShadow(this.perk.getName(),
                    Alignment.LEFT,
                    x + this.height + 4,
                    y + ((20 - 8) / 2),
                    this.width,
                    0xFFFFFFFF);
            } else {
                ScreenDrawing.drawString(this.perk.getName().formatted(Formatting.DARK_GRAY),
                        Alignment.LEFT,
                        x + this.height + 4,
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

    @Environment(EnvType.CLIENT)
    public static class SkillButton extends WButton {
        private Skill skill;

        public SkillButton() {
            setOnClick(() -> {
                List<Skill.Perk> data = Skill.Perk.list(this.skill);
                data.sort(Comparator.comparing((p) -> p.level));
                open(new SkillScreen(this.skill.getName(),
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

            ScreenDrawing.drawStringWithShadow(this.skill.getName().formatted(this.skill.format), Alignment.LEFT, x + this.height, y + ((20 - 8) / 2), this.width, 0xFFFFFFFF);

            assert MinecraftClient.getInstance().player != null;
            ScreenDrawing.drawStringWithShadow(Mestiere.COMPONENT.get(MinecraftClient.getInstance().player).getLevel(skill) + "", Alignment.RIGHT, x, y + ((20 - 8) / 2), this.width - 4, 0xFFFFFFFF);
        }
    }
}
