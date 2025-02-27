package jackrin.notalone.entity;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import jackrin.notalone.NotAlone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.registry.tag.BiomeTags;

import java.util.Optional;

public class NotAloneUtils {
    public static final Map<UUID, Double> playerFovMap = new ConcurrentHashMap<>();
    public static final Map<UUID, Double> playerAspectRatioMap = new ConcurrentHashMap<>();
    private static final Random RANDOM = new Random();
    private static final int SPAWN_CHANCE = 100;
    private static Optional<ServerPlayerEntity> markedPlayer = Optional.empty();
    private static long markEndTime = 0L;
    private static final long MARK_DURATION_TICKS = 20L * 60 * 20;

    private static boolean hasClearLineOfSight(World world, Vec3d start, Vec3d end, PlayerEntity player) {
        Set<Block> ignoredBlocks = Set.of(
                Blocks.GLASS, Blocks.GLASS_PANE,
                Blocks.IRON_BARS, Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE,
                Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE,
                Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE,
                Blocks.ACACIA_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE,
                Blocks.OAK_TRAPDOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.BIRCH_TRAPDOOR, Blocks.JUNGLE_TRAPDOOR,
                Blocks.ACACIA_TRAPDOOR, Blocks.DARK_OAK_TRAPDOOR,
                Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR,
                Blocks.DARK_OAK_DOOR,
                Blocks.LADDER, Blocks.VINE, Blocks.TALL_GRASS, Blocks.SHORT_GRASS, Blocks.FLOWER_POT,
                Blocks.WATER, Blocks.LAVA);

        Set<Block> leavesBlocks = Set.of(
                Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES,
                Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES);

        Vec3d current = start;
        double stepSize = 0.1;
        double distance = start.distanceTo(end);
        Vec3d direction = end.subtract(start).normalize().multiply(stepSize);
        Set<BlockPos> uniqueLeafBlocks = new HashSet<>();

        for (double traveled = 0; traveled < distance; traveled += stepSize) {
            BlockPos pos = BlockPos.ofFloored(current);
            BlockState state = world.getBlockState(pos);

            if (leavesBlocks.contains(state.getBlock())) {
                uniqueLeafBlocks.add(pos);
            }

            if (!ignoredBlocks.contains(state.getBlock()) && (state.isOpaque() || uniqueLeafBlocks.size() > 2)) {
                return false;
            }

            current = current.add(direction);
        }

        return true;
    }

    public static boolean isInTightViewCone(PlayerEntity player, Entity entity, double coneAngle) {
        Vec3d playerEyes = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d playerLookVec = player.getRotationVec(1.0F);

        Box boundingBox = entity.getBoundingBox();

        List<Vec3d> sampledPoints = List.of(
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                boundingBox.getCenter());

        for (Vec3d point : sampledPoints) {
            Vec3d toTargetVec = point.subtract(playerEyes).normalize();
            double dotProduct = playerLookVec.dotProduct(toTargetVec);
            double angle = Math.toDegrees(Math.acos(dotProduct));

            if (angle < (coneAngle / 2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInPlayerView(PlayerEntity player, Entity entity, World world) {
        Vec3d playerEyes = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d playerLookVec = player.getRotationVec(1.0F);

        Box boundingBox = entity.getBoundingBox();

        List<Vec3d> sampledPoints = List.of(
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.minZ),
                new Vec3d(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ),
                new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ),
                new Vec3d(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ),
                boundingBox.getCenter());

        double fovSetting = playerFovMap.getOrDefault(player.getUuid(), 70.0);
        double aspectRatio = playerAspectRatioMap.getOrDefault(player.getUuid(), 16.0 / 9.0);

        double horizontalFov = 2 * Math.toDegrees(Math.atan(Math.tan(Math.toRadians(fovSetting / 2)) * aspectRatio));

        for (Vec3d point : sampledPoints) {
            Vec3d toTargetVec = point.subtract(playerEyes).normalize();
            double dotProduct = playerLookVec.dotProduct(toTargetVec);
            double angle = Math.toDegrees(Math.acos(dotProduct));

            if (angle < (horizontalFov / 2) && hasClearLineOfSight(world, playerEyes, point, player)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBlockInPlayerView(PlayerEntity player, BlockPos pos, World world) {
        Vec3d playerEyes = player.getPos().add(0, player.getEyeHeight(player.getPose()), 0);
        Vec3d playerLookVec = player.getRotationVec(1.0F);
        Vec3d toTargetVec = new Vec3d(
                pos.getX() - playerEyes.x,
                pos.getY() - playerEyes.y,
                pos.getZ() - playerEyes.z).normalize();

        double dotProduct = playerLookVec.dotProduct(toTargetVec);
        double angle = Math.toDegrees(Math.acos(dotProduct));

        double fovSetting = playerFovMap.getOrDefault(player.getUuid(), 70.0);
        double aspectRatio = playerAspectRatioMap.getOrDefault(player.getUuid(), 16.0 / 9.0);

        double horizontalFov = 2 * Math.toDegrees(Math.atan(Math.tan(Math.toRadians(fovSetting / 2)) * aspectRatio));

        boolean hasLineOfSight = false;

        for (double yOffset = -1.0; yOffset <= 1.0; yOffset += 0.5) {
            Vec3d adjustedPos = new Vec3d(pos.getX(), pos.getY() + yOffset, pos.getZ());
            if (hasClearLineOfSight(world, playerEyes, adjustedPos, player)) {
                hasLineOfSight = true;
                break;
            }
        }

        return angle < (horizontalFov / 2) && hasLineOfSight;
    }

    private static boolean isFarEnoughFromAllPlayers(BlockPos pos, List<ServerPlayerEntity> players, int minDistance) {
        for (ServerPlayerEntity player : players) {
            if (pos.getSquaredDistance(player.getBlockPos()) < minDistance * minDistance) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInAnyPlayersView(ServerWorld world, BlockPos pos, List<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            if (player.getBlockPos().getSquaredDistance(pos) < 128 * 128) {
                if (isBlockInPlayerView(player, pos, world)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isInForest(ServerWorld world, BlockPos pos) {
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);

        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);

        RegistryKey<Biome> biomeKey = biomeEntry.getKey().orElse(null);
        if (biomeKey == null) {
            return false;
        }

        return biomeRegistry.entryOf(biomeKey).isIn(BiomeTags.IS_FOREST);
    }

    private static BlockPos findValidSpawnPosition(ServerWorld world, ServerPlayerEntity targetPlayer,
            List<ServerPlayerEntity> allPlayers) {
        int MIN_DISTANCE = 64;
        int MIN_DISTANCE_FOREST = 16;
        int MAX_DISTANCE = 128;
        int MAX_DISTANCE_FOREST = 32;
        final int ATTEMPTS = 100;

        BlockPos targetPos = targetPlayer.getBlockPos();

        boolean inForestBiome = isInForest(world, targetPos);

        for (int i = 0; i < ATTEMPTS; i++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            if (inForestBiome) {
                MIN_DISTANCE = MIN_DISTANCE_FOREST;
                MAX_DISTANCE = MAX_DISTANCE_FOREST;
            }
            double distance = MIN_DISTANCE + RANDOM.nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
            BlockPos playerPos = targetPlayer.getBlockPos();
            BlockPos spawnPos = targetPos.add(
                    (int) (Math.cos(angle) * distance),
                    0,
                    (int) (Math.sin(angle) * distance));

            int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, spawnPos.getX(), spawnPos.getZ());
            spawnPos = new BlockPos(spawnPos.getX(), topY, spawnPos.getZ());

            BlockState spawnBlockState = world.getBlockState(spawnPos);
            if (!spawnBlockState.getFluidState().isEmpty()) {
                continue;
            }

            BlockState blockAboveState = world.getBlockState(spawnPos.up());
            if (!blockAboveState.isAir()) {
                continue;
            }

            BlockState blockBelowState = world.getBlockState(spawnPos.down());
            if (!blockBelowState.isSolidBlock(world, spawnPos.down())) {
                continue;
            }

            Vec3d end = new Vec3d(playerPos.getX(),
                    playerPos.getY() + targetPlayer.getEyeHeight(targetPlayer.getPose()), playerPos.getZ());
            Vec3d start = new Vec3d(spawnPos.getX(),
                    spawnPos.getY() + targetPlayer.getEyeHeight(targetPlayer.getPose()), spawnPos.getZ());

            if (isFarEnoughFromAllPlayers(spawnPos, allPlayers, MIN_DISTANCE) &&
                    !isInAnyPlayersView(world, spawnPos, allPlayers) &&
                    (inForestBiome || hasClearLineOfSight(world, start, end, targetPlayer))) {
                return spawnPos;
            }
        }

        return null;
    }

    private static void spawnEntity(ServerWorld world, ServerPlayerEntity markedPlayer) {
        List<ServerPlayerEntity> players = world.getPlayers();

        if (players.isEmpty())
            return;
        BlockPos spawnPos = findValidSpawnPosition(world, markedPlayer, players);

        if (spawnPos != null) {
            NotAloneEntity entity = new NotAloneEntity(NotAlone.ENTITY, world);
            entity.refreshPositionAndAngles(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0, 0);
            world.spawnEntity(entity);
        }
    }

    private static boolean isEntityAlreadyPresent(ServerWorld world) {
        return !world.getEntitiesByType(NotAlone.ENTITY, entity -> true).isEmpty();
    }

    public static void trySpawnEntity(ServerWorld world) {
        if (!markedPlayer.isPresent()) {
            return;
        }

        if (isEntityAlreadyPresent(world)) {
            return;
        }

        if (RANDOM.nextInt(SPAWN_CHANCE) == 0) {
            spawnEntity(world, markedPlayer.get());
        }
    }

    private static void chooseNewMarkedPlayer(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        if (players.isEmpty()) {
            markedPlayer = Optional.empty();
            return;
        }

        ServerWorld overworld = server.getOverworld();
        if (overworld == null) {
            markedPlayer = Optional.empty();
            return;
        }

        ServerPlayerEntity newMarkedPlayer = players.get(overworld.getRandom().nextInt(players.size()));
        markedPlayer = Optional.of(newMarkedPlayer);
        markEndTime = overworld.getTime() + MARK_DURATION_TICKS;

        newMarkedPlayer.sendMessage(
                Text.literal("You feel a strange presence watching you...").formatted(Formatting.RED), false);
        System.out.println("New marked player: " + newMarkedPlayer.getName().getString());
    }

    public static void checkMarkExpiration(MinecraftServer server) {
        ServerWorld overworld = server.getOverworld();
        if (overworld == null)
            return;

        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

        if (markedPlayer.isEmpty()) {
            chooseNewMarkedPlayer(server);
            return;
        }

        if (overworld.getTime() >= markEndTime) {
            markedPlayer = Optional.empty();
            chooseNewMarkedPlayer(server);
            return;
        }

        ServerPlayerEntity currentPlayer = markedPlayer.get();
        boolean isStillInGame = players.stream().anyMatch(player -> player.getUuid().equals(currentPlayer.getUuid()));

        if (!isStillInGame) {
            markedPlayer = Optional.empty();
            chooseNewMarkedPlayer(server);
        }
    }

    public static void updatePlayerFOV(UUID playerUUID, double fov) {
        playerFovMap.put(playerUUID, fov);
    }
}
