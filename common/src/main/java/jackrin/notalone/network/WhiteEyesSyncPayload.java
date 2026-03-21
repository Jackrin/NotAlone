package jackrin.notalone.network;

import commonnetwork.networking.data.PacketContext;
import commonnetwork.networking.data.Side;
import jackrin.notalone.NotAlone;
import jackrin.notalone.utils.NotAloneUtils;
import jackrin.notalone.utils.WhiteEyesAnimal;
import jackrin.notalone.utils.WhiteEyesAnimalClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WhiteEyesSyncPayload {
    private final int entityId;
    private final boolean hasWhiteEyes;

    public static final ResourceLocation CHANNEL = NotAlone.id("white_eyes");
    public static final StreamCodec<FriendlyByteBuf, WhiteEyesSyncPayload> STREAM_CODEC = StreamCodec.ofMember(
            WhiteEyesSyncPayload::encode, WhiteEyesSyncPayload::new
    );

    public WhiteEyesSyncPayload(int entityId, boolean hasWhiteEyes) {
        this.entityId = entityId;
        this.hasWhiteEyes = hasWhiteEyes;
    }

    public static CustomPacketPayload.Type<CustomPacketPayload> type() {
        return new CustomPacketPayload.Type<>(CHANNEL);
    }

    public WhiteEyesSyncPayload(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.hasWhiteEyes = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeBoolean(hasWhiteEyes);
    }

    public static void handle(PacketContext<WhiteEyesSyncPayload> ctx) {
        if (Side.CLIENT.equals(ctx.side())) {
            Minecraft client = Minecraft.getInstance();
            if (client.level == null) return;

            Entity entity = client.level.getEntity(ctx.message().entityId);
            if (entity instanceof LivingEntity living) {
                if (ctx.message().hasWhiteEyes) {
                    WhiteEyesAnimalClient.animal_uuid = living.getUUID();
                } else {
                    WhiteEyesAnimalClient.animal_uuid = null;
                }
            }
        } else {
            ServerPlayer player = ctx.sender();
            if (player == null) return;

            Entity entity = player.level().getEntity(ctx.message().entityId);
            if (entity instanceof LivingEntity living) {
                if (!ctx.message().hasWhiteEyes) {
                    if (entity instanceof Animal animal) {
                        WhiteEyesAnimal.animal_uuid = null;
                        WhiteEyesAnimal.stareGoalSet = false;
                        WhiteEyesAnimal.serverReaction(animal, player);
                    }
                }
            }
        }
    }
}
