package jackrin.notalone.network;

import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import net.minecraft.client.Minecraft;
import jackrin.notalone.NotAlone;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;


public class EntityRotationSyncPayload {
    private final int entityId;
    private final float yaw;
    private final float pitch;
    private final float bodyYaw;

    public static final Identifier CHANNEL = NotAlone.id("entity_rotation_sync");
    public static final StreamCodec<FriendlyByteBuf, EntityRotationSyncPayload> STREAM_CODEC = StreamCodec.ofMember(EntityRotationSyncPayload::encode, EntityRotationSyncPayload::new);

    public EntityRotationSyncPayload(int entityId, float yaw, float pitch, float bodyYaw) {
        this.entityId = entityId;
        this.yaw = yaw;
        this.pitch = pitch;
        this.bodyYaw = bodyYaw;
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type()
    {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    public EntityRotationSyncPayload(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.bodyYaw = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeFloat(bodyYaw);
    }

    public static void handle(PacketContext<EntityRotationSyncPayload> ctx)
    {
        if (Side.CLIENT.equals(ctx.side())) {
            Minecraft client = Minecraft.getInstance();
            client.execute(() -> {
                if (client.level == null) return;
                Entity entity = client.level.getEntity(ctx.message().entityId);
                if (entity instanceof Entity myEntity) {
                    myEntity.setYRot(ctx.message().yaw);
                    myEntity.setXRot(ctx.message().pitch);
                    myEntity.setYBodyRot(ctx.message().bodyYaw);
                    myEntity.setYHeadRot(ctx.message().yaw);
                }
            });
        }
    }
}
