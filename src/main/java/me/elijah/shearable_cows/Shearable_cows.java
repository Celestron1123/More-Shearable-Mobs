package me.elijah.shearable_cows;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shearable_cows implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("shearable_cows");

    @Override
    public void onInitialize() {
        LOGGER.info("Cow mod activated. Hell yeah!");
    }
}
