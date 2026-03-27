/**
 * Main Mixin methods for sheep-butchering logic
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
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

@Mixin(Sheep.class)
public class MixinSheepEntity {

    /**
     * Shortcut method for referencing this sheep
     *
     * @return This sheep
     */
    @Unique
    private Sheep thisSheep() {
        return (Sheep) (Object) this;
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
        return thisSheep().getEntityData().get(IS_SHEEP_BUTCHERED);
    }

    /**
     * Sets the butcher state of a sheep
     *
     * @param butchered Whether the sheep is butchered
     */
    @Unique
    public void setButchered(boolean butchered) {
        thisSheep().getEntityData().set(IS_SHEEP_BUTCHERED, butchered);
        if (butchered) {
            thisSheep().getEntityData().set(REGEN_SHEEP_TIMER, sampleRegrowTimer(thisSheep().getRandom()));
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
     * Method that performs the actual butchering to the sheep.
     *
     * @param level                The current world
     * @param shearedSoundCategory The sound of the shear
     */
    @Unique
    public void butchered(ServerLevel level, SoundSource shearedSoundCategory) {
        level.playSound(null, thisSheep(), SoundEvents.SLIME_SQUISH, shearedSoundCategory, 1.0F, 1.0F);
        setButchered(true);
        int dropCount = determineDropCount();
        ItemStack beefStack = new ItemStack(Items.MUTTON, dropCount);
        ItemEntity drop = new ItemEntity(level, thisSheep().getX(), thisSheep().getY() + 1, thisSheep().getZ(), beefStack);
        level.addFreshEntity(drop);
        drop.setDeltaMovement(drop.getDeltaMovement().add((thisSheep().getRandom().nextFloat() - thisSheep().getRandom().nextFloat()) * 0.1F, thisSheep().getRandom().nextFloat() * 0.05F, (thisSheep().getRandom().nextFloat() - thisSheep().getRandom().nextFloat()) * 0.1F));
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
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true, remap = false)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.SHEARS)) {
            Level level = thisSheep().level();
            if (level instanceof ServerLevel serverLevel) {
                // Only intercept if we are butchering it. Otherwise, let vanilla handle the wool shearing.
                if (this.isButcherable()) {
                    this.butchered(serverLevel, SoundSource.PLAYERS);
                    itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    player.swing(hand, true);
                    cir.setReturnValue(InteractionResult.SUCCESS);
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
    @Inject(method = "defineSynchedData", at = @At("TAIL"), remap = false)
    private void injectMobShearedTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(IS_SHEEP_BUTCHERED, false);
        builder.define(REGEN_SHEEP_TIMER, 0);
    }

    /**
     * Writes custom NBT data about sheep and their shear-states
     * to persist between loads
     *
     * @param output NBT writer
     * @param ci     Unused
     */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void onWriteNbt(ValueOutput output, CallbackInfo ci) {
        try {
            output.putBoolean("IsSheepButchered", thisSheep().getEntityData().get(IS_SHEEP_BUTCHERED));
            output.putInt("RegenSheepTicks", thisSheep().getEntityData().get(REGEN_SHEEP_TIMER));
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to write sheep NBT", e);
        }
    }

    /**
     * Reads custom NBT data about sheep and their shear-states
     * to persist between loads
     *
     * @param input NBT writer
     * @param ci    Unused
     */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), remap = false)
    private void onReadNbt(ValueInput input, CallbackInfo ci) {
        try {
            thisSheep().getEntityData().set(IS_SHEEP_BUTCHERED, input.getBooleanOr("IsSheepButchered", false));
            thisSheep().getEntityData().set(REGEN_SHEEP_TIMER, input.getIntOr("RegenSheepTicks", 0));
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to read sheep NBT", e);
        }
    }
}

