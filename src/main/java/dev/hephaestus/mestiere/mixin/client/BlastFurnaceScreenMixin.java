package dev.hephaestus.mestiere.mixin.client;

import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.client.gui.widgets.WItemButton;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.BlastFurnaceScreen;
import net.minecraft.client.gui.screen.recipebook.AbstractFurnaceRecipeBookScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlastFurnaceScreen.class)
public class BlastFurnaceScreenMixin extends AbstractFurnaceScreen<BlastFurnaceScreenHandler> {
	private WItemButton button;
	private BlockPos pos;

	public BlastFurnaceScreenMixin(BlastFurnaceScreenHandler handler, AbstractFurnaceRecipeBookScreen recipeBook, PlayerInventory inventory, Text title, Identifier background) {
		super(handler, recipeBook, inventory, title, background);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initButton(BlastFurnaceScreenHandler container, PlayerInventory inventory, Text title, CallbackInfo ci) {
		HitResult hit = inventory.player.rayTrace(10.0, 0.0f, false);
		if (hit instanceof BlockHitResult) {
			pos = ((BlockHitResult) hit).getBlockPos();
			button = new WItemButton(Items.ANVIL);
		} else {
			pos = null;
			button = null;
		}
	}

	@Override
	public void init() {
		super.init();

		if (button != null) {
			button.setLocation(x - 26, y + 8);

			button.setOnClick(() -> {
				if (pos != null) {
					PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
					buf.writeIdentifier(Registry.BLOCK.getId(Blocks.BLAST_FURNACE));
					buf.writeBlockPos(pos);
					ClientSidePacketRegistry.INSTANCE.sendToServer(Mestiere.OPEN_CRAFT_SCREEN, buf);
				}
			});
		}
	}

	@Override
	protected void drawBackground(MatrixStack matrixStack, float f, int mouseY, int i) {
		super.drawBackground(matrixStack, f, mouseY, i);

		if (button != null && !this.recipeBook.isOpen()) {
			button.setLocation(x - 26, y + 8);
			button.paintBackground(button.getX(), button.getY(), mouseY - button.getX(), i - button.getY());
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.button.onClick((int)mouseX - this.button.getX(), (int)mouseY - this.button.getY(), button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
}
