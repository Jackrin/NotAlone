package jackrin.notalone.client.model;

import jackrin.notalone.NotAlone;
import jackrin.notalone.client.renderer.NotAloneEntityRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class NotAloneEntityModel extends EntityModel<NotAloneEntityRenderState> {
    public static final ModelLayerLocation ENTITY = new ModelLayerLocation(
            NotAlone.id("notalone_entity"), "main"
    );

    private final ModelPart entity;
    private final ModelPart head;

    public NotAloneEntityModel(ModelPart root) {
        super(root);
        this.entity = root.getChild("entity");
        this.head = entity.getChild("head");
    }

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        PartDefinition entity = root.addOrReplaceChild("entity", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        entity.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(32, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
                PartPose.offset(0.0F, -24.0F, 0.0F)
        );

        entity.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(16, 32)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(0.0F, -24.0F, 0.0F)
        );

        entity.addOrReplaceChild("rightArm", CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(40, 32)
                        .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-5.0F, -22.0F, 0.0F)
        );

        entity.addOrReplaceChild("leftArm", CubeListBuilder.create()
                        .texOffs(32, 48)
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(48, 48)
                        .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(5.0F, -22.0F, 0.0F)
        );

        entity.addOrReplaceChild("rightLeg", CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 32)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(-1.9F, -12.0F, 0.0F)
        );

        entity.addOrReplaceChild("leftLeg", CubeListBuilder.create()
                        .texOffs(16, 48)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 48)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
                PartPose.offset(1.9F, -12.0F, 0.0F)
        );

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(NotAloneEntityRenderState state) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        setHeadAngles(state.yRot, state.xRot);
    }

    private void setHeadAngles(float headYaw, float headPitch) {
        headYaw = Mth.clamp(headYaw, -60.0F, 60.0F);
        headPitch = Mth.clamp(headPitch, -90.0F, 90.0F);
        this.head.yRot = headYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }
}

