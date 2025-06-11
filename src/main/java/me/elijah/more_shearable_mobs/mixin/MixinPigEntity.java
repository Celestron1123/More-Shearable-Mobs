/**
 * Main Mixin methods for pig-shearing logic
 *
 * @author Elijah Potter
 * @date 5/23/2025
 */

package me.elijah.more_shearable_mobs.mixin;

import net.minecraft.entity.Shearable;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.ItemEntity;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;
import static net.minecraft.entity.LivingEntity.getSlotForHand;

@Mixin(PigEntity.class)
public class MixinPigEntity implements Shearable {

    /**
     * Shortcut method for referencing this pig
     *
     * @return This pig
     */
    @Unique
    private PigEntity thisPig() {
        return (PigEntity) (Object) this;
    }

    /**
     * From Shearable interface
     *
     * @return Whether a pig is in a shearable state
     */
    @Override
    public boolean isShearable() {
        return thisPig().isAlive() && !isSheared() && !thisPig().isBaby();
    }

    /**
     * @return Whether a pig is sheared
     */
    @Unique
    public boolean isSheared() {
        return thisPig().getDataTracker().get(IS_PIG_SHEARED);
    }

    /**
     * Sets the sheared state of a pig, starts the shear countdown
     *
     * @param sheared What state the pig should enter
     */
    @Unique
    public void setSheared(boolean sheared) {
        thisPig().getDataTracker().set(IS_PIG_SHEARED, sheared);
        if (sheared) {
            thisPig().getDataTracker().set(REGROW_PIG_TIMER, sampleRegrowTimer(thisPig().getRandom()));
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
     * Method that performs the actual shearing to the pig.
     *
     * @param world                The current world
     * @param shearedSoundCategory The sound of the shear
     * @param shears               The player's shears
     */
    @Override
    public void sheared(ServerWorld world, SoundCategory shearedSoundCategory, ItemStack shears) {
        world.playSoundFromEntity(null, thisPig(), SoundEvents.ENTITY_SLIME_SQUISH, shearedSoundCategory, 1.0F, 1.0F);
        setSheared(true);
        int dropCount = determineDropCount();
        ItemStack leatherStack = new ItemStack(Items.PORKCHOP, dropCount);
        ItemEntity drop = new ItemEntity(world, thisPig().getX(), thisPig().getY() + 1, thisPig().getZ(), leatherStack);
        world.spawnEntity(drop);
        drop.setVelocity(drop.getVelocity().add((thisPig().getRandom().nextFloat() - thisPig().getRandom().nextFloat()) * 0.1F, thisPig().getRandom().nextFloat() * 0.05F, (thisPig().getRandom().nextFloat() - thisPig().getRandom().nextFloat()) * 0.1F));
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
    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.SHEARS)) {
            World var5 = thisPig().getWorld();
            if (var5 instanceof ServerWorld serverWorld) {
                if (this.isShearable()) {
                    this.sheared(serverWorld, SoundCategory.PLAYERS, itemStack);
                    itemStack.damage(1, player, getSlotForHand(hand));
                    player.swingHand(hand, true);
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            }
        }
    }
}
