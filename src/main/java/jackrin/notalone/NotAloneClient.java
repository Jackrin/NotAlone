package jackrin.notalone;

import jackrin.notalone.client.EntityModel;
import jackrin.notalone.client.EntityRenderer;
import jackrin.notalone.entity.EntityRotationSyncPayload;
import jackrin.notalone.entity.NotAloneEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

public class NotAloneClient implements ClientModInitializer {
    private static double lastSentFov = 70.0; 
    private static double lastSentAspectRatio = 16.0 / 9.0;
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(EntityModel.ENTITY, EntityModel::getTexturedModelData);
        EntityRendererRegistry.register(NotAlone.ENTITY, EntityRenderer::new);
        PayloadTypeRegistry.playS2C().register(SyncFovPayload.ID, SyncFovPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(EntityRotationSyncPayload.ID, (payload, context) -> {
            MinecraftClient client = MinecraftClient.getInstance();
    
            client.execute(() -> {
                if (client.world == null) return; 
                
                Entity entity = client.world.getEntityById(payload.entityId());
                if (entity instanceof NotAloneEntity myEntity) {
                    myEntity.setYaw(payload.yaw());
                    myEntity.setPitch(payload.pitch());
                    myEntity.bodyYaw = payload.bodyYaw();
                    myEntity.headYaw = payload.yaw();
                    myEntity.prevBodyYaw = payload.bodyYaw(); 
                }
            });
        });

        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                double playerFov = client.options.getFov().getValue();
                double aspectRatio = (double) client.getWindow().getFramebufferWidth() / client.getWindow().getFramebufferHeight();
    
                if (playerFov != lastSentFov || aspectRatio != lastSentAspectRatio) {
                    lastSentFov = playerFov;
                    lastSentAspectRatio = aspectRatio;
    
                    ClientPlayNetworking.send(new SyncFovPayload(playerFov, aspectRatio));
                }
            }
        });
    }
    
}
