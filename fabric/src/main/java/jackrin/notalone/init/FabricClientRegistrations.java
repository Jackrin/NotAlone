package jackrin.notalone.init;

import jackrin.notalone.client.ClientSyncHandler;
import jackrin.notalone.client.model.NotAloneEntityModel;
import jackrin.notalone.client.renderer.NotAloneEntityRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class FabricClientRegistrations {
    public static void init() {
        EntityModelLayerRegistry.registerModelLayer(NotAloneEntityModel.ENTITY, NotAloneEntityModel::createLayerDefinition);
        EntityRendererRegistry.register(ModEntities.ENTITY, NotAloneEntityRenderer::new);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            client.execute(() -> {
                ClientSyncHandler.tickClient(client);
            });
        });
    }
}