package jackrin.notalone.init;

import jackrin.notalone.client.ClientSyncHandler;
import jackrin.notalone.client.model.EntityModel;
import jackrin.notalone.client.renderer.EntityRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class FabricClientRegistrations {
    public static void init() {
        EntityModelLayerRegistry.registerModelLayer(EntityModel.ENTITY, EntityModel::createLayerDefinition);
        EntityRendererRegistry.register(ModEntities.ENTITY, EntityRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            client.execute(() -> {
                ClientSyncHandler.tickClient(client);
            });
        });
    }
}