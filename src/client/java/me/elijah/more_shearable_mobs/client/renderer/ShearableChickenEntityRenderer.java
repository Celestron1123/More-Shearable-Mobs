/**
 * Chicken renderer for shearable chickens
 *
 * @auther Elijah Potter
 * @date 03/25/2026
 */

package me.elijah.more_shearable_mobs.client.renderer;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;

@Environment(EnvType.CLIENT)
public class ShearableChickenEntityRenderer extends ChickenRenderer {

    // Skinned textures
    private static final Identifier SHEARED_TEXTURE_TEMP =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/chicken/shearedchicken_temp.png");

    private static final Identifier SHEARED_TEXTURE_COLD =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/chicken/shearedchicken_cold.png");

    private static final Identifier SHEARED_TEXTURE_WARM =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/chicken/shearedchicken_warm.png");

    // Butchered textures
    private static final Identifier BUTCHERED_TEXTURE_TEMP =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/chicken/skelken_temp.png");

    private static final Identifier BUTCHERED_TEXTURE_COLD =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/chicken/skelken_cold.png");

    private static final Identifier BUTCHERED_TEXTURE_WARM =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/chicken/skelken_warm.png");

    /**
     * Constructor
     *
     * @param context Renderer Context
     */
    public ShearableChickenEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Subclass that stores the shear states safely for the render thread
     */
    public static class ShearableChickenRenderState extends ChickenRenderState {
        public boolean isSheared;
        public boolean isButchered;
        public boolean isWarm;
        public boolean isCold;
    }

    /**
     * @return Custom Chicken render state
     */
    @Override
    public ChickenRenderState createRenderState() {
        return new ShearableChickenRenderState();
    }

    /**
     * Extracts variables from the chicken entity safely into the render state
     *
     * @param chicken   This chicken
     * @param state     The chicken's state
     * @param tickDelta Tick number
     */
    @Override
    public void extractRenderState(Chicken chicken, ChickenRenderState state, float tickDelta) {
        super.extractRenderState(chicken, state, tickDelta);
        ShearableChickenRenderState shearableState = (ShearableChickenRenderState) state;
        shearableState.isSheared = chicken.getEntityData().get(IS_CHICK_SHEARED);
        shearableState.isButchered = chicken.getEntityData().get(IS_CHICK_BUTCHERED);
        shearableState.isWarm = chicken.getVariant().is(ChickenVariants.WARM);
        shearableState.isCold = chicken.getVariant().is(ChickenVariants.COLD);
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the chicken's shear-state
     */
    @Override
    public Identifier getTextureLocation(ChickenRenderState state) {
        ShearableChickenRenderState shearableState = (ShearableChickenRenderState) state;
        if (!shearableState.isBaby && shearableState.isButchered) {
            if (shearableState.isWarm) return BUTCHERED_TEXTURE_WARM;
            if (shearableState.isCold) return BUTCHERED_TEXTURE_COLD;
            return BUTCHERED_TEXTURE_TEMP;
        } else if (shearableState.isSheared) {
            if (shearableState.isWarm) return SHEARED_TEXTURE_WARM;
            if (shearableState.isCold) return SHEARED_TEXTURE_COLD;
            return SHEARED_TEXTURE_TEMP;
        }
        return super.getTextureLocation(state);
    }
}