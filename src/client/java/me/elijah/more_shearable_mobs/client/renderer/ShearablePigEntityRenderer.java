/**
 * Pig renderer for shearable pigs
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
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.render.entity.state.SaddleableRenderState;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.Identifier;

import net.minecraft.client.render.entity.PigEntityRenderer;

@Environment(EnvType.CLIENT)
public class ShearablePigEntityRenderer extends AgeableMobEntityRenderer<PigEntity, LivingEntityRenderState, PigEntityModel> {

    // Normal texture
    private static final Identifier NORMAL_TEXTURE =
            Identifier.of("minecraft", "textures/entity/pig/pig.png");

    // Butchered texture
    private static final Identifier SHEARED_TEXTURE =
            Identifier.of("more_shearable_mobs", "textures/entity/pig/skelepig.png");

    /**
     * Constructor
     *
     * @param context Yeah
     */
    public ShearablePigEntityRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new PigEntityModel(context.getPart(EntityModelLayers.PIG)),
                new PigEntityModel(context.getPart(EntityModelLayers.PIG_BABY)),
                0.4F
        );
        this.addFeature(new SaddleFeatureRenderer(this, new PigEntityModel(context.getPart(EntityModelLayers.PIG_SADDLE)), new PigEntityModel(context.getPart(EntityModelLayers.PIG_BABY_SADDLE)), Identifier.ofVanilla("textures/entity/pig/pig_saddle.png")));
    }

    /**
     * Subclass that stores the pig entity
     */
    public static class ShearablePigRenderState extends LivingEntityRenderState implements SaddleableRenderState {
        public PigEntity pigEntity;
        public boolean saddled;

        public boolean isSaddled() {
            return saddled;
        }
    }

    /**
     * @return Pig render state
     */
    @Override
    public LivingEntityRenderState createRenderState() {
        return new ShearablePigRenderState();
    }

    /**
     * Updates the pig's render state
     *
     * @param pig       This pig
     * @param state     The pig's state
     * @param tickDelta Tick number
     */
    @Override
    public void updateRenderState(PigEntity pig, LivingEntityRenderState state, float tickDelta) {
        super.updateRenderState(pig, state, tickDelta);
        ((ShearablePigRenderState) state).saddled = pig.isSaddled();
        ((ShearablePigRenderState) state).pigEntity = pig;
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the pig's shear-state
     */
    @Override
    public Identifier getTexture(LivingEntityRenderState state) {
        PigEntity pig = ((ShearablePigRenderState) state).pigEntity;
        if (pig.getDataTracker().get(IS_PIG_SHEARED)) {
            return SHEARED_TEXTURE;
        }
        return NORMAL_TEXTURE;
    }
}

