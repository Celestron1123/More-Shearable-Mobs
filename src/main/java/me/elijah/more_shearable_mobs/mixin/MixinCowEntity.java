/**
 * Main Mixin methods for cow-shearing logic
 *
 * @author Elijah Potter
 * @date 10/10/2025
 */

package me.elijah.more_shearable_mobs.mixin;

import me.elijah.more_shearable_mobs.More_shearable_mobs;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

@Mixin(CowEntity.class)
public class MixinCowEntity {

    /**
     * Shortcut method for referencing this cow
     *
     * @return This cow
     */
    @Unique
    private CowEntity thisCow() {
        return (CowEntity) (Object) this;
    }

    /**
     * Adds custom data trackers to cows
     *
     * @param builder Data tracker
     * @param ci      Unused
     */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectCowShearedTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(IS_COW_SHEARED, false);
        builder.add(IS_COW_BUTCHERED, false);
        builder.add(REGROW_COW_TIMER, 0);
        builder.add(REGEN_COW_TIMER, 0);
    }

    /**
     * Writes custom NBT data about cows and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void onWriteCustomData(WriteView nbt, CallbackInfo ci) {
        try {
            nbt.putBoolean("IsCowSheared", thisCow().getDataTracker().get(IS_COW_SHEARED));
            nbt.putBoolean("IsCowButchered", thisCow().getDataTracker().get(IS_COW_BUTCHERED));
            nbt.putInt("RegrowCowTicks", thisCow().getDataTracker().get(REGROW_COW_TIMER));
            nbt.putInt("RegenCowTicks", thisCow().getDataTracker().get(REGEN_COW_TIMER));
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to write cow NBT", e);
        }
    }

    /**
     * Reads custom NBT data about cows and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void onReadNbt(ReadView nbt, CallbackInfo ci) {
        try {
            thisCow().getDataTracker().set(IS_COW_SHEARED, nbt.getBoolean("IsCowSheared", false));
            thisCow().getDataTracker().set(IS_COW_BUTCHERED, nbt.getBoolean("IsCowButchered", false));
            thisCow().getDataTracker().set(REGROW_COW_TIMER, nbt.getInt("RegrowCowTicks", 0));
            thisCow().getDataTracker().set(REGEN_COW_TIMER, nbt.getInt("RegenCowTicks", 0));
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to read cow NBT", e);
        }
    }
}
