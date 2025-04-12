package jackrin.notalone.utils;

import commonnetwork.api.Network;
import jackrin.notalone.mixin.MobAccessor;
import jackrin.notalone.network.WhiteEyesSyncPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class WhiteEyesAnimal {

    public static final double MAX_TRIGGER_DISTANCE = 64;
    public static final double RADIUS = 64;
    public static UUID animal_uuid = null;
    public static boolean stareGoalSet = false;
    public static long endTime = 0L;
    public static final long MARK_DURATION_TICKS = 20L * 60 * 3;

    public static final Map<Animal, SavedGoals> originalGoalsMap = new HashMap<>();

    public static void triggerWhiteEyedAnimalEffect(ServerPlayer player) {
        @SuppressWarnings("resource")
        ServerLevel level = player.serverLevel();
        double radius = 64;

        AABB box = player.getBoundingBox().inflate(radius);

        List<Animal> animals = level.getEntitiesOfClass(Animal.class, box, animal ->
                animal.isAlive()
                        && !animal.isRemoved()
                        && animal.distanceTo(player) <= radius
                        && (animal instanceof Sheep || animal instanceof Pig || animal instanceof Cow)
        );

        if (animals.isEmpty()) return;

        Animal chosen = animals.get(level.getRandom().nextInt(animals.size()));
        ServerLevel overworld = chosen.getServer().overworld();
        animal_uuid = chosen.getUUID();
        endTime = overworld.getGameTime() + MARK_DURATION_TICKS;
        WhiteEyesSyncPayload payload = new WhiteEyesSyncPayload(chosen.getId(), true);
        Network.getNetworkHandler().sendToClient(payload, player);
    }

    public static void tick(Animal animal) {
        if (animal.level().isClientSide) {
            WhiteEyesAnimalClient.tick(animal);
        } else {

            if (animal.getUUID() != animal_uuid)
                return;

            ServerPlayer player = NotAloneUtils.markedPlayer;
            if (player == null || player.level() != animal.level())
                return;

            if (!stareGoalSet) {
                overrideGoals(animal);
            }
        }
    }

    public static void serverReaction(Animal animal, ServerPlayer player) {
        if (!(animal.level() instanceof ServerLevel)) return;

        if (animal instanceof Mob) {
            SavedGoals saved = originalGoalsMap.remove(animal);
            if (saved != null) {
                saved.restore((Mob) animal);
            }
        }

        animal_uuid = null;
        stareGoalSet = false;

        WhiteEyesSyncPayload payload = new WhiteEyesSyncPayload(animal.getId(), false);
        Network.getNetworkHandler().sendToClient(payload, player);
    }

    public static void overrideGoals(Animal animal) {

        ServerPlayer player = NotAloneUtils.markedPlayer;

        if (!(animal instanceof Mob))
            return;
        Mob mob = (Mob) animal;

        if (player == null || player.level() != animal.level())
            return;

        if (!originalGoalsMap.containsKey(animal)) {
            originalGoalsMap.put(animal, new SavedGoals(mob));
        }

        MobAccessor accessor = (MobAccessor) mob;

        accessor.getGoalSelector().getAvailableGoals().clear();
        accessor.getTargetSelector().getAvailableGoals().clear();

        accessor.getGoalSelector().addGoal(0, new Goal() {
            @Override
            public boolean canUse() {
                return mob.distanceTo(player) < MAX_TRIGGER_DISTANCE;
            }
            @Override
            public void tick() {
                mob.getLookControl().setLookAt(player, 10.0F, 10.0F);
            }
        });
        stareGoalSet = true;
    }

    public static class SavedGoals {
        private final List<WrappedGoal> originalGoalEntries;
        private final List<WrappedGoal> originalTargetEntries;

        public SavedGoals(Mob mob) {
            MobAccessor accessor = (MobAccessor) mob;
            this.originalGoalEntries = new ArrayList<>(accessor.getGoalSelector().getAvailableGoals());
            this.originalTargetEntries = new ArrayList<>(accessor.getTargetSelector().getAvailableGoals());
        }

        public void restore(Mob mob) {
            MobAccessor accessor = (MobAccessor) mob;

            accessor.getGoalSelector().getAvailableGoals().clear();
            accessor.getTargetSelector().getAvailableGoals().clear();

            accessor.getGoalSelector().getAvailableGoals().addAll(originalGoalEntries);
            accessor.getTargetSelector().getAvailableGoals().addAll(originalTargetEntries);
        }
    }
}

