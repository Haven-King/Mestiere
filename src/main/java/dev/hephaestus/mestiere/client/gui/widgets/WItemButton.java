package dev.hephaestus.mestiere.client.gui.widgets;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class WItemButton extends WButton {
	private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");

	private final WItem item;
	private static final WPanel DUMMY = new WPlainPanel() {{setSize(10,10);}};
	public WItemButton(Item item) {
		this.item = new WItem(new ItemStack(item, 1));
		this.width = 20;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void paintBackground(int x, int y, int mouseX, int mouseY) {
//		if (isWithinBounds(mouseX, mouseY))
//			ScreenDrawing.texturedGuiRect(x, y, 0, 0, RECIPE_BUTTON_TEXTURE, 19, 0, 0);
//		else
//			ScreenDrawing.texturedGuiRect(x, y, 0, 0, RECIPE_BUTTON_TEXTURE, 0, 0, 0);

		BackgroundPainter.VANILLA.paintBackground(x+5, y+5, DUMMY);
		if (isWithinBounds(mouseX, mouseY)) {
			int color = (48 << 24) + 255;
			ScreenDrawing.coloredRect(x+1, y, 20, 1, color);
			ScreenDrawing.coloredRect(x, y+1, 22, 20, color);
			ScreenDrawing.coloredRect(x-1, y+21, 22, 1, color);
		}
		item.paintBackground(x+2, y+2, mouseX, mouseY);
	}
}
