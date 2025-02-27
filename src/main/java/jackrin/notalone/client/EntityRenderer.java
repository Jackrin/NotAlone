package jackrin.notalone.client;

import jackrin.notalone.NotAlone;
import jackrin.notalone.entity.NotAloneEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class EntityRenderer extends MobEntityRenderer<NotAloneEntity, EntityModel<NotAloneEntity>> {

    public EntityRenderer(EntityRendererFactory.Context context) {
        super(context, new EntityModel<>(context.getPart(EntityModel.ENTITY)), 0.5f);
    }

    @Override
    public Identifier getTexture(NotAloneEntity entity) {
        return NotAlone.id("textures/entity/entity/entity.png");
    }

    @Override
    public void render(NotAloneEntity livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        matrixStack.scale(1f, 1f, 1f);
        super.render(livingEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }
}
