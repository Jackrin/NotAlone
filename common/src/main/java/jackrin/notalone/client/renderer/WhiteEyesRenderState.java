package jackrin.notalone.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
public interface WhiteEyesRenderState {

    Entity getEntity();
    void setEntity(Entity entity);
}
