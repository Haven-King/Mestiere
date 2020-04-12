package dev.hephaestus.mestiere.mixin.hardcore;

import dev.hephaestus.fiblib.FibLib;
import dev.hephaestus.mestiere.Mestiere;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Shadow public ServerWorld getServerWorld() {return null;}

    @Inject(method = "setGameMode", at = @At("TAIL"))
    public void setGamemodeInjection(GameMode gameMode, CallbackInfo ci) {
        FibLib.Blocks.update(this.getServerWorld(), Mestiere.CONFIG.levelRequiredToDetect.keySet());
    }
}
