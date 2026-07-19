package jackrin.notalone.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import jackrin.notalone.NotAlone;
import jackrin.notalone.client.renderer.WhiteEyesRenderState;
import jackrin.notalone.utils.WhiteEyesAnimalClient;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;

import java.util.Map;
import java.util.logging.Logger;

public class WhiteEyesLayer extends  RenderLayer<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> {
    private static final Map<Class<?>, Identifier> TEXTURES = Map.of(
            Sheep.class, NotAlone.id("textures/entity/sheep/white_eyes.png"),
            Pig.class, NotAlone.id("textures/entity/pig/white_eyes.png"),
            Cow.class, NotAlone.id("textures/entity/cow/white_eyes.png"),
            Chicken.class, NotAlone.id("textures/entity/chicken/white_eyes.png")
    );

    public WhiteEyesLayer(RenderLayerParent<LivingEntityRenderState, EntityModel<LivingEntityRenderState>> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack,
                       SubmitNodeCollector collector,
                       int lightCoords,
                       LivingEntityRenderState state,
                       float yRot,
                       float xRot) {
        if(state instanceof WhiteEyesRenderState abs) {
            if (WhiteEyesAnimalClient.animal_uuid != ((WhiteEyesRenderState) state).getEntity().getUUID()) return;
            Minecraft client = Minecraft.getInstance();
            if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK ||
                    client.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
                return;
            }

            Identifier texture = TEXTURES.get(((WhiteEyesRenderState) state).getEntity().getClass());
            if (texture == null) return;

            RenderType renderType = RenderTypes.entityCutoutZOffset(texture, true);
            int color = ARGB.colorFromFloat(1F, 1F, 1F, 1F);
            collector.submitModel(
                    this.getParentModel(),
                    state,
                    poseStack,
                    renderType,
                    lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    color,
                    null,
                    0,
                    null
            );
        }
    }
}

