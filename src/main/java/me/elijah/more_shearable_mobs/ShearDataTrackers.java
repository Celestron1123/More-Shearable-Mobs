/**
 * Custom data trackers for mobs
 *
 * @author Elijah Potter
 * @date 3/25/2026
 */

package me.elijah.more_shearable_mobs;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;

public class ShearDataTrackers {

    // ---------------- Cow Data Trackers ------------------------

    /**
     * Boolean tracker for whether a cow has had its skin removed
     */
    public static final EntityDataAccessor<Boolean> IS_COW_SHEARED =
            SynchedEntityData.defineId(Cow.class, EntityDataSerializers.BOOLEAN);

    /**
     * Boolean tracker for whether a cow has had its flesh removed
     */
    public static final EntityDataAccessor<Boolean> IS_COW_BUTCHERED =
            SynchedEntityData.defineId(Cow.class, EntityDataSerializers.BOOLEAN);

    /**
     * Integer timer for how long a cow has left to regrow its skin
     */
    public static final EntityDataAccessor<Integer> REGROW_COW_TIMER =
            SynchedEntityData.defineId(Cow.class, EntityDataSerializers.INT);

    /**
     * Integer timer for how long a cow has left to regrow its flesh
     */
    public static final EntityDataAccessor<Integer> REGEN_COW_TIMER =
            SynchedEntityData.defineId(Cow.class, EntityDataSerializers.INT);

    // ---------------- Chicken Data Trackers ------------------------

    /**
     * Boolean tracker for whether a chicken has had its skin removed
     */
    public static final EntityDataAccessor<Boolean> IS_CHICK_SHEARED =
            SynchedEntityData.defineId(Chicken.class, EntityDataSerializers.BOOLEAN);

    /**
     * Boolean tracker for whether a chicken has had its flesh removed
     */
    public static final EntityDataAccessor<Boolean> IS_CHICK_BUTCHERED =
            SynchedEntityData.defineId(Chicken.class, EntityDataSerializers.BOOLEAN);

    /**
     * Integer timer for how long a chicken has left to regrow its skin
     */
    public static final EntityDataAccessor<Integer> REGROW_CHICK_TIMER =
            SynchedEntityData.defineId(Chicken.class, EntityDataSerializers.INT);

    /**
     * Integer timer for how long a chicken has left to regrow its flesh
     */
    public static final EntityDataAccessor<Integer> REGEN_CHICK_TIMER =
            SynchedEntityData.defineId(Chicken.class, EntityDataSerializers.INT);

    // ---------------- Pig Data Trackers ------------------------

    /**
     * Boolean tracker for whether a pig has had its flesh removed
     */
    public static final EntityDataAccessor<Boolean> IS_PIG_BUTCHERED =
            SynchedEntityData.defineId(Pig.class, EntityDataSerializers.BOOLEAN);


    /**
     * Integer timer for how long a pig has left to regrow its flesh
     */
    public static final EntityDataAccessor<Integer> REGEN_PIG_TIMER =
            SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);

    // ---------------- Sheep Data Trackers ------------------------

    /**
     * Boolean tracker for whether a sheep has had its flesh removed
     */
    public static final EntityDataAccessor<Boolean> IS_SHEEP_BUTCHERED =
            SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BOOLEAN);

    /**
     * Integer timer for how long a pig has left to regrow its flesh
     */
    public static final EntityDataAccessor<Integer> REGEN_SHEEP_TIMER =
            SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.INT);
}
