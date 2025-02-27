package jackrin.notalone;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record SyncFovPayload(double fov, double aspectRatio) implements CustomPayload {
    public static final CustomPayload.Id<SyncFovPayload> ID = new CustomPayload.Id<>(Identifier.of("modid", "sync_fov"));

    public static final PacketCodec<PacketByteBuf, SyncFovPayload> CODEC = PacketCodec.of(
        SyncFovPayload::write,
        SyncFovPayload::read
    );

    private void write(PacketByteBuf buf) {
        buf.writeDouble(fov);
        buf.writeDouble(aspectRatio);
    }

    private static SyncFovPayload read(PacketByteBuf buf) {
        return new SyncFovPayload(buf.readDouble(), buf.readDouble());
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
