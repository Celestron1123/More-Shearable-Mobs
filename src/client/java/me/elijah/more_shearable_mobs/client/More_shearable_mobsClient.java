/**
 * Main client page for shearable mobs
 *
 * @author Elijah Potter
 * @date 5/23/2025
 */

package me.elijah.more_shearable_mobs.client;

import me.elijah.more_shearable_mobs.client.renderer.ShearableChickenEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.entity.EntityType;
import me.elijah.more_shearable_mobs.client.renderer.ShearableCowEntityRenderer;
import me.elijah.more_shearable_mobs.client.renderer.ShearablePigEntityRenderer;
import me.elijah.more_shearable_mobs.client.renderer.ShearableSheepEntityRenderer;

public class More_shearable_mobsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntityType.COW, ShearableCowEntityRenderer::new);
        EntityRendererRegistry.register(EntityType.CHICKEN, ShearableChickenEntityRenderer::new);
        EntityRendererRegistry.register(EntityType.PIG, ShearablePigEntityRenderer::new);
        EntityRendererRegistry.register(EntityType.SHEEP, ShearableSheepEntityRenderer::new);
    }
}
