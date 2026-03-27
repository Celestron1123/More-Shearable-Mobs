/**
 * Sheep renderer for shearable sheep
 *
 * @auther Elijah Potter
 * @date 03/26/2026
 */

package me.elijah.more_shearable_mobs.client.renderer;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.sheep.Sheep;

@Environment(EnvType.CLIENT)
public class ShearableSheepEntityRenderer extends SheepRenderer {

    // Butchered texture
    private static final Identifier BUTCHERED_TEXTURE =
            Identifier.fromNamespaceAndPath("more_shearable_mobs", "textures/entity/sheep/skeleshep_wooled.png");

    /**
     * Constructor
     *
     * @param context Yeah
     */
    public ShearableSheepEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * Subclass that stores the shear states safely for the render thread
     */
    public static class ShearableSheepRenderState extends SheepRenderState {
        public boolean isButchered;
    }

    /**
     * @return Custom Sheep render state
     */
    @Override
    public SheepRenderState createRenderState() {
        return new ShearableSheepRenderState();
    }

    /**
     * Extracts variables from the sheep entity safely into the render state
     *
     * @param sheep     This sheep
     * @param state     The sheep's state
     * @param tickDelta Tick number
     */
    @Override
    public void extractRenderState(Sheep sheep, SheepRenderState state, float tickDelta) {
        super.extractRenderState(sheep, state, tickDelta);
        ShearableSheepRenderState shearableState = (ShearableSheepRenderState) state;
        shearableState.isButchered = sheep.getEntityData().get(IS_SHEEP_BUTCHERED);
        if (shearableState.isButchered) {
            // Force the state to WHITE so the vanilla engine doesn't tint the skeleton
            // Unless it's jeb_ then it breaks everything UGGHHHHHH
            state.woolColor = net.minecraft.world.item.DyeColor.WHITE;
        }
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the sheep's shear-state
     */
    @Override
    public Identifier getTextureLocation(SheepRenderState state) {
        ShearableSheepRenderState shearableState = (ShearableSheepRenderState) state;
        if (!shearableState.isBaby && shearableState.isButchered) {
            return BUTCHERED_TEXTURE;
        }
        return super.getTextureLocation(state);
    }
}