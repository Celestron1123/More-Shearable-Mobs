package me.elijah.shearable_cows.client.renderer;

import me.elijah.shearable_cows.CowDataTrackers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.AgeableMobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ShearableCowEntityRenderer extends AgeableMobEntityRenderer<CowEntity, LivingEntityRenderState, CowEntityModel> {

    private static final Identifier NORMAL_TEXTURE =
            Identifier.of("minecraft", "textures/entity/cow/cow.png");

    private static final Identifier SHEARED_TEXTURE =
            Identifier.of("shearable_cows", "textures/entity/cow/sheared_cow.png");

    public ShearableCowEntityRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new CowEntityModel(context.getPart(EntityModelLayers.COW)),
                new CowEntityModel(context.getPart(EntityModelLayers.COW_BABY)),
                0.7F
        );
    }

    // Custom render state that stores the cow entity
    public static class ShearableCowRenderState extends LivingEntityRenderState {
        public CowEntity cowEntity;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new ShearableCowRenderState();
    }

    @Override
    public void updateRenderState(CowEntity cow, LivingEntityRenderState state, float tickDelta) {
        super.updateRenderState(cow, state, tickDelta);
        ((ShearableCowRenderState) state).cowEntity = cow;
    }

    @Override
    public Identifier getTexture(LivingEntityRenderState state) {
        CowEntity cow = ((ShearableCowRenderState) state).cowEntity;
        if (!cow.isBaby() && cow.getDataTracker().get(CowDataTrackers.IS_SHEARED)) {
            return SHEARED_TEXTURE;
        }
        return NORMAL_TEXTURE;
    }
}
