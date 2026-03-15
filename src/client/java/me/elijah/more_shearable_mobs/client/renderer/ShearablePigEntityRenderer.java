/**
 * Pig renderer for shearable pigs
 *
 * @auther Elijah Potter
 * @date 10/10/2025
 */

package me.elijah.more_shearable_mobs.client.renderer;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BabyModelPair;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.render.entity.model.ColdPigEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import net.minecraft.client.render.entity.PigEntityRenderer;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class ShearablePigEntityRenderer extends MobEntityRenderer<PigEntity, PigEntityRenderState, PigEntityModel> {
    private final Map<PigVariant.Model, BabyModelPair<PigEntityModel>> modelPairs;

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
                0.7F
        );
        this.modelPairs = createModelPairs(context);
        this.addFeature(new SaddleFeatureRenderer(this, context.getEquipmentRenderer(), EquipmentModel.LayerType.PIG_SADDLE, (pigEntityRenderState) -> ((ShearablePigRenderState) pigEntityRenderState).saddleStack, new PigEntityModel(context.getPart(EntityModelLayers.PIG_SADDLE)), new PigEntityModel(context.getPart(EntityModelLayers.PIG_BABY_SADDLE))));
    }

    private static Map<PigVariant.Model, BabyModelPair<PigEntityModel>> createModelPairs(EntityRendererFactory.Context context) {
        return Maps.newEnumMap(Map.of(
                PigVariant.Model.NORMAL, new BabyModelPair<>(new PigEntityModel(context.getPart(EntityModelLayers.PIG)), new PigEntityModel(context.getPart(EntityModelLayers.PIG_BABY))),
                PigVariant.Model.COLD, new BabyModelPair<>(new ColdPigEntityModel(context.getPart(EntityModelLayers.COLD_PIG)), new ColdPigEntityModel(context.getPart(EntityModelLayers.COLD_PIG_BABY)))));
    }

    /**
     * Subclass that stores the pig entity
     */
    public static class ShearablePigRenderState extends PigEntityRenderState {
        public PigEntity pigEntity;
        public ItemStack saddleStack = ItemStack.EMPTY;
    }

    public void render(PigEntityRenderState pigEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        if (pigEntityRenderState.variant != null) {
            this.model = (PigEntityModel) ((BabyModelPair) this.modelPairs.get(pigEntityRenderState.variant.modelAndTexture().model())).get(pigEntityRenderState.baby);
            super.render(pigEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
        }
    }

    /**
     * @return Pig render state
     */
    @Override
    public PigEntityRenderState createRenderState() {
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
    public void updateRenderState(PigEntity pig, PigEntityRenderState state, float tickDelta) {
        super.updateRenderState(pig, state, tickDelta);
        ((ShearablePigRenderState) state).saddleStack = pig.getEquippedStack(EquipmentSlot.SADDLE).copy();
        state.variant = pig.getVariant().value();
        ((ShearablePigRenderState) state).pigEntity = pig;
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the pig's shear-state
     */
    @Override
    public Identifier getTexture(PigEntityRenderState state) {
        PigEntity pig = ((ShearablePigRenderState) state).pigEntity;
        if (pig.getDataTracker().get(IS_PIG_BUTCHERED)) {
            return SHEARED_TEXTURE;
        }
        return state.variant == null ? MissingSprite.getMissingSpriteId() : state.variant.modelAndTexture().asset().texturePath();
    }
}

