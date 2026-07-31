/**
 * Because the other one sucked
 *
 * @author Elijah Potter
 * @date 10/10/2025
 */

package me.elijah.more_shearable_mobs.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.animal.sheep.SheepModel;
import net.minecraft.client.model.animal.sheep.SheepFurModel;
import net.minecraft.client.renderer.entity.state.SheepRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.Identifier;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.IS_SHEEP_BUTCHERED;

@Environment(EnvType.CLIENT)
public class BetterUndercoatRenderer extends RenderLayer<SheepRenderState, SheepModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/sheep/sheep_wool_undercoat.png");
    private final EntityModel<SheepRenderState> model;
    private final EntityModel<SheepRenderState> babyModel;

    public BetterUndercoatRenderer(RenderLayerParent<SheepRenderState, SheepModel> context, EntityModelSet loader) {
        super(context);
        this.model = new SheepFurModel(loader.bakeLayer(ModelLayers.SHEEP_WOOL_UNDERCOAT));
        this.babyModel = new SheepFurModel(loader.bakeLayer(ModelLayers.SHEEP_BABY_WOOL));
    }

    @Override
    public void submit(PoseStack matrices, SubmitNodeCollector queue, int light, SheepRenderState state, float limbAngle, float limbDistance) {
        ShearableSheepEntityRenderer.ShearableSheepRenderState shearableState =
                (ShearableSheepEntityRenderer.ShearableSheepRenderState) state;

        if (shearableState.isButchered) {
            return;
        }

        //TODO: test having a sheep named Jeb_
        if (!state.isInvisible && (state.isJebSheep || state.woolColor != DyeColor.WHITE)) {
            EntityModel<SheepRenderState> entityModel = state.isBaby ? this.babyModel : this.model;
            coloredCutoutModelCopyLayerRender(entityModel, TEXTURE, matrices, queue, light, state, state.getWoolColor(), 0);
        }
    }
}
