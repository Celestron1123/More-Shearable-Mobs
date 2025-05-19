package me.elijah.shearable_cows.mixin;

import me.elijah.shearable_cows.CowDataTrackers;
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

@Mixin(LivingEntity.class)
public class MixinPassiveEntity {

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectCowShearedTracker(DataTracker.Builder builder, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity) {
            builder.add(IS_SHEARED, false);
            builder.add(REGROW_TIMER, 0);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if ((Object) this instanceof CowEntity cow) {
            int ticks = cow.getDataTracker().get(REGROW_TIMER);
            if (ticks > 0) {
                cow.getDataTracker().set(REGROW_TIMER, ticks - 1);
            } else {
                cow.getDataTracker().set(IS_SHEARED, false);
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity cow) {
            nbt.putBoolean("IsSheared", cow.getDataTracker().get(CowDataTrackers.IS_SHEARED));
            nbt.putInt("RegrowTicks", cow.getDataTracker().get(CowDataTrackers.REGROW_TIMER));
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity cow) {
            if (nbt.contains("IsSheared")) {
                cow.getDataTracker().set(CowDataTrackers.IS_SHEARED, nbt.getBoolean("IsSheared"));
            }
            if (nbt.contains("RegrowTicks")) {
                cow.getDataTracker().set(CowDataTrackers.REGROW_TIMER, nbt.getInt("RegrowTicks"));
            }
        }
    }
}
