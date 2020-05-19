package dev.hephaestus.mestiere;

import dev.hephaestus.mestiere.client.gui.MestiereScreen;
import dev.hephaestus.mestiere.client.gui.ScrollingGuiDescription;
import dev.hephaestus.mestiere.client.gui.SkillCraftingScreen;
import dev.hephaestus.mestiere.client.gui.widgets.SkillButton;
import dev.hephaestus.mestiere.crafting.SkillCraftingController;
import dev.hephaestus.mestiere.skills.Skills;
import dev.hephaestus.mestiere.util.MestiereComponent;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.client.screen.ScreenProviderRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.hephaestus.mestiere.Mestiere.newID;

public class MestiereClient implements ClientModInitializer {
    public static FabricKeyBinding openSkillsMenu;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        KeyBindingRegistry.INSTANCE.addCategory(Mestiere.MOD_NAME);

        openSkillsMenu = FabricKeyBinding.Builder.create(
            newID("open_skills_menu"),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            Mestiere.MOD_NAME
        ).build();

        KeyBindingRegistry.INSTANCE.register(openSkillsMenu);

        ClientTickCallback.EVENT.register(e -> {
            if (openSkillsMenu.isPressed() && MinecraftClient.getInstance().player != null && !MestiereScreen.isOpen()) {
                List<Identifier> data = new ArrayList<>(Mestiere.SKILLS.skills.keySet());
                data.sort(Comparator.comparing(Identifier::getPath));
                MestiereScreen.open(new MestiereScreen(new TranslatableText("mestiere.skills"), new ScrollingGuiDescription<>(
                    data,
                    SkillButton::new,
                    (i, button) -> button.init(i)
                )));
            }
        });

        EntityComponentCallback.event(ClientPlayerEntity.class).register((player, components) ->
            components.put(Mestiere.COMPONENT, new MestiereComponent(player)));

        ScreenProviderRegistry.INSTANCE.registerFactory(
                Registry.BLOCK.getId(Blocks.SMITHING_TABLE), (syncId, identifier, player, buf) ->
                        new SkillCraftingScreen(new SkillCraftingController(syncId, Skills.SMITHING,
                                new RecipeType[] {Mestiere.TYPES.netherite, Mestiere.TYPES.tools}, player.inventory, ScreenHandlerContext.create(player.world, buf.readBlockPos())))
        );
    }
}
