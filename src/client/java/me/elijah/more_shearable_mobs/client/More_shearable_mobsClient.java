/**
 * Main client page for shearable mobs
 *
 * @author Elijah Potter
 * @date 03/26/2026
 */

package me.elijah.more_shearable_mobs.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import me.elijah.more_shearable_mobs.client.renderer.ShearableCowEntityRenderer;
import me.elijah.more_shearable_mobs.client.renderer.ShearablePigEntityRenderer;
import me.elijah.more_shearable_mobs.client.renderer.ShearableSheepEntityRenderer;
import me.elijah.more_shearable_mobs.client.renderer.ShearableChickenEntityRenderer;

public class More_shearable_mobsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRenderers.register(EntityType.COW, ShearableCowEntityRenderer::new);
        EntityRenderers.register(EntityType.CHICKEN, ShearableChickenEntityRenderer::new);
        EntityRenderers.register(EntityType.PIG, ShearablePigEntityRenderer::new);
        EntityRenderers.register(EntityType.SHEEP, ShearableSheepEntityRenderer::new);
    }
}
