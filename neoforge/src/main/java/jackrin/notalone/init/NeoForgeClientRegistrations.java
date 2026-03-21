package jackrin.notalone.init;

import jackrin.notalone.Constants;
import jackrin.notalone.client.ClientSyncHandler;
import jackrin.notalone.client.renderer.NotAloneEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod(Constants.MOD_ID)
public class NeoForgeClientRegistrations {

    @EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class ClientGameEvents {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft client = Minecraft.getInstance();
            ClientSyncHandler.tickClient(client);
        }
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                EntityRenderers.register(ModEntities.ENTITY, NotAloneEntityRenderer::new);
            });
        }
    }
}
