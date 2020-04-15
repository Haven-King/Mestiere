package dev.hephaestus.mestiere.mixin;

import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    public void sendLang(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        Mestiere.debug("" + Mestiere.COMPONENT.get(player).isClientConnected());

        if (!Mestiere.COMPONENT.get(player).isClientConnected() && !(Objects.requireNonNull(player.getServer()).isOwner(player.getGameProfile())))
            player.sendResourcePackUrl("https://s3-us-west-2.amazonaws.com/hephaestus.dev/files/Mestiere-Lang-Files-0.0.2.zip", "");
    }
}
