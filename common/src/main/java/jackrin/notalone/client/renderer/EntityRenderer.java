package jackrin.notalone.client.renderer;

import jackrin.notalone.NotAlone;
import jackrin.notalone.client.model.EntityModel;
import jackrin.notalone.entity.NotAloneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;


public class EntityRenderer extends MobRenderer<NotAloneEntity, EntityModel<NotAloneEntity>> {

    public EntityRenderer(EntityRendererProvider.Context context) {
        super(context, new EntityModel<>(context.bakeLayer(EntityModel.ENTITY)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(NotAloneEntity entity) {
        return NotAlone.id("textures/entity/entity/entity.png");
    }

    @Override
    public void render(NotAloneEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}

