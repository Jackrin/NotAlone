package jackrin.notalone.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jackrin.notalone.NotAlone;
import jackrin.notalone.client.renderer.WhiteEyesRenderState;
import jackrin.notalone.entity.NotAloneEntity;
import jackrin.notalone.utils.WhiteEyesAnimalClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;

import java.util.Map;

public class WhiteEyesLayer extends  RenderLayer<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> {

    private static final Map<Class<?>, ResourceLocation> TEXTURES = Map.of(
            Sheep.class, NotAlone.id("textures/entity/sheep/white_eyes.png"),
            Pig.class, NotAlone.id("textures/entity/pig/white_eyes.png"),
            Cow.class, NotAlone.id("textures/entity/cow/white_eyes.png"),
            Chicken.class, NotAlone.id("textures/entity/chicken/white_eyes.png")
    );

    public WhiteEyesLayer(RenderLayerParent<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntityRenderState state,
                       float limbSwing, float limbSwingAmount) {

        if(state instanceof WhiteEyesRenderState abs) {
            if (WhiteEyesAnimalClient.animal_uuid != ((WhiteEyesRenderState) state).getEntity().getUUID()) return;
            Minecraft client = Minecraft.getInstance();
            if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK ||
                    client.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
                return;
            }

            ResourceLocation texture = TEXTURES.get(((WhiteEyesRenderState) state).getEntity().getClass());
            if (texture == null) return;

            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
            this.getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}

