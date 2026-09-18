package jackrin.notalone.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FootstepEffects {
    public static void tryPlayFootsteps(ServerPlayer player) {
        ServerLevel level = player.level();

        float yaw = player.getYRot();
        double radians = Math.toRadians(yaw);
        Vec3 forward = new Vec3(-Math.sin(radians), 0, Math.cos(radians)).normalize();
        Vec3 right = new Vec3(-forward.z, 0, forward.x);

        Vec3 origin = player.position().subtract(forward.scale(8));
        BlockPos originBlock = BlockPos.containing(origin);

        int halfWidth = 3;
        int length = 4;
        int height = 4;

        for (int dy = -height; dy <= height; dy++) {
            for (int lw = -halfWidth; lw <= halfWidth; lw++) {
                for (int lf = -length; lf <= length; lf++) {
                    Vec3 offset = forward.scale(lf).add(right.scale(lw)).add(0, dy, 0);
                    BlockPos base = originBlock.offset(
                            (int)Math.round(offset.x),
                            (int)Math.round(offset.y),
                            (int)Math.round(offset.z)
                    );

                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        BlockPos p1 = base;
                        BlockPos p2 = base.relative(dir, 1);

                        if (isWalkable(level, p1) && isWalkable(level, p2)) {
                            int stepCount = 2 + level.getRandom().nextInt(2);
                            BlockPos[] path = new BlockPos[stepCount];
                            for (int i = 0; i < stepCount; i++) {
                                path[i] = base.relative(dir, i);
                            }

                            if (level.getRandom().nextBoolean()) {
                                for (int i = 0; i < stepCount / 2; i++) {
                                    BlockPos temp = path[i];
                                    path[i] = path[stepCount - 1 - i];
                                    path[stepCount - 1 - i] = temp;
                                }
                            }

                            simulateFootsteps(level, path);
                            return;
                        }
                    }
                }
            }
        }
    }

    private static boolean isWalkable(ServerLevel level, BlockPos pos) {
        BlockState floor = level.getBlockState(pos);
        BlockState above1 = level.getBlockState(pos.above());
        BlockState above2 = level.getBlockState(pos.above(2));

        boolean hasSpaceAbove = above1.getCollisionShape(level, pos.above()).isEmpty()
                && above2.getCollisionShape(level, pos.above(2)).isEmpty();

        return floor.isCollisionShapeFullBlock(level, pos) && hasSpaceAbove;
    }

    private static void simulateFootsteps(ServerLevel level, BlockPos[] path) {
        int stepDelay = 6 + level.getRandom().nextInt(2);
        for (int i = 0; i < path.length; i++) {
            BlockPos pos = path[i];
            int delay = i * stepDelay;
            TickTaskScheduler.schedule(() -> {
                BlockState state = level.getBlockState(pos);
                SoundType sound = state.getSoundType();
                Vec3 soundPos = Vec3.atCenterOf(pos);

                level.playSound(
                        null,
                        soundPos.x, soundPos.y, soundPos.z,
                        sound.getStepSound(),
                        SoundSource.PLAYERS,
                        sound.getVolume() * 0.3f,
                        sound.getPitch()
                );

            }, delay);
        }
    }
}
