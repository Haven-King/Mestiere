package dev.hephaestus.mestiere.util;

import net.minecraft.network.Packet;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PacketFixer {
    static PacketFixer inject(Object object, ServerPlayerEntity player) {
        PacketFixer fixed = ((PacketFixer) object);
        fixed.fix(player);
        return fixed;
    }

    Packet<?> fix(ServerPlayerEntity player);
}
