package com.melon.sabledeployerfix.mixin;

import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(value = SubLevelHelper.class, remap = false)
public abstract class SubLevelHelperMixin {

    /*
     * pushEntityLocal/popEntityLocal are paired calls. Recording the decision
     * made during push is necessary because a successful push changes the
     * entity position; recomputing the condition during pop would therefore
     * produce the wrong answer.
     */
    @Unique
    private static final ThreadLocal<Deque<Frame>>
            sableDeployerFix$frames =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
            method =
                    "pushEntityLocal(" +
                            "Ldev/ryanhcode/sable/sublevel/SubLevel;" +
                            "Lnet/minecraft/world/entity/Entity;" +
                            "Lnet/minecraft/commands/arguments/" +
                            "EntityAnchorArgument$Anchor;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private static void sableDeployerFix$pushEntityLocal(
            final SubLevel targetSubLevel,
            final Entity entity,
            final EntityAnchorArgument.Anchor anchor,
            final CallbackInfo ci
    ) {
        if (!(entity instanceof DeployerFakePlayer))
            return;

        final SubLevel sourceSubLevel =
                Sable.HELPER.getContaining(
                        entity.level(),
                        entity.position()
                );

        final boolean skip =
                sourceSubLevel != null
                        && sourceSubLevel == targetSubLevel;

        sableDeployerFix$frames.get().push(
                new Frame(
                        entity,
                        targetSubLevel,
                        anchor,
                        skip
                )
        );

        if (skip)
            ci.cancel();
    }

    @Inject(
            method =
                    "popEntityLocal(" +
                            "Ldev/ryanhcode/sable/sublevel/SubLevel;" +
                            "Lnet/minecraft/world/entity/Entity;" +
                            "Lnet/minecraft/commands/arguments/" +
                            "EntityAnchorArgument$Anchor;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private static void sableDeployerFix$popEntityLocal(
            final SubLevel targetSubLevel,
            final Entity entity,
            final EntityAnchorArgument.Anchor anchor,
            final CallbackInfo ci
    ) {
        if (!(entity instanceof DeployerFakePlayer))
            return;

        final Deque<Frame> frames =
                sableDeployerFix$frames.get();

        if (frames.isEmpty())
            return;

        final Frame frame = frames.peek();

        /*
         * Do not cancel an unmatched pop. Clear stale bookkeeping and let
         * Sable execute its original method instead.
         */
        if (frame.entity() != entity
                || frame.targetSubLevel() != targetSubLevel
                || frame.anchor() != anchor) {
            frames.clear();
            sableDeployerFix$frames.remove();
            return;
        }

        frames.pop();

        if (frames.isEmpty())
            sableDeployerFix$frames.remove();

        if (frame.skipped())
            ci.cancel();
    }

    @Unique
    private record Frame(
            Entity entity,
            SubLevel targetSubLevel,
            EntityAnchorArgument.Anchor anchor,
            boolean skipped
    ) {
    }
}