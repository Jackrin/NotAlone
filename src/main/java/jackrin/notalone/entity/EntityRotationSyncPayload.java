package jackrin.notalone.entity;

import jackrin.notalone.NotAlone;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record EntityRotationSyncPayload(int entityId, float yaw, float pitch, float bodyYaw) implements CustomPayload {
    public static final CustomPayload.Id<EntityRotationSyncPayload> ID = new CustomPayload.Id<>(NotAlone.id("entity_rotation_sync")); 

    public static final PacketCodec<PacketByteBuf, EntityRotationSyncPayload> CODEC = PacketCodec.of(
        EntityRotationSyncPayload::write,
        EntityRotationSyncPayload::read
    );

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeFloat(bodyYaw);
    }

    private static EntityRotationSyncPayload read(PacketByteBuf buf) {
        return new EntityRotationSyncPayload(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID; 
    }
}