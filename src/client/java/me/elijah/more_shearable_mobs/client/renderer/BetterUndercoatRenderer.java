/**
 * Because the other one sucked
 *
 * @author Elijah Potter
 * @date 10/10/2025
 */

package me.elijah.more_shearable_mobs.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.client.render.entity.model.SheepWoolEntityModel;
import net.minecraft.client.render.entity.state.SheepEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.IS_SHEEP_BUTCHERED;

@Environment(EnvType.CLIENT)
public class BetterUndercoatRenderer extends FeatureRenderer<SheepEntityRenderState, SheepEntityModel> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/sheep/sheep_wool_undercoat.png");
    private final EntityModel<SheepEntityRenderState> model;
    private final EntityModel<SheepEntityRenderState> babyModel;

    public BetterUndercoatRenderer(FeatureRendererContext<SheepEntityRenderState, SheepEntityModel> context, LoadedEntityModels loader) {
        super(context);
        this.model = new SheepWoolEntityModel(loader.getModelPart(EntityModelLayers.SHEEP_WOOL_UNDERCOAT));
        this.babyModel = new SheepWoolEntityModel(loader.getModelPart(EntityModelLayers.SHEEP_BABY_WOOL_UNDERCOAT));
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, SheepEntityRenderState state, float limbAngle, float limbDistance) {
        SheepEntity sheep = ((ShearableSheepEntityRenderer.ShearableSheepRenderState) state).sheepEntity;

        if (sheep.getDataTracker().get(IS_SHEEP_BUTCHERED)) return;

        //TODO: test having a sheep named Jeb_
        if (!state.invisible && (state.rainbow || state.color != DyeColor.WHITE)) {
            EntityModel<SheepEntityRenderState> entityModel = state.baby ? this.babyModel : this.model;
            render(entityModel, TEXTURE, matrices, queue, light, state, state.getRgbColor(), 0);
        }
    }
}
