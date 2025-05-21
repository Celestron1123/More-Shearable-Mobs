/**
 * Custom data trackers for cows
 *
 * @author Elijah Potter
 * @date 5/19/2025
 */

package me.elijah.shearable_cows;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.CowEntity;

public class CowDataTrackers {

    /**
     * Boolean tracker for whether a cow has had its skin removed
     */
    public static final TrackedData<Boolean> IS_SHEARED =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    /**
     * Boolean tracker for whether a cow has had its flesh removed
     */
    public static final TrackedData<Boolean> IS_BUTCHERED =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    /**
     * Integer timer for how long a cow has left to regrow its skin
     */
    public static final TrackedData<Integer> REGROW_TIMER =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.INTEGER);

    /**
     * Integer timer for how long a cow has left to regrow its flesh
     */
    public static final TrackedData<Integer> REGEN_TIMER =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.INTEGER);
}
