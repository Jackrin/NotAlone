package jackrin.notalone.network;

import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import jackrin.notalone.NotAlone;
import jackrin.notalone.utils.NotAloneUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class SyncFovPayload {
    private final double fov;
    private final double aspectRatio;

    public static final ResourceLocation CHANNEL = NotAlone.id("sync_fov");
    public static final StreamCodec<FriendlyByteBuf, SyncFovPayload> STREAM_CODEC = StreamCodec.ofMember(SyncFovPayload::encode, SyncFovPayload::new);

    public SyncFovPayload(double fov, double aspectRatio) {
        this.fov = fov;
        this.aspectRatio = aspectRatio;
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type()
    {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    public SyncFovPayload(FriendlyByteBuf buf) {
        this.fov = buf.readDouble();
        this.aspectRatio = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeDouble(fov);
        buf.writeDouble(aspectRatio);
    }

    public static void handle(PacketContext<SyncFovPayload> ctx)
    {
        if (Side.SERVER.equals(ctx.side())) {
            ServerPlayer player = ctx.sender();
            NotAloneUtils.playerFovMap.put(player.getUUID(), ctx.message().fov);
            NotAloneUtils.playerAspectRatioMap.put(player.getUUID(), ctx.message().aspectRatio);
        }
    }
}
