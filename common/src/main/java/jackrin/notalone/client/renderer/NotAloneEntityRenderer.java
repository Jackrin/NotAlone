package jackrin.notalone.client.renderer;

import jackrin.notalone.NotAlone;
import jackrin.notalone.client.model.NotAloneEntityModel;
import jackrin.notalone.entity.NotAloneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;

public class NotAloneEntityRenderer extends MobRenderer<NotAloneEntity, NotAloneEntityRenderState, NotAloneEntityModel> {

    public NotAloneEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new NotAloneEntityModel(context.bakeLayer(NotAloneEntityModel.ENTITY)), 0.5f);
    }

    @Override
    public NotAloneEntityRenderState createRenderState() {
        return new NotAloneEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(NotAloneEntityRenderState state) {
        return NotAlone.id("textures/entity/entity/entity.png");
    }

    @Override
    public void submit(NotAloneEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        super.submit(renderState, poseStack, collector, cameraRenderState);
    }
}

