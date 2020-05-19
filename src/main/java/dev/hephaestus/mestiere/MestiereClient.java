package dev.hephaestus.mestiere;

import dev.hephaestus.mestiere.client.gui.screens.SkillScreen;
import dev.hephaestus.mestiere.crafting.SkillCrafter;
import dev.hephaestus.mestiere.util.MestiereComponent;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.network.ClientPlayerEntity;

public class MestiereClient implements ClientModInitializer {

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        KeyBindingRegistry.INSTANCE.addCategory(Mestiere.MOD_NAME);

        SkillScreen.register();

        EntityComponentCallback.event(ClientPlayerEntity.class).register((player, components) ->
            components.put(Mestiere.COMPONENT, new MestiereComponent(player)));

        SkillCrafter.Builder.registerAllScreenProviders();
    }
}
