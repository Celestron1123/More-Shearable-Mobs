/**
 * Mixin methods for passive entities, utilizing methods not
 * available to the cow class in particular.
 *
 * @author Elijah Potter
 * @date 5/19/2025
 */

package me.elijah.shearable_cows.mixin;

import me.elijah.shearable_cows.CowDataTrackers;
import me.elijah.shearable_cows.Shearable_cows;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.nbt.NbtCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.elijah.shearable_cows.CowDataTrackers.IS_SHEARED;
import static me.elijah.shearable_cows.CowDataTrackers.REGROW_TIMER;
import static me.elijah.shearable_cows.CowDataTrackers.IS_BUTCHERED;

@Mixin(LivingEntity.class)
public class MixinPassiveEntity {

    /**
     * Adds custom data trackers to cows
     *
     * @param builder Data tracker
     * @param ci      Unused
     */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectCowShearedTracker(DataTracker.Builder builder, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity) {
            builder.add(IS_SHEARED, false);
            builder.add(IS_BUTCHERED, false);
            builder.add(REGROW_TIMER, 0);
        }
    }

    /**
     * Every tick, the cow's timer is checked and decremented
     * until 0 to restore the skin.
     *
     * @param ci Unused
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if ((Object) this instanceof CowEntity cow) {
            if (cow.getDataTracker().get(IS_BUTCHERED)) {
                //Do something
            } else if (cow.getDataTracker().get(IS_SHEARED)) {
                int ticks = cow.getDataTracker().get(REGROW_TIMER);
                if (ticks > 0) {
                    cow.getDataTracker().set(REGROW_TIMER, ticks - 1);
                } else {
                    cow.getDataTracker().set(IS_SHEARED, false);
                }
            }
        }
    }

    /**
     * Writes custom NBT data about cows and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        try {
            if ((Object) this instanceof CowEntity cow) {
                nbt.putBoolean("IsSheared", cow.getDataTracker().get(CowDataTrackers.IS_SHEARED));
                nbt.putBoolean("IsButchered", cow.getDataTracker().get(IS_BUTCHERED));
                nbt.putInt("RegrowTicks", cow.getDataTracker().get(CowDataTrackers.REGROW_TIMER));
            }
        } catch (Exception e) {
            Shearable_cows.LOGGER.error("Failed to write cow NBT", e);
        }
    }

    /**
     * Reads custom NBT data about cows and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        try {
            if ((Object) this instanceof CowEntity cow) {
                if (nbt.contains("IsSheared")) {
                    cow.getDataTracker().set(CowDataTrackers.IS_SHEARED, nbt.getBoolean("IsSheared"));
                }
                if (nbt.contains("IsButchered")) {
                    cow.getDataTracker().set(IS_BUTCHERED, nbt.getBoolean("IsButchered"));
                }
                if (nbt.contains("RegrowTicks")) {
                    cow.getDataTracker().set(CowDataTrackers.REGROW_TIMER, nbt.getInt("RegrowTicks"));
                }
            }
        } catch (Exception e) {
            Shearable_cows.LOGGER.error("Failed to read cow NBT", e);
        }
    }
}
