package com.melon.sabledeployerfix.mixin;

import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(UseOnContext.class)
public abstract class UseOnContextMixin {

    @Shadow
    @Final
    private Level level;

    @Shadow
    @Final
    @Nullable
    private Player player;

    @Shadow
    public abstract BlockPos getClickedPos();

    @Inject(
            method = "getClickedFace",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void sableDeployerFix$getClickedFace(
            final CallbackInfoReturnable<Direction> cir
    ) {
        if (!(this.player instanceof DeployerFakePlayer))
            return;

        final SubLevel targetSubLevel =
                Sable.HELPER.getContaining(
                        this.level,
                        this.getClickedPos()
                );

        if (targetSubLevel == null)
            return;

        /*
         * DeployerHandler places the FakePlayer at its source-local ray
         * origin. This identifies the SubLevel whose coordinate system the
         * FakePlayer currently uses.
         */
        final SubLevel sourceSubLevel =
                Sable.HELPER.getContaining(
                        this.player.level(),
                        this.player.position()
                );

        /*
         * This small compatibility fix only substitutes the hit face when
         * source and target use the same local coordinate system.
         */
        if (sourceSubLevel == null
                || sourceSubLevel != targetSubLevel) {
            return;
        }

        final Vec3 localLook =
                this.player.getLookAngle();

        final Direction localClickedFace =
                Direction.getNearest(
                        localLook.x,
                        localLook.y,
                        localLook.z
                ).getOpposite();

        cir.setReturnValue(localClickedFace);
    }
}