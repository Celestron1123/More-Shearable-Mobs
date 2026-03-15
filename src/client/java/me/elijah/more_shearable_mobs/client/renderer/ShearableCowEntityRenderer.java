/**
 * Cow renderer for shearable cows
 *
 * @auther Elijah Potter
 * @date 6/11/2025
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
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.ChickenEntityRenderState;
import net.minecraft.client.render.entity.state.CowEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.CowVariant;
import net.minecraft.entity.passive.CowVariants;
import net.minecraft.util.Identifier;

import net.minecraft.client.render.entity.CowEntityRenderer;

import java.util.Map;


@Environment(EnvType.CLIENT)
public class ShearableCowEntityRenderer extends MobEntityRenderer<CowEntity, CowEntityRenderState, CowEntityModel> {
    private final Map<CowVariant.Model, BabyModelPair<CowEntityModel>> babyModelPairMap;

    // Skinned textures
    private static final Identifier SHEARED_TEXTURE_TEMP =
            Identifier.of("more_shearable_mobs", "textures/entity/cow/shearedcow_temperate.png");

    private static final Identifier SHEARED_TEXTURE_WARM =
            Identifier.of("more_shearable_mobs", "textures/entity/cow/shearedcow_warm.png");

    private static final Identifier SHEARED_TEXTURE_COLD =
            Identifier.of("more_shearable_mobs", "textures/entity/cow/shearedcow_cold.png");

    // Butchered textures
    private static final Identifier BUTCHERED_TEXTURE_TEMP =
            Identifier.of("more_shearable_mobs", "textures/entity/cow/skelecow_temperate.png");

    private static final Identifier BUTCHERED_TEXTURE_WARM =
            Identifier.of("more_shearable_mobs", "textures/entity/cow/skelecow_warm.png");

    private static final Identifier BUTCHERED_TEXTURE_COLD =
            Identifier.of("more_shearable_mobs", "textures/entity/cow/skelecow_cold.png");

    /**
     * Constructor
     *
     * @param context Yeah
     */
    public ShearableCowEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new CowEntityModel(context.getPart(EntityModelLayers.COW)), 0.7F);
        this.babyModelPairMap = createBabyModelPairMap(context);
    }

    private static Map<CowVariant.Model, BabyModelPair<CowEntityModel>> createBabyModelPairMap(EntityRendererFactory.Context context) {
        return Maps.newEnumMap(Map.of(
                CowVariant.Model.NORMAL, new BabyModelPair<>(new CowEntityModel(context.getPart(EntityModelLayers.COW)), new CowEntityModel(context.getPart(EntityModelLayers.COW_BABY))),
                CowVariant.Model.WARM, new BabyModelPair<>(new CowEntityModel(context.getPart(EntityModelLayers.WARM_COW)), new CowEntityModel(context.getPart(EntityModelLayers.WARM_COW_BABY))),
                CowVariant.Model.COLD, new BabyModelPair<>(new CowEntityModel(context.getPart(EntityModelLayers.COLD_COW)), new CowEntityModel(context.getPart(EntityModelLayers.COLD_COW_BABY)))));
    }

    /**
     * Subclass that stores the cow entity
     */
    public static class ShearableCowRenderState extends CowEntityRenderState {
        public CowEntity cowEntity;
    }

    /**
     * @return Cow render state
     */
    @Override
    public CowEntityRenderState createRenderState() {
        return new ShearableCowRenderState();
    }

    /**
     * Updates the cow's render state
     *
     * @param cow       This cow
     * @param state     The cow's state
     * @param tickDelta Tick number
     */
    @Override
    public void updateRenderState(CowEntity cow, CowEntityRenderState state, float tickDelta) {
        super.updateRenderState(cow, state, tickDelta);
        state.variant = cow.getVariant().value();
        ((ShearableCowRenderState) state).cowEntity = cow;
    }

    /**
     * Retrieves the correct texture
     *
     * @param state Current render state
     * @return Texture, depending on the cow's shear-state
     */
    @Override
    public Identifier getTexture(CowEntityRenderState state) {
        CowEntity cow = ((ShearableCowRenderState) state).cowEntity;
        if (!cow.isBaby() && cow.getDataTracker().get(IS_COW_BUTCHERED)) {
            if (cow.getVariant().getKey().orElseThrow().equals(CowVariants.WARM)) {
                return BUTCHERED_TEXTURE_WARM;
            } else if (cow.getVariant().getKey().orElseThrow().equals(CowVariants.COLD)) {
                return BUTCHERED_TEXTURE_COLD;
            } else {
                return BUTCHERED_TEXTURE_TEMP;
            }
        } else if (cow.getDataTracker().get(IS_COW_SHEARED)) {
            if (cow.getVariant().getKey().orElseThrow().equals(CowVariants.WARM)) {
                return SHEARED_TEXTURE_WARM;
            } else if (cow.getVariant().getKey().orElseThrow().equals(CowVariants.COLD)) {
                return SHEARED_TEXTURE_COLD;
            } else {
                return SHEARED_TEXTURE_TEMP;
            }
        }
        return state.variant == null ? MissingSprite.getMissingSpriteId() : state.variant.modelAndTexture().asset().texturePath();
    }

    public void render(CowEntityRenderState cowEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState) {
        if (cowEntityRenderState.variant != null) {
            this.model = (CowEntityModel) ((BabyModelPair) this.babyModelPairMap.get(cowEntityRenderState.variant.modelAndTexture().model())).get(cowEntityRenderState.baby);
            super.render(cowEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
        }
    }
}

