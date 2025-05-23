/**
 * Custom data trackers for mobs
 *
 * @author Elijah Potter
 * @date 5/23/2025
 */

package me.elijah.more_shearable_mobs;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.CowEntity;

public class ShearDataTrackers {

    /**
     * Boolean tracker for whether a cow has had its skin removed
     */
    public static final TrackedData<Boolean> IS_COW_SHEARED =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    /**
     * Boolean tracker for whether a cow has had its flesh removed
     */
    public static final TrackedData<Boolean> IS_COW_BUTCHERED =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    /**
     * Integer timer for how long a cow has left to regrow its skin
     */
    public static final TrackedData<Integer> REGROW_COW_TIMER =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.INTEGER);

    /**
     * Integer timer for how long a cow has left to regrow its flesh
     */
    public static final TrackedData<Integer> REGEN_COW_TIMER =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.INTEGER);
}
