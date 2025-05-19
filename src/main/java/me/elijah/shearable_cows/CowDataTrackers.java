package me.elijah.shearable_cows;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.CowEntity;

public class CowDataTrackers {
    public static final TrackedData<Boolean> IS_SHEARED =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    public static final TrackedData<Integer> REGROW_TIMER =
            DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.INTEGER);
}
