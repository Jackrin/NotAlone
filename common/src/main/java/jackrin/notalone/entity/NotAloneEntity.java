package jackrin.notalone.entity;

import commonnetwork.api.Network;
import jackrin.notalone.client.ClientUtils;
import jackrin.notalone.network.EntityRotationSyncPayload;
import jackrin.notalone.utils.NotAloneUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings("resource")
public class NotAloneEntity extends PathfinderMob {
    private int timeSeen = 0;
    private int timeExisted = 0;
    private static final double INSTANT_DISAPPEAR_CONE = 70.0;
    private static final int MAX_EXISTENCE_TIME = 3000;
    public boolean seen = false;

    public NotAloneEntity(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
        this.setPersistenceRequired();
        this.setInvulnerable(true);
    }

    private int getMaxSeenTime(double distance) {
        final int minSeenTime = 2;
        final int maxSeenTime = 20;
        final double minDistance = 24.0;
        final double maxDistance = 96.0;

        if (distance <= minDistance) {
            return minSeenTime;
        } else if (distance >= maxDistance) {
            return maxSeenTime;
        } else {
            double factor = (distance - minDistance) / (maxDistance - minDistance);
            return minSeenTime + (int)Math.round(factor * (maxSeenTime - minSeenTime));
        }
    }

    private void syncRotation() {
        EntityRotationSyncPayload payload = new EntityRotationSyncPayload(this.getId(), this.getYRot(), this.getXRot(), this.yBodyRot);

        if (this.level() instanceof ServerLevel serverLevel) {
            MinecraftServer server = serverLevel.getServer();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                Network.getNetworkHandler().sendToClient(payload, player);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.setPersistenceRequired();
        timeExisted++;
        if (timeExisted >= MAX_EXISTENCE_TIME) {
            this.seen = true;
            if (!this.level().isClientSide) {
                this.remove(RemovalReason.DISCARDED);
            }
            return;
        }
        if (!this.level().isClientSide) {
            for (Player player : this.level().players()) {
                double distance = this.distanceTo(player);

                if (distance < 16) {
                    this.remove(RemovalReason.DISCARDED);
                    return;
                }

                if (NotAloneUtils.isInPlayerView(player, this, this.level(), 0.2)) {

                    if (NotAloneUtils.isInTightViewCone(player, this, INSTANT_DISAPPEAR_CONE)) {
                        this.remove(RemovalReason.DISCARDED);
                        return;
                    }

                    int maxSeenTime = getMaxSeenTime(distance);
                    timeSeen++;
                    if (timeSeen >= maxSeenTime) {
                        this.remove(RemovalReason.DISCARDED);
                        return;
                    }
                } else {
                    timeSeen = 0;
                }
            }
        }

        if (this.level().isClientSide) {
            Player clientPlayer = ClientUtils.getClientPlayer();
            if (clientPlayer != null) {
                double distance = this.distanceTo(clientPlayer);
                if (distance < 16) {
                    this.seen = true;
                    return;
                }
                if (NotAloneUtils.isInPlayerView(clientPlayer, this, this.level(), 0.2)) {
                    if (NotAloneUtils.isInTightViewCone(clientPlayer, this, INSTANT_DISAPPEAR_CONE)) {
                        this.seen = true;
                        return;
                    }
                    int maxSeenTime = getMaxSeenTime(distance);
                    timeSeen++;
                    if (timeSeen >= maxSeenTime) {
                        this.seen = true;
                        return;
                    }
                } else {
                    timeSeen = 0;
                }
            }
        }

        if (this.level().isClientSide) {
            return;
        }


        Player nearestPlayer = this.level().getNearestPlayer(this, 128D);

        if (nearestPlayer != null) {

            double dx = nearestPlayer.getX() - this.getX();
            double dz = nearestPlayer.getZ() - this.getZ();
            double dy = (nearestPlayer.getY() + nearestPlayer.getEyeHeight(nearestPlayer.getPose())) - (this.getY() + this.getEyeHeight(this.getPose()));


            float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0F / Math.PI)) - 90.0F;
            float targetPitch = (float) -(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0F / Math.PI));


            this.setYRot(targetYaw);
            this.setXRot(targetPitch);


            this.yBodyRot = targetYaw;
            this.yHeadRot = targetYaw;
            this.yBodyRotO = targetYaw;

            syncRotation();
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceSquared) {
        return false;
    }


    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide()) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void kill(@NotNull ServerLevel level) {
        if (!this.level().isClientSide) {
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (status == 2) return;
        super.handleEntityEvent(status);
    }


    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(@NotNull ServerLevel level, DamageSource damageSource) {
        return !damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

}

