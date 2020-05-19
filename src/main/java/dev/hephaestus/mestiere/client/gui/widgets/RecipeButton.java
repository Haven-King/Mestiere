package dev.hephaestus.mestiere.client.gui.widgets;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.crafting.SkillCrafter;
import dev.hephaestus.mestiere.crafting.SkillRecipe;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class RecipeButton extends WButton {
    private static final WSprite ARROW = new WSprite(new Identifier("textures/gui/container/villager2.png"), 15f / 512f, 171f / 256f, 25f / 512f, 180f / 256f);
    private static final WSprite CROSSED_ARROW = new WSprite(new Identifier("textures/gui/container/villager2.png"), 25f / 512f, 171f / 256f, 35f / 512f, 180f / 256f);

    static {
        ARROW.setSize(10, 9);
        CROSSED_ARROW.setSize(10, 9);
    }


    private SkillRecipe recipe;

    public RecipeButton() {

    }

    private int animCounter = 0;
    private SkillCrafter controller;
    private int maxNumberOfInputs = 1;

    public void init(SkillRecipe recipe, SkillCrafter controller, int maxNumberOfInputs) {
        this.recipe = recipe;
        this.setEnabled(recipe.canCraft(controller.getPlayer()));

        this.controller = controller;
        this.maxNumberOfInputs = maxNumberOfInputs;
    }


    @Override
    @Environment(EnvType.CLIENT)
    public void paintBackground(int x, int y, int mouseX, int mouseY) {
        ++animCounter;

        super.paintBackground(x, y, mouseX, mouseY);

        if (this.isEnabled())
            ARROW.paintBackground(x + 9 + 18*(maxNumberOfInputs), y + 6);
        else
            CROSSED_ARROW.paintBackground(x + 9 + 18*(maxNumberOfInputs), y + 6);

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        for (int i = 0; i < recipe.numberOfInputs(); ++i) {
            ItemStack itemStack = recipe.getItem(i, animCounter);
            itemRenderer.renderGuiItem(itemStack, x+2 + 18*i, y+2);
            itemRenderer.renderGuiItemOverlay(textRenderer, itemStack, x+2 + 18*i, y+2);
        }

        itemRenderer.renderGuiItem(recipe.getOutput(), x+45 + 18*(maxNumberOfInputs-1), y+2);
        itemRenderer.renderGuiItemOverlay(textRenderer, recipe.getOutput(), x+45 + 18*(maxNumberOfInputs-1), y+2);
    }

    @Override
    public void onClick(int x, int y, int button) {
        if (this.isEnabled()) {
            controller.setRecipe(recipe);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeByte(controller.syncId);
            buf.writeIdentifier(recipe.getId());
            ClientSidePacketRegistry.INSTANCE.sendToServer(Mestiere.SELECT_RECIPE_ID, buf);
        }
    }
}
