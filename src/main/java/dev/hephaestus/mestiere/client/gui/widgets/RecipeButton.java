package dev.hephaestus.mestiere.client.gui.widgets;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.crafting.SkillCraftingController;
import dev.hephaestus.mestiere.crafting.SkillRecipe;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;

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
    private IntList stacks1;
    private IntList stacks2;
    private ItemStack ingredient1;
    private ItemStack ingredient2;
    private SkillCraftingController controller;

    public void init(SkillRecipe recipe, SkillCraftingController controller) {
        this.recipe = recipe;

        this.stacks1 = recipe.getFirstIngredient().getIds();
        this.stacks2 = recipe.getSecondIngredient().getIds();

        this.ingredient1 = new ItemStack(
                Registry.ITEM.get(stacks1.getInt(0)),
                recipe.getFirstIngredientCount()
        );

        if (!recipe.getSecondIngredient().isEmpty()) {
            this.ingredient2 = new ItemStack(
                    Registry.ITEM.get(stacks2.getInt(0)),
                    recipe.getFirstIngredientCount()
            );
        }

        this.setEnabled(recipe.canCraft(controller.getPlayer()));

        this.controller = controller;
    }


    @Override
    @Environment(EnvType.CLIENT)
    public void paintBackground(int x, int y, int mouseX, int mouseY) {
        this.ingredient1 = recipe.getFirstItem(animCounter);

        if (!recipe.getSecondIngredient().isEmpty()) {
            this.ingredient2 = recipe.getSecondItem(animCounter);
        }

        ++animCounter;

        super.paintBackground(x, y, mouseX, mouseY);

        if (this.isEnabled())
            ARROW.paintBackground(x + 48, y + 6);
        else
            CROSSED_ARROW.paintBackground(x + 48, y + 6);

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        itemRenderer.renderGuiItem(this.ingredient1, x+2, y+2);
        itemRenderer.renderGuiItemOverlay(textRenderer, this.ingredient1, x+2, y+2);

        if (!recipe.getSecondIngredient().isEmpty()) {
            itemRenderer.renderGuiItem(this.ingredient2, x+20, y+2);
            itemRenderer.renderGuiItemOverlay(textRenderer, this.ingredient2, x+20, y+2);
        }

        itemRenderer.renderGuiItem(recipe.getOutput(), x+63, y+2);
        itemRenderer.renderGuiItemOverlay(textRenderer, recipe.getOutput(), x+63, y+2);
    }

    @Override
    public void tick() {
        super.tick();
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
