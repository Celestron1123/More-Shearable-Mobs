/**
 * Sheep renderer for shearable sheep
 *
 * @auther Elijah Potter
 * @date 5/19/2025
 */

package me.elijah.more_shearable_mobs.client.renderer;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.AgeableMobEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.Identifier;

// For refs
import net.minecraft.client.render.entity.SheepEntityRenderer;
import net.minecraft.client.render.entity.feature.SheepWoolFeatureRenderer;

@Environment(EnvType.CLIENT)
public class ShearableSheepEntityRenderer extends AgeableMobEntityRenderer<SheepEntity, SheepEntityRenderState, SheepEntityModel> {

    // Normal texture
    private static final Identifier NORMAL_TEXTURE =
            Identifier.of("minecraft", "textures/entity/sheep/sheep.png");

    // Butchered texture
    private static final Identifier BUTCHERED_TEXTURE =
            Identifier.of("more_shearable_mobs", "textures/entity/sheep/skeleshep_wooled.png");

    /**
     * Constructor
     *
     * @param context Yeah
     */
    public ShearableSheepEntityRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new SheepEntityModel(context.getPart(EntityModelLayers.SHEEP)),
                new SheepEntityModel(context.getPart(EntityModelLayers.SHEEP_BABY)),
                0.7F
        );
        this.addFeature(new SheepWoolFeatureRenderer(this, context.getEntityModels()));
    }

    /**
     * Subclass that stores the sheep entity
     */
    public static class ShearableSheepRenderState extends SheepEntityRenderState {
        public SheepEntity sheepEntity;
    }

    /**
     * @return Sheep render state
     */
    @Override
    public SheepEntityRenderState createRenderState() {
        return new ShearableSheepRenderState();
    }

    /**
     * Updates the sheep's render state
     *
     * @param sheep     This sheep
     * @param state     The sheep's state
     * @param tickDelta Tick number
     */
    @Override
    public void updateRenderState(SheepEntity sheep, SheepEntityRenderState state, float tickDelta) {
        super.updateRenderState(sheep, state, tickDelta);
        state.headAngle = sheep.getHeadAngle(tickDelta);
        state.neckAngle = sheep.getNeckAngle(tickDelta);
        state.sheared = sheep.isSheared();
        state.color = sheep.getColor();
        state.id = sheep.getId();
        ((ShearableSheepRenderState) state).sheepEntity = sheep;
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the sheep's shear-state
     */
    @Override
    public Identifier getTexture(SheepEntityRenderState state) {
        SheepEntity sheep = ((ShearableSheepRenderState) state).sheepEntity;
        if (sheep.getDataTracker().get(IS_SHEEP_BUTCHERED)) {
            return BUTCHERED_TEXTURE;
        }
        return NORMAL_TEXTURE;
    }
}

