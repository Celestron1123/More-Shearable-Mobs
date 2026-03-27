/**
 * Coverage for chickens
 *
 * @author Elijah Potter
 * @date 03/26/2026
 */

package me.elijah.more_shearable_mobs.mixin;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Animal.class)
public class MixinAnimalEntity {

//    /$$$$$$$$                  /$$              /$$$$$$  /$$       /$$           /$$
//   | $$_____/                 | $$             /$$__  $$| $$      |__/          | $$
//   | $$    /$$   /$$  /$$$$$$$| $$   /$$      | $$  \__/| $$$$$$$  /$$  /$$$$$$$| $$   /$$  /$$$$$$  /$$$$$$$   /$$$$$$$
//   | $$$$$| $$  | $$ /$$_____/| $$  /$$/      | $$      | $$__  $$| $$ /$$_____/| $$  /$$/ /$$__  $$| $$__  $$ /$$_____/
//   | $$__/| $$  | $$| $$      | $$$$$$/       | $$      | $$  \ $$| $$| $$      | $$$$$$/ | $$$$$$$$| $$  \ $$|  $$$$$$
//   | $$   | $$  | $$| $$      | $$_  $$       | $$    $$| $$  | $$| $$| $$      | $$_  $$ | $$_____/| $$  | $$ \____  $$
//   | $$   |  $$$$$$/|  $$$$$$$| $$ \  $$      |  $$$$$$/| $$  | $$| $$|  $$$$$$$| $$ \  $$|  $$$$$$$| $$  | $$ /$$$$$$$/
//   |__/    \______/  \_______/|__/  \__/       \______/ |__/  |__/|__/ \_______/|__/  \__/ \_______/|__/  |__/|_______/

    /**
     * Supermethod: Handles the logic for when a player interacts with a chicken using shears
     *
     * @param player Current player
     * @param hand   The player's hand
     * @param cir    Reports success of method
     */
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true, remap = false)
    private void onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if ((Object) this instanceof Chicken chicken) {
            ItemStack itemStack = player.getItemInHand(hand);
            if (itemStack.is(Items.SHEARS)) {
                Level var5 = chicken.level();
                if (var5 instanceof ServerLevel serverWorld) {
                    if (chicken.isAlive() && chicken.getEntityData().get(IS_CHICK_SHEARED) && !chicken.getEntityData().get(IS_CHICK_BUTCHERED) && !chicken.isBaby()) { // isButcherable
                        serverWorld.playSound(null, chicken, SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
                        chicken.getEntityData().set(IS_CHICK_BUTCHERED, true);
                        chicken.getEntityData().set(REGEN_CHICK_TIMER, sampleRegrowTimer(chicken.getRandom()));
                        chicken.getEntityData().set(REGROW_CHICK_TIMER, sampleRegrowTimer(chicken.getRandom()));
                        int dropCount = 1;
                        ItemStack beefStack = new ItemStack(Items.CHICKEN, dropCount);
                        ItemEntity drop = new ItemEntity(serverWorld, chicken.getX(), chicken.getY() + 1, chicken.getZ(), beefStack);
                        serverWorld.addFreshEntity(drop);
                        drop.setDeltaMovement(drop.getDeltaMovement().add((chicken.getRandom().nextFloat() - chicken.getRandom().nextFloat()) * 0.1F, chicken.getRandom().nextFloat() * 0.05F, (chicken.getRandom().nextFloat() - chicken.getRandom().nextFloat()) * 0.1F));
                        itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                        player.swing(hand, true);
                        cir.setReturnValue(InteractionResult.SUCCESS);
                    } else if (((Shearable) chicken).readyForShearing()) {
                        ((Shearable) chicken).shear(serverWorld, SoundSource.PLAYERS, itemStack);
                        itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                        player.swing(hand, true);
                        cir.setReturnValue(InteractionResult.SUCCESS);
                    }
                }
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
}
