package me.elijah.shearable_cows.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.entity.passive.CowEntity;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.entity.LivingEntity.getSlotForHand;
import static me.elijah.shearable_cows.CowDataTrackers.IS_SHEARED;
import static me.elijah.shearable_cows.CowDataTrackers.REGROW_TIMER;

@Mixin(CowEntity.class)
public class MixinCowEntity implements Shearable {

    @Unique
    protected final Random random = Random.create();

    @Unique
    private CowEntity thisCow() {
        return (CowEntity)(Object)this;
    }

    @Override
    public boolean isShearable() {
        return thisCow().isAlive() && !isSheared() && !thisCow().isBaby();
    }

    @Unique
    public boolean isSheared(){
        return thisCow().getDataTracker().get(IS_SHEARED);
    }

    @Unique
    public void setSheared(boolean sheared) {
        thisCow().getDataTracker().set(IS_SHEARED, sheared);
        thisCow().getDataTracker().set(REGROW_TIMER, sampleRegrowTimer(random));
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

    @Override
    public void sheared(ServerWorld world, SoundCategory shearedSoundCategory, ItemStack shears) {
        world.playSoundFromEntity((PlayerEntity)null, thisCow(), SoundEvents.ENTITY_SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
        setSheared(true);
        int dropCount;
        float chance = thisCow().getRandom().nextFloat();
        if (chance < 0.45f) {
            dropCount = 1;
        } else if (chance < 0.90f) {
            dropCount = 2;
        } else {
            dropCount = 3;
        }
        ItemStack leatherStack = new ItemStack(Items.LEATHER, dropCount);
        ItemEntity drop = new ItemEntity(world, thisCow().getX(), thisCow().getY() + 1, thisCow().getZ(), leatherStack);
        world.spawnEntity(drop);
        drop.setVelocity(drop.getVelocity().add((double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double)(this.random.nextFloat() * 0.05F), (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)));
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.SHEARS)){
            World var5 = thisCow().getWorld();
            if (var5 instanceof ServerWorld){
                ServerWorld serverWorld = (ServerWorld)var5;
                if (this.isShearable()){
                    this.sheared(serverWorld, SoundCategory.PLAYERS, itemStack);
                    itemStack.damage(1, player, getSlotForHand(hand));
                    player.swingHand(hand, true);
                    cir.setReturnValue(ActionResult.SUCCESS);
                }
            }
        }
    }
}
