package jackrin.notalone.init;

import jackrin.notalone.NotAlone;
import jackrin.notalone.entity.NotAloneEntity;
import jackrin.notalone.utils.NotAloneUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class FabricRegistrations {

    public static void init() {
        ModEntities.ENTITY = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                NotAlone.id("notalone_entity"),
                EntityType.Builder.of(NotAloneEntity::new, MobCategory.MISC)
                        .sized(0.6F, 1.8F)
                        .build("notalone_entity")
        );
        FabricDefaultAttributeRegistry.register(
                ModEntities.ENTITY,
                NotAloneEntity.createMobAttributes()
        );

        ServerTickEvents.END_WORLD_TICK.register(NotAloneUtils::trySpawnEntity);
        ServerTickEvents.END_SERVER_TICK.register(NotAloneUtils::checkMarkExpiration);
    }
}