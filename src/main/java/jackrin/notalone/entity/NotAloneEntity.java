package jackrin.notalone.entity;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;


public class NotAloneEntity extends PathAwareEntity {
    private int timeSeen = 0; 
    private int timeExisted = 0; 
    private static final int MAX_SEEN_TIME_NEAR = 1;  
    private static final int MAX_SEEN_TIME_FAR = 10; 
    private static final double INSTANT_DISAPPEAR_CONE = 60.0;
    private static final int MAX_EXISTENCE_TIME = 3000;
    public boolean seen = false;

    public NotAloneEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
        this.setPersistent();
        this.setInvulnerable(true);
    }

    
    private void syncRotation() {
        EntityRotationSyncPayload payload = new EntityRotationSyncPayload(this.getId(), this.getYaw(), this.getPitch(), this.bodyYaw);

        for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
            ServerPlayNetworking.send(player, payload); 
        }
    }

    @Override
    protected void initGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        this.setPersistent();
        timeExisted++; 
        if (timeExisted >= MAX_EXISTENCE_TIME) {
            this.seen = true;
            if (!this.getWorld().isClient) {
                this.remove(RemovalReason.DISCARDED);
            }
            return;
        }
        if (!this.getWorld().isClient) {
            for (PlayerEntity player : this.getWorld().getPlayers()) {
                double distance = this.distanceTo(player);
    
                if (distance < 16) {
                    this.remove(RemovalReason.DISCARDED); 
                    return;
                }
    
                if (NotAloneUtils.isInPlayerView(player, this, this.getWorld())) {
                    
                    if (NotAloneUtils.isInTightViewCone(player, this, INSTANT_DISAPPEAR_CONE)) {
                        this.remove(RemovalReason.DISCARDED); 
                        return;
                    }
    
                    
                    int maxSeenTime = distance < 64 ? MAX_SEEN_TIME_NEAR : MAX_SEEN_TIME_FAR;
    
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
        
        if (this.getWorld().isClient) {
            PlayerEntity clientPlayer = MinecraftClient.getInstance().player;
            if (clientPlayer != null) {
                double distance = this.distanceTo(clientPlayer);
                if (distance < 16) {
                    this.seen = true;
                    return;
                }
                if (NotAloneUtils.isInPlayerView(clientPlayer, this, this.getWorld())) {
                    if (NotAloneUtils.isInTightViewCone(clientPlayer, this, INSTANT_DISAPPEAR_CONE)) {
                        this.seen = true;
                        return;
                    }
                    int maxSeenTime = distance < 64 ? MAX_SEEN_TIME_NEAR : MAX_SEEN_TIME_FAR;
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

        if (this.getWorld().isClient) {
            return;
        }

        
        PlayerEntity nearestPlayer = this.getWorld().getClosestPlayer(this, 128D);

        if (nearestPlayer != null) {
            
            double dx = nearestPlayer.getX() - this.getX();
            double dz = nearestPlayer.getZ() - this.getZ();
            double dy = (nearestPlayer.getY() + nearestPlayer.getEyeHeight(nearestPlayer.getPose())) - (this.getY() + this.getEyeHeight(this.getPose()));

            
            float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180.0F / Math.PI)) - 90.0F;
            float targetPitch = (float) -(MathHelper.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0F / Math.PI));

            
            this.setYaw(targetYaw);
            this.setPitch(targetPitch);

            
            this.bodyYaw = targetYaw;
            this.headYaw = targetYaw;
            this.prevBodyYaw = targetYaw;  

            syncRotation();
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!this.getWorld().isClient) {
            this.remove(RemovalReason.DISCARDED); 
        }
    }

    @Override
    protected void dropLoot(DamageSource source, boolean causedByPlayer) {
        
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
        
    }

    @Override
    public void kill() {
        if (!this.getWorld().isClient) {
            this.remove(RemovalReason.KILLED); 
        }
    }


    @Override
    public void handleStatus(byte status) {
        if (status == 2) return; 
        super.handleStatus(status);
    }

    @Override
    public boolean isAttackable() {
        return false; 
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return !damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY); 
    }


}
