
package jackrin.notalone.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import jackrin.notalone.entity.NotAloneEntity;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        
        MinecraftClient client = MinecraftClient.getInstance();

        
        if (entity instanceof NotAloneEntity notAloneEntity) { 
            
            if (client.options.getPerspective() == Perspective.THIRD_PERSON_BACK ||
                client.options.getPerspective() == Perspective.THIRD_PERSON_FRONT ||
                notAloneEntity.seen == true) {
                cir.setReturnValue(false);
            }
        }
    }
}


