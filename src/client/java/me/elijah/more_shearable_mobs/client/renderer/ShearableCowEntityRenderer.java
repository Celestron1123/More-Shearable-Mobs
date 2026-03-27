/**
 * Cow renderer for shearable cows
 *
 * @author Elijah Potter
 * @date 3/25/2026
 */

package me.elijah.more_shearable_mobs.client.renderer;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CowRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariants;

@Environment(EnvType.CLIENT)
public class ShearableCowEntityRenderer extends CowRenderer {

    // Skinned textures
    private static final Identifier SHEARED_TEXTURE_TEMP =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/cow/shearedcow_temperate.png");

    private static final Identifier SHEARED_TEXTURE_WARM =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/cow/shearedcow_warm.png");

    private static final Identifier SHEARED_TEXTURE_COLD =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/cow/shearedcow_cold.png");

    // Butchered textures
    private static final Identifier BUTCHERED_TEXTURE_TEMP =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/cow/skelecow_temperate.png");

    private static final Identifier BUTCHERED_TEXTURE_WARM =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/cow/skelecow_warm.png");

    private static final Identifier BUTCHERED_TEXTURE_COLD =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/cow/skelecow_cold.png");

    /**
     * Constructor
     *
     * @param context Renderer Context
     */
    public ShearableCowEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Subclass that stores the shear states safely for the render thread
     */
    public static class ShearableCowRenderState extends CowRenderState {
        public boolean isSheared;
        public boolean isButchered;
        public boolean isWarm;
        public boolean isCold;
    }

    /**
     * @return Custom Cow render state
     */
    @Override
    public CowRenderState createRenderState() {
        return new ShearableCowRenderState();
    }

    /**
     * Extracts variables from the cow entity safely into the render state
     *
     * @param cow       This cow
     * @param state     The cow's state
     * @param tickDelta Tick number
     */
    @Override
    public void extractRenderState(Cow cow, CowRenderState state, float tickDelta) {
        super.extractRenderState(cow, state, tickDelta);
        ShearableCowRenderState shearableState = (ShearableCowRenderState) state;
        shearableState.isSheared = cow.getEntityData().get(IS_COW_SHEARED);
        shearableState.isButchered = cow.getEntityData().get(IS_COW_BUTCHERED);
        shearableState.isWarm = cow.getVariant().is(CowVariants.WARM);
        shearableState.isCold = cow.getVariant().is(CowVariants.COLD);
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the cow's shear-state
     */
    @Override
    public Identifier getTextureLocation(CowRenderState state) {
        ShearableCowRenderState shearableState = (ShearableCowRenderState) state;
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