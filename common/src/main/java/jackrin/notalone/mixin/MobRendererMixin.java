package jackrin.notalone.mixin;

import jackrin.notalone.client.renderer.layer.WhiteEyesLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobRenderer.class)
public abstract class MobRendererMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityRendererProvider.Context context, EntityModel model, float shadowRadius, CallbackInfo ci) {
        LivingEntityRenderer<?, ?> renderer = (LivingEntityRenderer<?, ?>)(Object) this;

        Class<?> clazz = this.getClass();
        if (
                clazz == SheepRenderer.class ||
                clazz == PigRenderer.class ||
                clazz == CowRenderer.class
        ) {
            LivingEntityRendererAccessor accessor = (LivingEntityRendererAccessor) renderer;
            accessor.getLayers().add(new WhiteEyesLayer<>(renderer));
        }

    }
}
