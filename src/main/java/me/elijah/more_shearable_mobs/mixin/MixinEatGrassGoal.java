/**
 * Stops sheep from eating grass while butchered
 *
 * @author: Elijah Potter
 * @date: 03/26/2026
 */

package me.elijah.more_shearable_mobs.mixin;

import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sheep.Sheep;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.IS_SHEEP_BUTCHERED;

@Mixin(EatBlockGoal.class)
public class MixinEatGrassGoal {

    @Shadow
    @Final
    private Mob mob;

    /**
     * Per the class description, just stops sheep from eating while butchered
     *
     * @param cir definitely does something
     */
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true, remap = false)
    private void onCanStart(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof Sheep sheep) {
            if (sheep.getEntityData().get(IS_SHEEP_BUTCHERED)) {
                cir.setReturnValue(false);
            }
        }
    }
}
