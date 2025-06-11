/**
 * Main Mixin methods for chicken-shearing logic
 *
 * @author Elijah Potter
 * @date 5/23/2025
 */

package me.elijah.more_shearable_mobs.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.random.Random;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;
import static net.minecraft.entity.LivingEntity.getSlotForHand;

@Mixin(AnimalEntity.class)
public class MixinChickenEntity implements Shearable {

    /**
     * Shortcut method for referencing this chicken
     *
     * @return This chicken
     */
    @Unique
    private ChickenEntity thisChicken() {
        if ((Object) this instanceof ChickenEntity chicken)
            return chicken;
        else return null;
    }

    /**
     * From Shearable interface
     *
     * @return Whether a chicken is in a shearable state
     */
    @Override
    public boolean isShearable() {
        if (thisChicken() != null)
            return thisChicken().isAlive() && !isSheared() && !thisChicken().isBaby();
        else return false;
    }

    /**
     * @return Whether a chicken is in a butcherable state
     */
    @Unique
    public boolean isButcherable() {
        if (thisChicken() != null)
            return thisChicken().isAlive() && isSheared() && !isButchered() && !thisChicken().isBaby();
        else return false;
    }

    /**
     * @return Whether a chicken is sheared
     */
    @Unique
    public boolean isSheared() {
        if (thisChicken() != null)
            return thisChicken().getDataTracker().get(IS_CHICK_SHEARED);
        else return false;
    }

    /**
     * @return Whether a chicken is butchered
     */
    @Unique
    public boolean isButchered() {
        if (thisChicken() != null)
            return thisChicken().getDataTracker().get(IS_CHICK_BUTCHERED);
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
            thisChicken().getDataTracker().set(IS_CHICK_SHEARED, sheared);
            if (sheared) {
                thisChicken().getDataTracker().set(REGROW_CHICK_TIMER, sampleRegrowTimer(thisChicken().getRandom()));
            }
        }
    }

    /**
     * Sets the butcher state of a chicken
     *
     * @param butchered Whether the chicken is butchered
     */
    @Unique
    public void setButchered(boolean butchered) {
        if (thisChicken() != null) {
            thisChicken().getDataTracker().set(IS_CHICK_BUTCHERED, butchered);
            if (butchered) {
                thisChicken().getDataTracker().set(REGEN_CHICK_TIMER, sampleRegrowTimer(thisChicken().getRandom()));
                thisChicken().getDataTracker().set(REGROW_CHICK_TIMER, sampleRegrowTimer(thisChicken().getRandom()));
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
     * Method that performs the actual shearing to the chicken.
     *
     * @param world                The current world
     * @param shearedSoundCategory The sound of the shear
     * @param shears               The player's shears
     */
    @Override
    public void sheared(ServerWorld world, SoundCategory shearedSoundCategory, ItemStack shears) {
        world.playSoundFromEntity(null, thisChicken(), SoundEvents.ENTITY_SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
        setSheared(true);
        int dropCount = determineDropCount();
        ItemStack leatherStack = new ItemStack(Items.FEATHER, dropCount);
        ItemEntity drop = new ItemEntity(world, thisChicken().getX(), thisChicken().getY() + 1, thisChicken().getZ(), leatherStack);
        world.spawnEntity(drop);
        drop.setVelocity(drop.getVelocity().add((thisChicken().getRandom().nextFloat() - thisChicken().getRandom().nextFloat()) * 0.1F, thisChicken().getRandom().nextFloat() * 0.05F, (thisChicken().getRandom().nextFloat() - thisChicken().getRandom().nextFloat()) * 0.1F));
    }

    /**
     * Method that performs the actual butchering to the chicken.
     *
     * @param world                The current world
     * @param shearedSoundCategory The sound of the shear
     */
    @Unique
    public void butchered(ServerWorld world, SoundCategory shearedSoundCategory) {
        world.playSoundFromEntity(null, thisChicken(), SoundEvents.ENTITY_SLIME_SQUISH, shearedSoundCategory, 1.0F, 1.0F);
        setButchered(true);
        int dropCount = 1;
        ItemStack beefStack = new ItemStack(Items.CHICKEN, dropCount);
        ItemEntity drop = new ItemEntity(world, thisChicken().getX(), thisChicken().getY() + 1, thisChicken().getZ(), beefStack);
        world.spawnEntity(drop);
        drop.setVelocity(drop.getVelocity().add((thisChicken().getRandom().nextFloat() - thisChicken().getRandom().nextFloat()) * 0.1F, thisChicken().getRandom().nextFloat() * 0.05F, (thisChicken().getRandom().nextFloat() - thisChicken().getRandom().nextFloat()) * 0.1F));
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

    /**
     * Handles the logic for when a player interacts with a chicken using shears
     *
     * @param player Current player
     * @param hand   The player's hand
     * @param cir    Reports success of method
     */
    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if ((Object) this instanceof ChickenEntity chicken) {
            ItemStack itemStack = player.getStackInHand(hand);
            if (itemStack.isOf(Items.SHEARS)) {
                World var5 = chicken.getWorld();
                if (var5 instanceof ServerWorld serverWorld) {
                    if (this.isButcherable()) {
                        this.butchered(serverWorld, SoundCategory.PLAYERS);
                        itemStack.damage(1, player, getSlotForHand(hand));
                        player.swingHand(hand, true);
                        cir.setReturnValue(ActionResult.SUCCESS);
                    } else if (this.isShearable()) {
                        this.sheared(serverWorld, SoundCategory.PLAYERS, itemStack);
                        itemStack.damage(1, player, getSlotForHand(hand));
                        player.swingHand(hand, true);
                        cir.setReturnValue(ActionResult.SUCCESS);
                    }
                }
            }
        }
    }
}