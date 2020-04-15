package dev.hephaestus.mestiere;

import dev.hephaestus.mestiere.client.MestiereClientComponent;
import dev.hephaestus.mestiere.client.gui.MestiereScreen;
import dev.hephaestus.mestiere.client.gui.ScrollingGuiDescription;
import dev.hephaestus.mestiere.client.gui.widgets.SkillButton;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.hephaestus.mestiere.Mestiere.newID;

public class MestiereClient implements ClientModInitializer {
    public static FabricKeyBinding openSkillsMenu;

    public static final ComponentType<MestiereClientComponent> COMPONENT =
            ComponentRegistry.INSTANCE.registerIfAbsent(newID("component.client"), MestiereClientComponent.class);

    @Override
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
            components.put(COMPONENT, new MestiereClientComponent(player)));
    }
}
