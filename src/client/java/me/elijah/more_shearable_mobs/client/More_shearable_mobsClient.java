/**
 * Main client page for shearable mobs
 *
 * @author Elijah Potter
 * @date 5/23/2025
 */

package me.elijah.more_shearable_mobs.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.entity.EntityType;
import me.elijah.more_shearable_mobs.client.renderer.ShearableCowEntityRenderer;

public class More_shearable_mobsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntityType.COW, ShearableCowEntityRenderer::new);
    }
}
