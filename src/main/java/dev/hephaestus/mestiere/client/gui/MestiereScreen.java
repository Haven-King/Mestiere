package dev.hephaestus.mestiere.client.gui;

import io.github.cottonmc.cotton.gui.GuiDescription;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.data.Alignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import java.util.Stack;

public class MestiereScreen extends CottonClientScreen {
    private static boolean isOpen = false;
    private static Stack<MestiereScreen> stack = new Stack<>();

    public static boolean isOpen() {
        return isOpen;
    }

    public MestiereScreen(String title, GuiDescription description) {
        super(new LiteralText(title), description);
        isOpen = true;
    }

    public static void open(MestiereScreen screen) {
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
        ((ScrollingGuiDescription)description).scroll((int)mouseX, (int)mouseY, amount);
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
        super.renderBackground();

        if (description!=null) {
            WPanel root = description.getRootPanel();
            if (root!=null) {
                root.paintBackground(left, top, mouseX-left, mouseY-top);
            }
        }

        if (getTitle() != null) {
            ScreenDrawing.drawString(getTitle().asFormattedString(), Alignment.CENTER, left, top, containerWidth, description.getTitleColor());
        }
    }
}
