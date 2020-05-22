package dev.hephaestus.mestiere.client.gui.screens;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.client.gui.widgets.ScrollingGui;
import dev.hephaestus.mestiere.client.gui.widgets.WItemButton;
import dev.hephaestus.mestiere.crafting.SkillCrafter;
import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class SkillCraftingScreen extends CottonInventoryScreen<SkillCrafter> {
    public static int i;
    private WItemButton button;
    public SkillCraftingScreen(SkillCrafter container, Item altScreenItem) {
        super(container, container.getPlayer());
        i++;
        if (altScreenItem != null) {
            button = new WItemButton(altScreenItem);
            button.setOnClick(() -> {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeInt(container.getSyncId());
                ClientSidePacketRegistry.INSTANCE.sendToServer(Mestiere.OPEN_ALT_SCREEN, buf);
            });
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        ((ScrollingGui)description).scroll((int)mouseX, (int)mouseY, amount);
        return true;
    }

    @Override
    public void reposition() {
        super.reposition();

        if (button != null) button.setLocation(x - 34, y);
    }

    @Override
    public void init() {
        super.reposition();

        if (button != null) button.setLocation(x - 34, y);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        super.drawBackground(matrices, partialTicks, mouseX, mouseY);

        if (button != null) {
            button.paintBackground(button.getX(), button.getY(), mouseX - button.getX(), mouseY - button.getY());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (button != null)
            button.onClick((int)mouseX - button.getX(), (int)mouseY - button.getY(), mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}