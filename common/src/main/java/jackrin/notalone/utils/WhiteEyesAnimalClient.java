package jackrin.notalone.utils;

import commonnetwork.api.Network;
import jackrin.notalone.client.ClientSyncHandler;
import jackrin.notalone.mixin.MobAccessor;
import jackrin.notalone.network.WhiteEyesSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhiteEyesAnimalClient {

    public static final double MAX_TRIGGER_DISTANCE = 64.0;
    public static final Map<Animal, WhiteEyesAnimal.SavedGoals> originalGoalsMap = new HashMap<>();
    public static UUID animal_uuid  = null;
    public static boolean stareGoalSet = false;

    public static void tick(Animal animal) {
        if (animal.getUUID() != animal_uuid)
            return;

        if (!stareGoalSet) {
            overrideGoals(animal);
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) return;

        double distance = player.distanceTo(animal);
        if (distance > MAX_TRIGGER_DISTANCE) return;

        Vec3 playerEyePos = player.getEyePosition();
        Vec3 animalFace = getAnimalFacePosition(animal);

        Vec3 toFace = animalFace.subtract(playerEyePos).normalize();
        Vec3 playerViewDirection = player.getLookAngle().normalize();

        double playerFov = ClientSyncHandler.lastSentFov;
        double aspectRatio = ClientSyncHandler.lastSentAspectRatio;

        double verticalHalfFovRadians = Math.toRadians(playerFov) / 2.0;
        double horizontalHalfFovRadians = Math.atan(Math.tan(verticalHalfFovRadians) * aspectRatio);

        double minDistance = 10.0;
        double maxDistance = 64.0;
        double dynamicFactor;
        if (distance <= minDistance) {
            dynamicFactor = 0.95;
        } else if (distance >= maxDistance) {
            dynamicFactor = 0.5;
        } else {
            double t = (distance - minDistance) / (maxDistance - minDistance);
            dynamicFactor = 0.95 - t * (0.95 - 0.5);
        }

        double modifiedHorizontalHalfFovRadians = horizontalHalfFovRadians * dynamicFactor;
        double dynamicFovThreshold = Math.cos(modifiedHorizontalHalfFovRadians);

        double fovDot = playerViewDirection.dot(toFace);
        if (fovDot < dynamicFovThreshold) return;

        Vec3 toPlayer = playerEyePos.subtract(animalFace).normalize();
        Vec3 animalFacing = getAnimalFacingDirection(animal);
        double animalDot = animalFacing.dot(toPlayer);
        if (animalDot < dynamicFovThreshold) return;

        Network.getNetworkHandler().sendToServer(new WhiteEyesSyncPayload(animal.getId(), false));
        stareGoalSet = false;
        TickTaskScheduler.schedule(() -> {
            animal_uuid = null;
        }, 20);
    }

    public static Vec3 getAnimalFacePosition(Animal animal) {
        float headYaw = animal.getYHeadRot();
        double rad = Math.toRadians(headYaw);
        Vec3 forward = new Vec3(-Math.sin(rad), 0, Math.cos(rad)).scale(0.3);
        return animal.position().add(forward).add(0, animal.getEyeHeight(), 0);
    }

    public static Vec3 getAnimalFacingDirection(Animal animal) {
        float headYaw = animal.getYHeadRot();
        double rad = Math.toRadians(headYaw);
        return new Vec3(-Math.sin(rad), 0, Math.cos(rad)).normalize();
    }

    public static void overrideGoals(Animal animal) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (!(animal instanceof Mob))
            return;
        Mob mob = (Mob) animal;

        if (player == null || player.level() != animal.level())
            return;

        if (!originalGoalsMap.containsKey(animal)) {
            originalGoalsMap.put(animal, new WhiteEyesAnimal.SavedGoals(mob));
        }

        MobAccessor accessor = (MobAccessor) mob;

        mob.getGoalSelector().getAvailableGoals().clear();
        accessor.getTargetSelector().getAvailableGoals().clear();

        mob.getGoalSelector().addGoal(0, new Goal() {
            @Override
            public boolean canUse() {
                return player != null && mob.distanceTo(player) < MAX_TRIGGER_DISTANCE;
            }
            @Override
            public void tick() {
                mob.getLookControl().setLookAt(player, 10.0F, 10.0F);
            }
        });
        stareGoalSet = true;
    }
}
