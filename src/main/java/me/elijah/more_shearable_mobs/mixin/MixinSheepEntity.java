/**
 * Main Mixin methods for sheep-butchering logic
 *
 * @author Elijah Potter
 * @date 5/23/2025
 */

package me.elijah.more_shearable_mobs.mixin;

import me.elijah.more_shearable_mobs.More_shearable_mobs;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;
import static net.minecraft.entity.LivingEntity.getSlotForHand;

@Mixin(SheepEntity.class)
public class MixinSheepEntity {

    /**
     * Shortcut method for referencing this sheep
     *
     * @return This sheep
     */
    @Unique
    private SheepEntity thisSheep() {
        return (SheepEntity) (Object) this;
    }

    /**
     * @return Whether a sheep is in a butcherable state
     */
    @Unique
    public boolean isButcherable() {
        return thisSheep().isAlive() && thisSheep().isSheared() && !isButchered() && !thisSheep().isBaby();
    }

    /**
     * @return Whether a sheep is butchered
     */
    @Unique
    public boolean isButchered() {
        return thisSheep().getDataTracker().get(IS_SHEEP_BUTCHERED);
    }

    /**
     * Sets the butcher state of a sheep
     *
     * @param butchered Whether the sheep is butchered
     */
    @Unique
    public void setButchered(boolean butchered) {
        thisSheep().getDataTracker().set(IS_SHEEP_BUTCHERED, butchered);
        if (butchered) {
            thisSheep().getDataTracker().set(REGEN_SHEEP_TIMER, sampleRegrowTimer(thisSheep().getRandom()));
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
    private int sampleRegrowTimer(Random random) {
        double mean = 1000;  // 50 seconds = 1000 ticks
        double stdDev = 300;
        double sample;
        do {
            sample = random.nextGaussian() * stdDev + mean;
        } while (sample < 200 || sample > 2400); // Clamp: 10s–120s

        return (int) sample;
    }

    /**
     * Method that performs the actual butchering to the sheep.
     *
     * @param world                The current world
     * @param shearedSoundCategory The sound of the shear
     */
    @Unique
    public void butchered(ServerWorld world, SoundCategory shearedSoundCategory) {
        world.playSoundFromEntity(null, thisSheep(), SoundEvents.ENTITY_SLIME_SQUISH, shearedSoundCategory, 1.0F, 1.0F);
        setButchered(true);
        int dropCount = determineDropCount();
        ItemStack beefStack = new ItemStack(Items.MUTTON, dropCount);
        ItemEntity drop = new ItemEntity(world, thisSheep().getX(), thisSheep().getY() + 1, thisSheep().getZ(), beefStack);
        world.spawnEntity(drop);
        drop.setVelocity(drop.getVelocity().add((thisSheep().getRandom().nextFloat() - thisSheep().getRandom().nextFloat()) * 0.1F, thisSheep().getRandom().nextFloat() * 0.05F, (thisSheep().getRandom().nextFloat() - thisSheep().getRandom().nextFloat()) * 0.1F));
    }

    /**
     * Determines how much leather/beef ought to be dropped
     *
     * @return integer of how much leather/beef to be dropped
     */
    @Unique
    private int determineDropCount() {
        float chance = thisSheep().getRandom().nextFloat();
        if (chance < 0.45f) {
            return 1;
        } else if (chance < 0.90f) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Handles the logic for when a player interacts with a sheep using shears
     *
     * @param player Current player
     * @param hand   The player's hand
     * @param cir    Reports success of method
     */
    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.SHEARS)) {
            World var5 = thisSheep().getWorld();
            if (var5 instanceof ServerWorld serverWorld) {
                if (this.isButcherable()) {
                    this.butchered(serverWorld, SoundCategory.PLAYERS);
                    itemStack.damage(1, player, getSlotForHand(hand));
                    player.swingHand(hand, true);
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            }
        }
    }

    /**
     * Adds custom data trackers to sheep
     *
     * @param builder Data tracker
     * @param ci      Unused
     */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectMobShearedTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(IS_SHEEP_BUTCHERED, false);
        builder.add(REGEN_SHEEP_TIMER, 0);
    }

    /**
     * Writes custom NBT data about sheep and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        try {
            nbt.putBoolean("IsSheepButchered", thisSheep().getDataTracker().get(IS_SHEEP_BUTCHERED));
            nbt.putInt("RegenSheepTicks", thisSheep().getDataTracker().get(REGEN_SHEEP_TIMER));
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to write sheep NBT", e);
        }
    }

    /**
     * Reads custom NBT data about sheep and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        try {
            if (nbt.contains("IsSheepButchered")) {
                thisSheep().getDataTracker().set(IS_SHEEP_BUTCHERED, nbt.getBoolean("IsSheepButchered"));
            }
            if (nbt.contains("RegenSheepTicks")) {
                thisSheep().getDataTracker().set(REGEN_SHEEP_TIMER, nbt.getInt("RegenSheepTicks"));
            }
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to read sheep NBT", e);
        }
    }
}

