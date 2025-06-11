/**
 * Chicken renderer for shearable chickens
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
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.ChickenEntityRenderState;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.util.Identifier;

import net.minecraft.client.render.entity.ChickenEntityRenderer;
import net.minecraft.util.math.MathHelper;


@Environment(EnvType.CLIENT)
public class ShearableChickenEntityRenderer extends AgeableMobEntityRenderer<ChickenEntity, ChickenEntityRenderState, ChickenEntityModel> {

    // Normal texture
    private static final Identifier NORMAL_TEXTURE =
            Identifier.of("minecraft", "textures/entity/chicken.png");

    // Skinned texture
    private static final Identifier SHEARED_TEXTURE =
            Identifier.of("more_shearable_mobs", "textures/entity/chicken/sheared_chicken.png");

    // Butchered texture
    private static final Identifier BUTCHERED_TEXTURE =
            Identifier.of("more_shearable_mobs", "textures/entity/chicken/skelken.png");

    /**
     * Constructor
     *
     * @param context Yeah
     */
    public ShearableChickenEntityRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new ChickenEntityModel(context.getPart(EntityModelLayers.CHICKEN)),
                new ChickenEntityModel(context.getPart(EntityModelLayers.CHICKEN_BABY)),
                0.3F
        );
    }

    /**
     * Subclass that stores the chicken entity
     */
    public static class ShearableChickenRenderState extends ChickenEntityRenderState {
        public ChickenEntity chickenEntity;
    }

    /**
     * @return Chicken render state
     */
    @Override
    public ChickenEntityRenderState createRenderState() {
        return new ShearableChickenRenderState();
    }

    /**
     * Updates the chicken's render state
     *
     * @param chicken   This chicken
     * @param state     The chicken's state
     * @param tickDelta Tick number
     */
    @Override
    public void updateRenderState(ChickenEntity chicken, ChickenEntityRenderState state, float tickDelta) {
        super.updateRenderState(chicken, state, tickDelta);
        state.flapProgress = MathHelper.lerp(tickDelta, chicken.prevFlapProgress, chicken.flapProgress);
        state.maxWingDeviation = MathHelper.lerp(tickDelta, chicken.prevMaxWingDeviation, chicken.maxWingDeviation);
        ((ShearableChickenRenderState) state).chickenEntity = chicken;
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the chicken's shear-state
     */
    @Override
    public Identifier getTexture(ChickenEntityRenderState state) {
        ChickenEntity chicken = ((ShearableChickenRenderState) state).chickenEntity;
        if (!chicken.isBaby() && chicken.getDataTracker().get(IS_CHICK_BUTCHERED)) {
            return BUTCHERED_TEXTURE;
        } else if (chicken.getDataTracker().get(IS_CHICK_SHEARED)) {
            return SHEARED_TEXTURE;
        }
        return NORMAL_TEXTURE;
    }
}