/**
 * Main Mixin methods for chicken-shearing logic. Some of them, anyway...
 *
 * @author Elijah Potter
 * @date 03/26/2026
 */

package me.elijah.more_shearable_mobs.mixin;

import me.elijah.more_shearable_mobs.More_shearable_mobs;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

@Mixin(Chicken.class)
public class MixinChickenEntity implements Shearable {

    /**
     * Shortcut method for referencing this chicken
     *
     * @return This chicken
     */
    @Unique
    private Chicken thisChicken() {
        return (Chicken) (Object) this;
    }

    /**
     * From Shearable interface
     *
     * @return Whether a chicken is in a shearable state
     */
    @Override
    public boolean readyForShearing() {
        if (thisChicken() != null)
            return thisChicken().isAlive() && !isSheared() && !thisChicken().isBaby();
        else return false;
    }

    /**
     * @return Whether a chicken is sheared
     */
    @Unique
    public boolean isSheared() {
        if (thisChicken() != null)
            return thisChicken().getEntityData().get(IS_CHICK_SHEARED);
        else return false;
    }

    /**
     * Sets the sheared state of a chicken, starts the shear countdown
     *
     * @param sheared What state the chicken should enter
     */
    @Unique
    public void setSheared(boolean sheared) {
        if (thisChicken() != null) {
            thisChicken().getEntityData().set(IS_CHICK_SHEARED, sheared);
            if (sheared) {
                thisChicken().getEntityData().set(REGROW_CHICK_TIMER, sampleRegrowTimer(thisChicken().getRandom()));
            }
        }
    }

    /**
     * This method simulates the same bell curve that sheep follow when determining
     * when their wool regenerates. Specifically: "The chance of eating grass is
     * 1⁄1000 per game tick, so a sheep is 63% likely to regrow new wool after 50
     * seconds, and over 90% likely to regrow new wool after 2 minutes."
     * Thus, the mean/expected value of the bell curve is 1000 ticks with a
     * standard deviation of 300 ticks.
     *
     * @param random Random number generator
     * @return an integer that follows the prescribed bell curve
     */
    @Unique
    private int sampleRegrowTimer(RandomSource random) {
        double mean = 1000;  // 50 seconds = 1000 ticks
        double stdDev = 300;
        double sample;
        do {
            sample = random.nextGaussian() * stdDev + mean;
        } while (sample < 200 || sample > 2400); // Clamp: 10s–120s

        return (int) sample;
    }

    /**
     * Method that performs the actual shearing to the chicken.
     *
     * @param world                The current world
     * @param shearedSoundCategory The sound of the shear
     * @param shears               The player's shears
     */
    @Override
    public void shear(ServerLevel world, @NotNull SoundSource shearedSoundCategory, @NotNull ItemStack shears) {
        world.playSound(null, thisChicken(), SoundEvents.SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
        setSheared(true);
        int dropCount = determineDropCount();
        ItemStack leatherStack = new ItemStack(Items.FEATHER, dropCount);
        ItemEntity drop = new ItemEntity(world, thisChicken().getX(), thisChicken().getY() + 1, thisChicken().getZ(), leatherStack);
        world.addFreshEntity(drop);
        drop.setDeltaMovement(drop.getDeltaMovement().add((thisChicken().getRandom().nextFloat() - thisChicken().getRandom().nextFloat()) * 0.1F, thisChicken().getRandom().nextFloat() * 0.05F, (thisChicken().getRandom().nextFloat() - thisChicken().getRandom().nextFloat()) * 0.1F));
    }

    /**
     * Determines how much leather/beef ought to be dropped
     *
     * @return integer of how much leather/beef to be dropped
     */
    @Unique
    private int determineDropCount() {
        float chance = thisChicken().getRandom().nextFloat();
        if (chance < 0.45f) {
            return 1;
        } else if (chance < 0.90f) {
            return 2;
        } else {
            return 3;
        }
    }

    // ================ Moved over from Living Entity Mixin :) ===================================

    /**
     * Adds custom data trackers to chickens
     *
     * @param builder Data tracker
     * @param ci      Unused
     */
    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = false)
    private void injectMobShearedTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(IS_CHICK_SHEARED, false);
        builder.define(IS_CHICK_BUTCHERED, false);
        builder.define(REGROW_CHICK_TIMER, 0);
        builder.define(REGEN_CHICK_TIMER, 0);
    }

    /**
     * Writes custom NBT data about chickens and their shear-states
     * to persist between loads
     *
     * @param output Data writer
     * @param ci     Unused
     */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void onWriteNbt(ValueOutput output, CallbackInfo ci) {
        try {
            output.putBoolean("IsChickSheared", thisChicken().getEntityData().get(IS_CHICK_SHEARED));
            output.putBoolean("IsChickButchered", thisChicken().getEntityData().get(IS_CHICK_BUTCHERED));
            output.putInt("RegrowChickTicks", thisChicken().getEntityData().get(REGROW_CHICK_TIMER));
            output.putInt("RegenChickTicks", thisChicken().getEntityData().get(REGEN_CHICK_TIMER));

        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to write chicken NBT", e);
        }
    }

    /**
     * Reads custom NBT data about chickens and their shear-states
     * to persist between loads
     *
     * @param input Data reader
     * @param ci    Unused
     */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void onReadNbt(ValueInput input, CallbackInfo ci) {
        try {
            thisChicken().getEntityData().set(IS_CHICK_SHEARED, input.getBooleanOr("IsChickSheared", false));
            thisChicken().getEntityData().set(IS_CHICK_BUTCHERED, input.getBooleanOr("IsChickButchered", false));
            thisChicken().getEntityData().set(REGROW_CHICK_TIMER, input.getIntOr("RegrowChickTicks", 0));
            thisChicken().getEntityData().set(REGEN_CHICK_TIMER, input.getIntOr("RegenChickTicks", 0));

        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to read chicken NBT", e);
        }
    }
}