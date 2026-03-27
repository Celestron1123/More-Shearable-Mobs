/**
 * Pig renderer for shearable pigs
 *
 * @auther Elijah Potter
 * @date 03/26/2026
 */

package me.elijah.more_shearable_mobs.client.renderer;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.state.PigRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.pig.Pig;

@Environment(EnvType.CLIENT)
public class ShearablePigEntityRenderer extends PigRenderer {

    // Butchered texture
    private static final Identifier SHEARED_TEXTURE =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/pig/skelepig.png");

    /**
     * Constructor
     *
     * @param context Renderer Context
     */
    public ShearablePigEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Subclass that stores the shear states safely for the render thread
     */
    public static class ShearablePigRenderState extends PigRenderState {
        public boolean isButchered;
    }

    /**
     * @return Custom Pig render state
     */
    @Override
    public PigRenderState createRenderState() {
        return new ShearablePigRenderState();
    }

    /**
     * Extracts variables from the pig entity safely into the render state
     *
     * @param pig       This pig
     * @param state     The pig's state
     * @param tickDelta Tick number
     */
    @Override
    public void extractRenderState(Pig pig, PigRenderState state, float tickDelta) {
        super.extractRenderState(pig, state, tickDelta);
        ShearablePigRenderState shearableState = (ShearablePigRenderState) state;
        shearableState.isButchered = pig.getEntityData().get(IS_PIG_BUTCHERED);
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the pig's shear-state
     */
    @Override
    public Identifier getTextureLocation(PigRenderState state) {
        ShearablePigRenderState shearableState = (ShearablePigRenderState) state;
        if (!shearableState.isBaby && shearableState.isButchered) {
            return SHEARED_TEXTURE;
        }
        return super.getTextureLocation(state);
    }
}