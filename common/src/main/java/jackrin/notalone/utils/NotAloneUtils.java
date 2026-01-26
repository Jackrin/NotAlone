package jackrin.notalone.utils;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import jackrin.notalone.entity.NotAloneEntity;
import jackrin.notalone.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NotAloneUtils {
    public static final Map<UUID, Double> playerFovMap = new ConcurrentHashMap<>();
    public static final Map<UUID, Double> playerAspectRatioMap = new ConcurrentHashMap<>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static final int SPAWN_CHANCE = 2000;
    private static final int FOOTSTEPS_CHANCE = 4000;
    private static final int WHITE_EYES_CHANCE = 2000;
    public static ServerPlayer markedPlayer = null;
    private static long markEndTime = 0L;
    private static final long MARK_DURATION_TICKS = 20L * 60 * 20;

    static Set<Block> ignoredBlocks = Set.of(
            Blocks.GLASS, Blocks.GLASS_PANE,
            Blocks.IRON_BARS, Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE,
            Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE,
            Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE,
            Blocks.ACACIA_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE,
            Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR,
            Blocks.ACACIA_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR,
            Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR,
            Blocks.DARK_OAK_DOOR,
            Blocks.LADDER, Blocks.VINE, Blocks.TALL_GRASS, Blocks.FERN, Blocks.FLOWER_POT,
            Blocks.WATER, Blocks.LAVA
    );

    static Set<Block> leavesBlocks = Set.of(
            Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
            Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES
    );

    private static boolean hasClearLineOfSight(Level level, Vec3 start, Vec3 end, Player player, double stepSize) {
        Vec3 current = start;
        double distance = start.distanceTo(end);
        Vec3 direction = end.subtract(start).normalize().scale(stepSize);
        Set<BlockPos> uniqueLeafBlocks = new HashSet<>();

        for (double traveled = 0; traveled < distance; traveled += stepSize) {
            BlockPos pos = BlockPos.containing(current);
            BlockState state = level.getBlockState(pos);

            if (leavesBlocks.contains(state.getBlock())) {
                uniqueLeafBlocks.add(pos);
            }

            if (!ignoredBlocks.contains(state.getBlock()) && (state.isCollisionShapeFullBlock(level, pos) || uniqueLeafBlocks.size() > 2)) {
                return false;
            }

            current = current.add(direction);
        }

        return true;
    }

    public static boolean isInTightViewCone(Player player, Entity entity, double coneAngle) {
        Vec3 playerEyes = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 playerLookVec = player.getViewVector(1.0F);

        AABB boundingBox = entity.getBoundingBox();

        List<Vec3> sampledPoints = List.of(
                new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                boundingBox.getCenter()
        );

        for (Vec3 point : sampledPoints) {
            Vec3 toTargetVec = point.subtract(playerEyes).normalize();
            double dotProduct = playerLookVec.dot(toTargetVec);
            double angle = Math.toDegrees(Math.acos(dotProduct));

            if (angle < (coneAngle / 2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInPlayerView(Player player, Entity entity, Level level, double stepSize) {
        Vec3 playerEyes = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 playerLookVec = player.getViewVector(1.0F);

        AABB boundingBox = entity.getBoundingBox();

        List<Vec3> sampledPoints = List.of(
                new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vec3(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vec3(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                boundingBox.getCenter());

        double fovSetting = playerFovMap.getOrDefault(player.getUUID(), 70.0);
        double aspectRatio = playerAspectRatioMap.getOrDefault(player.getUUID(), 16.0 / 9.0);

        double horizontalFov = 2 * Math.toDegrees(Math.atan(Math.tan(Math.toRadians(fovSetting / 2)) * aspectRatio));

        for (Vec3 point : sampledPoints) {
            Vec3 toTargetVec = point.subtract(playerEyes).normalize();
            double dotProduct = playerLookVec.dot(toTargetVec);
            double angle = Math.toDegrees(Math.acos(dotProduct));

            if (angle < (horizontalFov / 2) && hasClearLineOfSight(level, playerEyes, point, player, stepSize)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isInForest(ServerLevel level, BlockPos pos) {
        Holder<Biome> biomeEntry = level.getBiome(pos);
        return biomeEntry.is(BiomeTags.IS_FOREST);
    }

    private static BlockPos findValidSpawnPosition(ServerLevel level, ServerPlayer targetPlayer, List<ServerPlayer> allPlayers) {
        int MIN_DISTANCE = 48;
        int MIN_DISTANCE_FOREST = 24;
        int MAX_DISTANCE = 96;
        int MAX_DISTANCE_FOREST = 96;
        final int ATTEMPTS = 100;

        BlockPos targetPos = targetPlayer.blockPosition();
        boolean inForestBiome = isInForest(level, targetPos);

        for (int i = 0; i < ATTEMPTS; i++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            if (inForestBiome) {
                MIN_DISTANCE = MIN_DISTANCE_FOREST;
                MAX_DISTANCE = MAX_DISTANCE_FOREST;
            }
            double distance = MIN_DISTANCE + RANDOM.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            BlockPos spawnPos = targetPos.offset(
                    (int) (Math.cos(angle) * distance),
                    0,
                    (int) (Math.sin(angle) * distance));

            int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, spawnPos.getX(), spawnPos.getZ());
            spawnPos = new BlockPos(spawnPos.getX(), topY, spawnPos.getZ());

            if (!level.getBlockState(spawnPos).getFluidState().isEmpty()) continue;
            if (!level.getBlockState(spawnPos.above()).isAir()) continue;
            if (!level.getBlockState(spawnPos.below()).isCollisionShapeFullBlock(level, spawnPos.below())) continue;
            if (leavesBlocks.contains(level.getBlockState(spawnPos.below()).getBlock())) continue;

            // Check that the spawn position (center of block) has clear sight of the player
            Vec3 spawnCenter = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5);
            Vec3 playerEyes = targetPlayer.position().add(0, targetPlayer.getEyeHeight(), 0);
            if (!hasClearLineOfSight(level, spawnCenter, playerEyes, targetPlayer, 0.5)) {
                continue;
            }

            return spawnPos;
        }

        return null;
    }

    private static void spawnEntity(ServerLevel level, ServerPlayer markedPlayer) {
        List<ServerPlayer> players = level.players();

        if (players.isEmpty()) return;
        BlockPos spawnPos = findValidSpawnPosition(level, markedPlayer, players);

        if (spawnPos != null) {
            NotAloneEntity entity = new NotAloneEntity(ModEntities.ENTITY, level);
            Vec3 vec = new Vec3(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
            entity.moveOrInterpolateTo(vec, 0, 0);
            level.addFreshEntity(entity);
        }
    }

    private static boolean isEntityAlreadyPresent(ServerLevel level) {
        return !level.getEntities(ModEntities.ENTITY, entity -> true).isEmpty();
    }

    public static void trySpawnEntity() {
        if (markedPlayer == null) {
            return;
        }

        ServerLevel level = markedPlayer.level();

        if (isEntityAlreadyPresent(level)) {
            return;
        }

        if (RANDOM.nextInt(SPAWN_CHANCE) == 0) {
            spawnEntity(level, markedPlayer);
        }
    }

    public static void tryPlayFootsteps() {
        if (markedPlayer == null) {
            return;
        }

        if (RANDOM.nextInt(FOOTSTEPS_CHANCE) == 0) {
            FootstepEffects.tryPlayFootsteps(markedPlayer);
        }
    }

    public static void tryWhiteEyesAnimal(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (markedPlayer == null || (overworld.getGameTime() >= WhiteEyesAnimal.endTime && WhiteEyesAnimal.animal_uuid != null)) {
            if (WhiteEyesAnimal.animal_uuid != null) {
                WhiteEyesAnimal.animal_uuid = null;
                WhiteEyesAnimal.stareGoalSet = false;
            }
            return;
        }
        if (RANDOM.nextInt(WHITE_EYES_CHANCE) == 0) {
            if (WhiteEyesAnimal.animal_uuid != null) return;
            WhiteEyesAnimal.triggerWhiteEyedAnimalEffect(markedPlayer);
        }
    }

    private static void chooseNewMarkedPlayer(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        if (players.isEmpty()) {
            markedPlayer = null;
            return;
        }

        ServerLevel overworld = server.overworld();
        markedPlayer = players.get(overworld.getRandom().nextInt(players.size()));
        markEndTime = overworld.getGameTime() + MARK_DURATION_TICKS;
    }

    public static void checkMarkExpiration(MinecraftServer server) {
        ServerLevel overworld = server.overworld();

        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        if (markedPlayer == null) {
            WhiteEyesAnimal.animal_uuid = null;
            WhiteEyesAnimal.stareGoalSet = false;
            chooseNewMarkedPlayer(server);
            return;
        }

        if (overworld.getGameTime() >= markEndTime) {
            markedPlayer = null;
            WhiteEyesAnimal.animal_uuid = null;
            WhiteEyesAnimal.stareGoalSet = false;
            chooseNewMarkedPlayer(server);
            return;
        }

        ServerPlayer currentPlayer = markedPlayer;
        boolean isStillInGame = players.stream().anyMatch(player -> player.getUUID().equals(currentPlayer.getUUID()));

        if (!isStillInGame) {
            markedPlayer = null;
            chooseNewMarkedPlayer(server);
        }
    }

}
