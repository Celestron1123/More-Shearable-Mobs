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
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;

public class ShearDataTrackers {

    // ---------------- Cow Data Trackers ------------------------

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

    // ---------------- Chicken Data Trackers ------------------------

    /**
     * Boolean tracker for whether a chicken has had its skin removed
     */
    public static final TrackedData<Boolean> IS_CHICK_SHEARED =
            DataTracker.registerData(ChickenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    /**
     * Boolean tracker for whether a chicken has had its flesh removed
     */
    public static final TrackedData<Boolean> IS_CHICK_BUTCHERED =
            DataTracker.registerData(ChickenEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    /**
     * Integer timer for how long a chicken has left to regrow its skin
     */
    public static final TrackedData<Integer> REGROW_CHICK_TIMER =
            DataTracker.registerData(ChickenEntity.class, TrackedDataHandlerRegistry.INTEGER);

    /**
     * Integer timer for how long a chicken has left to regrow its flesh
     */
    public static final TrackedData<Integer> REGEN_CHICK_TIMER =
            DataTracker.registerData(ChickenEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // ---------------- Pig Data Trackers ------------------------

    /**
     * Boolean tracker for whether a pig has had its flesh removed
     */
    public static final TrackedData<Boolean> IS_PIG_SHEARED =
            DataTracker.registerData(PigEntity.class, TrackedDataHandlerRegistry.BOOLEAN);


    /**
     * Integer timer for how long a pig has left to regrow its flesh
     */
    public static final TrackedData<Integer> REGROW_PIG_TIMER =
            DataTracker.registerData(PigEntity.class, TrackedDataHandlerRegistry.INTEGER);

    // ---------------- Sheep Data Trackers ------------------------

    /**
     * Boolean tracker for whether a sheep has had its flesh removed
     */
    public static final TrackedData<Boolean> IS_SHEEP_BUTCHERED =
            DataTracker.registerData(SheepEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    /**
     * Integer timer for how long a pig has left to regrow its flesh
     */
    public static final TrackedData<Integer> REGEN_SHEEP_TIMER =
            DataTracker.registerData(SheepEntity.class, TrackedDataHandlerRegistry.INTEGER);
}
