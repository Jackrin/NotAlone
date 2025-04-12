package jackrin.notalone.init;

import jackrin.notalone.Constants;
import jackrin.notalone.NotAlone;
import jackrin.notalone.client.ClientSyncHandler;
import jackrin.notalone.client.model.EntityModel;
import jackrin.notalone.client.renderer.EntityRenderer;
import jackrin.notalone.entity.NotAloneEntity;
import jackrin.notalone.utils.NotAloneUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.Supplier;

@Mod(Constants.MOD_ID)
public class NeoForgeRegistrations {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);

    public static final Supplier<EntityType<NotAloneEntity>> NOT_ALONE_ENTITY =
            ENTITY_TYPES.register("notalone_entity", () -> {
                EntityType<NotAloneEntity> entityType = EntityType.Builder.of(NotAloneEntity::new, MobCategory.MISC)
                        .sized(0.6F, 1.8F)
                        .build(NotAlone.id("notalone_entity").toString());

                ModEntities.ENTITY = entityType;

                return entityType;
            });

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register((eventBus));
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {
        @SubscribeEvent
        public static void onWorldTick(LevelTickEvent.Post  event) {
            if (!event.getLevel().isClientSide()) {
                if (event.getLevel() instanceof ServerLevel serverLevel) {
                    NotAloneUtils.trySpawnEntity(serverLevel);
                }
            }
        }

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post  event) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;
            NotAloneUtils.checkMarkExpiration(server);
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ServerEvents {

        @SubscribeEvent
        public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(EntityModel.ENTITY, EntityModel::createLayerDefinition);
        }

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.ENTITY, NotAloneEntity.createMobAttributes().build());
        }
    }
}
