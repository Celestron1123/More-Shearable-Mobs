/**
 * Main client page for shearable cows
 *
 * @author Elijah Potter
 * @date 5/19/2025
 */

package me.elijah.shearable_cows.client;

import me.elijah.shearable_cows.client.renderer.ShearableCowEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.entity.EntityType;

public class Shearable_cowsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(EntityType.COW, ShearableCowEntityRenderer::new);
    }
}
