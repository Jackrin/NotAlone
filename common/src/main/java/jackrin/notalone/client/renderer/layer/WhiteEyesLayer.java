package jackrin.notalone.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import jackrin.notalone.NotAlone;
import jackrin.notalone.entity.NotAloneEntity;
import jackrin.notalone.utils.WhiteEyesAnimalClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Sheep;

import java.util.Map;

public class WhiteEyesLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private static final Map<Class<?>, ResourceLocation> TEXTURES = Map.of(
            Sheep.class, NotAlone.id("textures/entity/sheep/white_eyes.png"),
            Pig.class, NotAlone.id("textures/entity/pig/white_eyes.png"),
            Cow.class, NotAlone.id("textures/entity/cow/white_eyes.png"),
            Chicken.class, NotAlone.id("textures/entity/chicken/white_eyes.png")
    );

    public WhiteEyesLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        if (WhiteEyesAnimalClient.animal_uuid != entity.getUUID()) return;

        Minecraft client = Minecraft.getInstance();

        if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK ||
                client.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
            return;
        }

        ResourceLocation texture = TEXTURES.get(entity.getClass());
        if (texture == null) return;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        this.getParentModel().renderToBuffer(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}

