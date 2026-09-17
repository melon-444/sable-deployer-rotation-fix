package com.melon.sabledeployerfix.mixin;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DeployerBlockEntity.class)
public abstract class DeployerBlockEntityMixin {
    @ModifyArg(method = "activate", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerHandler;activate(Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/Vec3;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity$Mode;)V"), index = 0, remap = false, require = 1, allow = 1)
    private DeployerFakePlayer sableDeployerFix$worldFacing(final DeployerFakePlayer player) {
        final SubLevel subLevel = Sable.HELPER.getContaining((DeployerBlockEntity) (Object) this);
        if (subLevel == null)
            return player;

        // Create supplies a source-local facing; placement contexts expect a world-space look.
        final Direction direction = ((DeployerBlockEntity) (Object) this).getBlockState().getValue(DirectionalKineticBlock.FACING);
        final Vector3d look = new Vector3d(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        subLevel.logicalPose().transformNormal(look);
        final double horizontal = Math.hypot(look.x, look.z);
        final float pitch = (float) Math.toDegrees(Math.atan2(-look.y, horizontal));
        // A vertical look has no unique yaw. Keep Create's yaw instead of atan2 round-off.
        final float yaw = horizontal > 1.0E-7
                ? (float) Math.toDegrees(Math.atan2(-look.x, look.z)) : player.getYRot();
        player.setXRot(Mth.wrapDegrees(pitch));
        player.setYRot(Mth.wrapDegrees(yaw));
        player.setYHeadRot(player.getYRot());
        return player;
    }

}
