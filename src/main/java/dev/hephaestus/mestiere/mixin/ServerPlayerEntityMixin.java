package dev.hephaestus.mestiere.mixin;

import com.mojang.authlib.GameProfile;
import dev.hephaestus.mestiere.Mestiere;
import dev.hephaestus.mestiere.util.SexedEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow public abstract void sendMessage(Text message, MessageType type);

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, GameProfile gameProfile) {
        super(world, blockPos, gameProfile);
    }

    private static Vec3d getLooking(ServerPlayerEntity player) {
        float f = -MathHelper.sin(player.yaw * 0.017453292F) * MathHelper.cos(player.pitch * 0.017453292F);
        float g = -MathHelper.sin(player.pitch * 0.017453292F);
        float h = MathHelper.cos(player.yaw * 0.017453292F) * MathHelper.cos(player.pitch * 0.017453292F);

        return new Vec3d(f,g,h);
    }

    private static EntityHitResult traceForEntity(ServerPlayerEntity player) {
        Vec3d vec3d2 = player.getRotationVec(1.0F);

        return ProjectileUtil.getEntityCollision(
            player.world,
            null,
            player.getCameraPosVec(1.0f),
            player.getCameraPosVec(1.0f).add(getLooking(player).multiply(5)),
            player.getCameraEntity().getBoundingBox().stretch(vec3d2.multiply(5)).expand(1.0D),
            (entity) -> !entity.isSpectator() && entity.isAlive() && entity.collides()
        );
    }

    @Override
    public void updatePositionAndAngles(double x, double y, double z, float yaw, float pitch) {
        super.updatePositionAndAngles(x, y, z, yaw, pitch);

        if (Mestiere.COMPONENT.get(this).hasPerk(Mestiere.newID("farming.sex_guru"))) {
            EntityHitResult hit = traceForEntity((ServerPlayerEntity) (Object) this);
            if (hit != null && hit.getEntity() instanceof AnimalEntity) {
                if (hit.getEntity().hasCustomName()) {
                    this.sendMessage(
                        new TranslatableText("perk.mestiere.farming.sex_guru.tip.named." + ((SexedEntity)hit.getEntity()).getSex().toString().toLowerCase(), hit.getEntity().getCustomName()),
                        MessageType.GAME_INFO
                    );
                } else {
                    this.sendMessage(
                        new TranslatableText("perk.mestiere.farming.sex_guru.tip.nameless." + ((SexedEntity)hit.getEntity()).getSex().toString().toLowerCase(), hit.getEntity().getName()),
                        MessageType.GAME_INFO
                    );
                }
            }
        }
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
