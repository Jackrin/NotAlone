
package jackrin.notalone.mixin;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import jackrin.notalone.entity.NotAloneEntity;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {

        Minecraft client = Minecraft.getInstance();


        if (entity instanceof NotAloneEntity notAloneEntity) {

            if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK ||
                    client.options.getCameraType() == CameraType.THIRD_PERSON_FRONT ||
                    notAloneEntity.seen) {
                cir.setReturnValue(false);
            }
        }
    }
}


