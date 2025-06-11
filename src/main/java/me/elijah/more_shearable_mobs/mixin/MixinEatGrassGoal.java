/**
 * Stops sheep from eating grass while butchered
 *
 * @author: Elijah Potter
 * @date: 6/5/2025
 */

package me.elijah.more_shearable_mobs.mixin;

import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.IS_SHEEP_BUTCHERED;

@Mixin(EatGrassGoal.class)
public class MixinEatGrassGoal {

    @Shadow
    @Final
    private MobEntity mob;


    @Inject(method = "canStart", at = @At("HEAD"), cancellable = true)
    private void onCanStart(CallbackInfoReturnable<Boolean> cir) {
        if (mob instanceof SheepEntity sheep) {
            if (sheep.getDataTracker().get(IS_SHEEP_BUTCHERED)) {
                cir.setReturnValue(false);
            }
        }
    }
}
