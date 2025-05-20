/**
 * Main initializer page for shearable cows
 *
 * @author Elijah Potter
 * @date 5/19/2025
 */

package me.elijah.shearable_cows;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shearable_cows implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("shearable_cows");

    @Override
    public void onInitialize() {
        Class<?> c = CowDataTrackers.class;
        LOGGER.info("CowDataTrackers loaded: {}", c.getName());
        LOGGER.info("Shearable cows tracker slot: {}", CowDataTrackers.IS_SHEARED);
    }
}
