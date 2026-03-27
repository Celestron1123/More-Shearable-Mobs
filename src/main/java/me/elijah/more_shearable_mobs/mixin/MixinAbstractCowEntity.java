/**
 * Main Mixin methods for abstract cow-shearing logic
 *
 * @author Elijah Potter
 * @date 3/25/2025
 */

package me.elijah.more_shearable_mobs.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

@Mixin(AbstractCow.class)
public class MixinAbstractCowEntity implements Shearable {

    /**
     * Shortcut method for referencing this cow
     *
     * @return This cow
     */
    @Unique
    private AbstractCow thisCow() {
        return (AbstractCow) (Object) this;
    }

    /**
     * From Shearable interface
     *
     * @return Whether a cow is in a shearable state
     */
    @Override
    public boolean readyForShearing() {
        return thisCow().isAlive() && !isSheared() && !thisCow().isBaby();
    }

    /**
     * @return Whether a cow is in a butcherable state
     */
    @Unique
    public boolean isButcherable() {
        return thisCow().isAlive() && isSheared() && !isButchered() && !thisCow().isBaby();
    }

    /**
     * @return Whether a cow is sheared
     */
    @Unique
    public boolean isSheared() {
        return thisCow().getEntityData().get(IS_COW_SHEARED);
    }

    /**
     * @return Whether a cow is butchered
     */
    @Unique
    public boolean isButchered() {
        return thisCow().getEntityData().get(IS_COW_BUTCHERED);
    }

    /**
     * Sets the sheared state of a cow, starts the shear countdown
     *
     * @param sheared What state the cow should enter
     */
    @Unique
    public void setSheared(boolean sheared) {
        thisCow().getEntityData().set(IS_COW_SHEARED, sheared);
        if (sheared) {
            thisCow().getEntityData().set(REGROW_COW_TIMER, sampleRegrowTimer(thisCow().getRandom()));
        }
    }

    /**
     * Sets the butcher state of a cow
     *
     * @param butchered Whether the cow is butchered
     */
    @Unique
    public void setButchered(boolean butchered) {
        thisCow().getEntityData().set(IS_COW_BUTCHERED, butchered);
        if (butchered) {
            thisCow().getEntityData().set(REGEN_COW_TIMER, sampleRegrowTimer(thisCow().getRandom()));
            thisCow().getEntityData().set(REGROW_COW_TIMER, sampleRegrowTimer(thisCow().getRandom()));
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
     * Method that performs the actual shearing to the cow.
     *
     * @param world                The current world
     * @param shearedSoundCategory The sound of the shear
     * @param shears               The player's shears
     */
    @Override
    public void shear(ServerLevel world, @NotNull SoundSource shearedSoundCategory, @NotNull ItemStack shears) {
        world.playSound(null, thisCow(), SoundEvents.SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
        setSheared(true);
        int dropCount = determineDropCount();
        ItemStack leatherStack = new ItemStack(Items.LEATHER, dropCount);
        ItemEntity drop = new ItemEntity(world, thisCow().getX(), thisCow().getY() + 1, thisCow().getZ(), leatherStack);
        world.addFreshEntity(drop);
        drop.setDeltaMovement(drop.getDeltaMovement().add((thisCow().getRandom().nextFloat() - thisCow().getRandom().nextFloat()) * 0.1F, thisCow().getRandom().nextFloat() * 0.05F, (thisCow().getRandom().nextFloat() - thisCow().getRandom().nextFloat()) * 0.1F));
    }

    /**
     * Method that performs the actual butchering to the cow.
     *
     * @param world                The current world
     * @param shearedSoundCategory The sound of the shear
     */
    @Unique
    public void butchered(ServerLevel world, SoundSource shearedSoundCategory) {
        world.playSound(null, thisCow(), SoundEvents.SLIME_SQUISH, shearedSoundCategory, 1.0F, 1.0F);
        setButchered(true);
        int dropCount = determineDropCount();
        ItemStack beefStack = new ItemStack(Items.BEEF, dropCount);
        ItemEntity drop = new ItemEntity(world, thisCow().getX(), thisCow().getY() + 1, thisCow().getZ(), beefStack);
        world.addFreshEntity(drop);
        drop.setDeltaMovement(drop.getDeltaMovement().add((thisCow().getRandom().nextFloat() - thisCow().getRandom().nextFloat()) * 0.1F, thisCow().getRandom().nextFloat() * 0.05F, (thisCow().getRandom().nextFloat() - thisCow().getRandom().nextFloat()) * 0.1F));
    }

    /**
     * Determines how much leather/beef ought to be dropped
     *
     * @return integer of how much leather/beef to be dropped
     */
    @Unique
    private int determineDropCount() {
        float chance = thisCow().getRandom().nextFloat();
        if (chance < 0.45f) {
            return 1;
        } else if (chance < 0.90f) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Handles the logic for when a player interacts with a cow using shears
     *
     * @param player Current player
     * @param hand   The player's hand
     * @param cir    Reports success of method
     */
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true, remap = false)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.SHEARS)) {
            Level var5 = thisCow().level();
            if (var5 instanceof ServerLevel serverLevel) {
                if (this.isButcherable()) {
                    this.butchered(serverLevel, SoundSource.PLAYERS);
                    itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    player.swing(hand, true);
                    cir.setReturnValue(InteractionResult.SUCCESS);
                } else if (this.readyForShearing()) {
                    this.shear(serverLevel, SoundSource.PLAYERS, itemStack);
                    itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    player.swing(hand, true);
                    cir.setReturnValue(InteractionResult.SUCCESS);
                }
            }
        }
    }
}

