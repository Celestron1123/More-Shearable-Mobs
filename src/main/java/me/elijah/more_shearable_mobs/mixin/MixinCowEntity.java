/**
 * Main Mixin methods for cow-shearing logic
 *
 * @author Elijah Potter
 * @date 03/25/2026
 */

package me.elijah.more_shearable_mobs.mixin;

import me.elijah.more_shearable_mobs.More_shearable_mobs;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

@Mixin(Cow.class)
public class MixinCowEntity {

    /**
     * Shortcut method for referencing this cow
     *
     * @return This cow
     */
    @Unique
    private Cow thisCow() {
        return (Cow) (Object) this;
    }

    /**
     * Adds custom data trackers to cows
     *
     * @param builder Data tracker
     * @param ci      Unused
     */
    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = false)
    private void injectCowShearedTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(IS_COW_SHEARED, false);
        builder.define(IS_COW_BUTCHERED, false);
        builder.define(REGROW_COW_TIMER, 0);
        builder.define(REGEN_COW_TIMER, 0);
    }

    /**
     * Writes custom NBT data about cows and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void onWriteCustomData(ValueOutput nbt, CallbackInfo ci) {
        try {
            nbt.putBoolean("IsCowSheared", thisCow().getEntityData().get(IS_COW_SHEARED));
            nbt.putBoolean("IsCowButchered", thisCow().getEntityData().get(IS_COW_BUTCHERED));
            nbt.putInt("RegrowCowTicks", thisCow().getEntityData().get(REGROW_COW_TIMER));
            nbt.putInt("RegenCowTicks", thisCow().getEntityData().get(REGEN_COW_TIMER));
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
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void onReadNbt(ValueInput nbt, CallbackInfo ci) {
        try {
            thisCow().getEntityData().set(IS_COW_SHEARED, nbt.getBooleanOr("IsCowSheared", false));
            thisCow().getEntityData().set(IS_COW_BUTCHERED, nbt.getBooleanOr("IsCowButchered", false));
            thisCow().getEntityData().set(REGROW_COW_TIMER, nbt.getIntOr("RegrowCowTicks", 0));
            thisCow().getEntityData().set(REGEN_COW_TIMER, nbt.getIntOr("RegenCowTicks", 0));
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to read cow NBT", e);
        }
    }
}
