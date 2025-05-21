/**
 * Cow renderer for shearable cows
 *
 * @auther Elijah Potter
 * @date 5/19/2025
 */

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

    // Normal texture
    private static final Identifier NORMAL_TEXTURE =
            Identifier.of("minecraft", "textures/entity/cow/cow.png");

    // Skinned texture
    private static final Identifier SHEARED_TEXTURE =
            Identifier.of("shearable_cows", "textures/entity/cow/sheared_cow.png");

    // Butchered texture
    private static final Identifier BUTCHERED_TEXTURE =
            Identifier.of("shearable_cows", "textures/entity/cow/skelecow.png");

    /**
     * Constructor
     *
     * @param context Yeah
     */
    public ShearableCowEntityRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new CowEntityModel(context.getPart(EntityModelLayers.COW)),
                new CowEntityModel(context.getPart(EntityModelLayers.COW_BABY)),
                0.7F
        );
    }

    /**
     * Subclass that stores the cow entity
     */
    public static class ShearableCowRenderState extends LivingEntityRenderState {
        public CowEntity cowEntity;
    }

    /**
     * @return Cow render state
     */
    @Override
    public LivingEntityRenderState createRenderState() {
        return new ShearableCowRenderState();
    }

    /**
     * Updates the cow's render state
     *
     * @param cow This cow
     * @param state The cow's state
     * @param tickDelta Tick number
     */
    @Override
    public void updateRenderState(CowEntity cow, LivingEntityRenderState state, float tickDelta) {
        super.updateRenderState(cow, state, tickDelta);
        ((ShearableCowRenderState) state).cowEntity = cow;
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the cow's shear-state
     */
    @Override
    public Identifier getTexture(LivingEntityRenderState state) {
        CowEntity cow = ((ShearableCowRenderState) state).cowEntity;
        if (!cow.isBaby() && cow.getDataTracker().get(CowDataTrackers.IS_BUTCHERED)) {
            return BUTCHERED_TEXTURE;
        } else if (cow.getDataTracker().get(CowDataTrackers.IS_SHEARED)) {
            return SHEARED_TEXTURE;
        }
        return NORMAL_TEXTURE;
    }
}
