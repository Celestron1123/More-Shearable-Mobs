/**
 * Main Mixin methods for pig-shearing logic
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

@Mixin(Pig.class)
public class MixinPigEntity implements Shearable {

    /**
     * Shortcut method for referencing this pig
     *
     * @return This pig
     */
    @Unique
    private Pig thisPig() {
        return (Pig) (Object) this;
    }

    /**
     * From Shearable interface
     *
     * @return Whether a pig is in a shearable state
     */
    @Override
    public boolean readyForShearing() {
        return thisPig().isAlive() && !isSheared() && !thisPig().isBaby();
    }

    /**
     * @return Whether a pig is sheared
     */
    @Unique
    public boolean isSheared() {
        return thisPig().getEntityData().get(IS_PIG_BUTCHERED);
    }

    /**
     * Sets the sheared state of a pig, starts the shear countdown
     *
     * @param sheared What state the pig should enter
     */
    @Unique
    public void setSheared(boolean sheared) {
        thisPig().getEntityData().set(IS_PIG_BUTCHERED, sheared);
        if (sheared) {
            thisPig().getEntityData().set(REGEN_PIG_TIMER, sampleRegrowTimer(thisPig().getRandom()));
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
     * Method that performs the actual shearing to the pig.
     *
     * @param world                The current world
     * @param shearedSoundCategory The sound of the shear
     * @param shears               The player's shears
     */
    @Override
    public void shear(ServerLevel world, @NotNull SoundSource shearedSoundCategory, @NotNull ItemStack shears) {
        world.playSound(null, thisPig(), SoundEvents.SLIME_SQUISH, shearedSoundCategory, 1.0F, 1.0F);
        setSheared(true);
        int dropCount = determineDropCount();
        ItemStack leatherStack = new ItemStack(Items.PORKCHOP, dropCount);
        ItemEntity drop = new ItemEntity(world, thisPig().getX(), thisPig().getY() + 1, thisPig().getZ(), leatherStack);
        world.addFreshEntity(drop);
        drop.setDeltaMovement(drop.getDeltaMovement().add((thisPig().getRandom().nextFloat() - thisPig().getRandom().nextFloat()) * 0.1F, thisPig().getRandom().nextFloat() * 0.05F, (thisPig().getRandom().nextFloat() - thisPig().getRandom().nextFloat()) * 0.1F));
    }

    /**
     * Determines how much leather/beef ought to be dropped
     *
     * @return integer of how much leather/beef to be dropped
     */
    @Unique
    private int determineDropCount() {
        float chance = thisPig().getRandom().nextFloat();
        if (chance < 0.45f) {
            return 1;
        } else if (chance < 0.90f) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Handles the logic for when a player interacts with a pig using shears
     *
     * @param player Current player
     * @param hand   The player's hand
     * @param cir    Reports success of method
     */
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true, remap = false)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.SHEARS)) {
            Level var5 = thisPig().level();
            if (var5 instanceof ServerLevel serverLevel) {
                if (this.readyForShearing()) {
                    this.shear(serverLevel, SoundSource.PLAYERS, itemStack);
                    itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    player.swing(hand, true);
                    cir.setReturnValue(InteractionResult.SUCCESS);
                }
            }
        }
    }

    /**
     * Adds custom data trackers to piggies hehe
     *
     * @param builder Data tracker
     * @param ci      Unused
     */
    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = false)
    private void injectCowShearedTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(IS_PIG_BUTCHERED, false);
        builder.define(REGEN_PIG_TIMER, 0);
    }

    /**
     * Writes custom NBT data about pigs and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void onWriteCustomData(ValueOutput nbt, CallbackInfo ci) {
        try {
            nbt.putBoolean("IsPigButchered", thisPig().getEntityData().get(IS_PIG_BUTCHERED));
            nbt.putInt("RegenPigTicks", thisPig().getEntityData().get(REGEN_PIG_TIMER));
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to write pig NBT", e);
        }
    }

    /**
     * Reads custom NBT data about pigs and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void onReadNbt(ValueInput nbt, CallbackInfo ci) {
        try {
            thisPig().getEntityData().set(IS_PIG_BUTCHERED, nbt.getBooleanOr("IsPigButchered", false));
            thisPig().getEntityData().set(REGEN_PIG_TIMER, nbt.getIntOr("RegenPigTicks", 0));
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to read pig NBT", e);
        }
    }
}
