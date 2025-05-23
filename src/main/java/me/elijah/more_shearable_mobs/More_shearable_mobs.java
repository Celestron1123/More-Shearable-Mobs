/**
 * Main initializer page for shearable mobs
 *
 * @author Elijah Potter
 * @date 5/23/2025
 */

package me.elijah.more_shearable_mobs;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class More_shearable_mobs implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("more_shearable_mobs");

    @Override
    public void onInitialize() {
        Class<?> c = ShearDataTrackers.class;
        LOGGER.info("Initializing More_Shearable_Mobs");
        LOGGER.info("ShearDataTrackers loaded: {}", c.getName());
        LOGGER.info("Shearable cows tracker slot: {}", ShearDataTrackers.IS_COW_SHEARED);
    }
}
