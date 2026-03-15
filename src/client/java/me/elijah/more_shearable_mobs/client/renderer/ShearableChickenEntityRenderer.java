/**
 * Chicken renderer for shearable chickens
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
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.client.render.entity.model.ColdChickenEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.ChickenEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.ChickenVariant;
import net.minecraft.entity.passive.ChickenVariants;
import net.minecraft.util.Identifier;

import net.minecraft.client.render.entity.ChickenEntityRenderer;
import net.minecraft.util.math.MathHelper;

import java.util.Map;


@Environment(EnvType.CLIENT)
public class ShearableChickenEntityRenderer extends MobEntityRenderer<ChickenEntity, ChickenEntityRenderState, ChickenEntityModel> {
    private final Map<ChickenVariant.Model, BabyModelPair<ChickenEntityModel>> babyModelPairMap;

    // Skinned textures
    private static final Identifier SHEARED_TEXTURE_TEMP =
            Identifier.of("more_shearable_mobs", "textures/entity/chicken/shearedchicken_temp.png");

    private static final Identifier SHEARED_TEXTURE_COLD =
            Identifier.of("more_shearable_mobs", "textures/entity/chicken/shearedchicken_cold.png");

    private static final Identifier SHEARED_TEXTURE_WARM =
            Identifier.of("more_shearable_mobs", "textures/entity/chicken/shearedchicken_warm.png");

    // Butchered textures
    private static final Identifier BUTCHERED_TEXTURE_TEMP =
            Identifier.of("more_shearable_mobs", "textures/entity/chicken/skelken_temp.png");

    private static final Identifier BUTCHERED_TEXTURE_COLD =
            Identifier.of("more_shearable_mobs", "textures/entity/chicken/skelken_cold.png");

    private static final Identifier BUTCHERED_TEXTURE_WARM =
            Identifier.of("more_shearable_mobs", "textures/entity/chicken/skelken_warm.png");

    /**
     * Constructor
     *
     * @param context Yeah
     */
    public ShearableChickenEntityRenderer(EntityRendererFactory.Context context) {
        super(
                context,
                new ChickenEntityModel(context.getPart(EntityModelLayers.CHICKEN)),
                0.3F
        );
        this.babyModelPairMap = createBabyModelPairMap(context);
    }

    private static Map<ChickenVariant.Model, BabyModelPair<ChickenEntityModel>> createBabyModelPairMap(EntityRendererFactory.Context context) {
        return Maps.newEnumMap(Map.of(
                ChickenVariant.Model.NORMAL, new BabyModelPair<>(new ChickenEntityModel(context.getPart(EntityModelLayers.CHICKEN)), new ChickenEntityModel(context.getPart(EntityModelLayers.CHICKEN_BABY))),
                ChickenVariant.Model.COLD, new BabyModelPair<>(new ColdChickenEntityModel(context.getPart(EntityModelLayers.COLD_CHICKEN)), new ColdChickenEntityModel(context.getPart(EntityModelLayers.COLD_CHICKEN_BABY)))));
    }

    public void render(ChickenEntityRenderState chickenEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        if (chickenEntityRenderState.variant != null) {
            this.model = (ChickenEntityModel) ((BabyModelPair) this.babyModelPairMap.get(chickenEntityRenderState.variant.modelAndTexture().model())).get(chickenEntityRenderState.baby);
            super.render(chickenEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
        }
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
        state.flapProgress = MathHelper.lerp(tickDelta, chicken.lastFlapProgress, chicken.flapProgress);
        state.maxWingDeviation = MathHelper.lerp(tickDelta, chicken.lastMaxWingDeviation, chicken.maxWingDeviation);
        state.variant = chicken.getVariant().value();
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
            if (chicken.getVariant().getKey().orElseThrow().equals(ChickenVariants.WARM)) {
                return BUTCHERED_TEXTURE_WARM;
            } else if (chicken.getVariant().getKey().orElseThrow().equals(ChickenVariants.COLD)) {
                return BUTCHERED_TEXTURE_COLD;
            } else {
                return BUTCHERED_TEXTURE_TEMP;
            }
        } else if (chicken.getDataTracker().get(IS_CHICK_SHEARED)) {
            if (chicken.getVariant().getKey().orElseThrow().equals(ChickenVariants.WARM)) {
                return SHEARED_TEXTURE_WARM;
            } else if (chicken.getVariant().getKey().orElseThrow().equals(ChickenVariants.COLD)) {
                return SHEARED_TEXTURE_COLD;
            } else {
                return SHEARED_TEXTURE_TEMP;
            }
        }
        return state.variant == null ? MissingSprite.getMissingSpriteId() : state.variant.modelAndTexture().asset().texturePath();
    }
}