package jackrin.notalone.client.renderer;

import jackrin.notalone.NotAlone;
import jackrin.notalone.client.model.NotAloneEntityModel;
import jackrin.notalone.entity.NotAloneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;


public class NotAloneEntityRenderer extends MobRenderer<NotAloneEntity, NotAloneEntityRenderState, NotAloneEntityModel> {

    public NotAloneEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new NotAloneEntityModel(context.bakeLayer(NotAloneEntityModel.ENTITY)), 0.5f);
    }

    @Override
    public NotAloneEntityRenderState createRenderState() {
        return new NotAloneEntityRenderState();
    }

    @Override
    public ResourceLocation getTextureLocation(NotAloneEntityRenderState renderState) {
        return NotAlone.id("textures/entity/entity/entity.png");
    }

    @Override
    public void render(NotAloneEntityRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(renderState, poseStack, bufferSource, packedLight);
    }
}

