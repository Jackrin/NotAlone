package jackrin.notalone;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jackrin.notalone.entity.EntityRotationSyncPayload;
import jackrin.notalone.entity.NotAloneEntity;
import jackrin.notalone.entity.NotAloneUtils;

public class NotAlone implements ModInitializer {
	public static final String MOD_ID = "notalone";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier ENTITY_SPAWN_ID = NotAlone.id("entity_spawn");

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static final EntityType<NotAloneEntity> ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            NotAlone.id("notalone_entity"),
            EntityType.Builder.create(NotAloneEntity::new, SpawnGroup.MISC).dimensions(0.6f, 1.8f).build()
    );

	@Override
	public void onInitialize() {
		FabricDefaultAttributeRegistry.register(ENTITY, NotAloneEntity.createMobAttributes());
		PayloadTypeRegistry.playS2C().register(EntityRotationSyncPayload.ID, EntityRotationSyncPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SyncFovPayload.ID, SyncFovPayload.CODEC);
		ServerTickEvents.END_WORLD_TICK.register(NotAloneUtils::trySpawnEntity);

		
		ServerPlayNetworking.registerGlobalReceiver(SyncFovPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			NotAloneUtils.playerFovMap.put(player.getUuid(), payload.fov());
			NotAloneUtils.playerAspectRatioMap.put(player.getUuid(), payload.aspectRatio());
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			NotAloneUtils.checkMarkExpiration(server); 
		});
		
    }
}