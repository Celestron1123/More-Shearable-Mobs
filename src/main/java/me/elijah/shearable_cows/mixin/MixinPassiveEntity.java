/**
 * Mixin methods for passive entities, utilizing methods not
 * available to the cow class in particular.
 *
 * @author Elijah Potter
 * @date 5/19/2025
 */

package me.elijah.shearable_cows.mixin;

import me.elijah.shearable_cows.CowDataTrackers;
import me.elijah.shearable_cows.Shearable_cows;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.elijah.shearable_cows.CowDataTrackers.*;

@Mixin(LivingEntity.class)
public class MixinPassiveEntity {

    /**
     * Adds custom data trackers to cows
     *
     * @param builder Data tracker
     * @param ci      Unused
     */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectCowShearedTracker(DataTracker.Builder builder, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity) {
            builder.add(IS_SHEARED, false);
            builder.add(IS_BUTCHERED, false);
            builder.add(REGROW_TIMER, 0);
            builder.add(REGEN_TIMER, 0);
        }
    }

    /**
     * Every tick, the cow's timer is checked and decremented
     * until 0 to restore the skin.
     *
     * @param ci Unused
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if ((Object) this instanceof CowEntity cow) {
            if (cow.getDataTracker().get(IS_BUTCHERED)) {
                int ticks = cow.getDataTracker().get(REGEN_TIMER);
                if (ticks > 0) {
                    cow.getDataTracker().set(REGEN_TIMER, ticks - 1);
                } else {
                    cow.getDataTracker().set(IS_BUTCHERED, false);
                }
            } else if (cow.getDataTracker().get(IS_SHEARED)) {
                int ticks = cow.getDataTracker().get(REGROW_TIMER);
                if (ticks > 0) {
                    cow.getDataTracker().set(REGROW_TIMER, ticks - 1);
                } else {
                    cow.getDataTracker().set(IS_SHEARED, false);
                }
            }
        }
    }

    /**
     * Writes custom NBT data about cows and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void onWriteNbt(NbtCompound nbt, CallbackInfo ci) {
        try {
            if ((Object) this instanceof CowEntity cow) {
                nbt.putBoolean("IsSheared", cow.getDataTracker().get(CowDataTrackers.IS_SHEARED));
                nbt.putBoolean("IsButchered", cow.getDataTracker().get(IS_BUTCHERED));
                nbt.putInt("RegrowTicks", cow.getDataTracker().get(CowDataTrackers.REGROW_TIMER));
                nbt.putInt("RegenTicks", cow.getDataTracker().get(CowDataTrackers.REGEN_TIMER));
            }
        } catch (Exception e) {
            Shearable_cows.LOGGER.error("Failed to write cow NBT", e);
        }
    }

    /**
     * Reads custom NBT data about cows and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
        try {
            if ((Object) this instanceof CowEntity cow) {
                if (nbt.contains("IsSheared")) {
                    cow.getDataTracker().set(CowDataTrackers.IS_SHEARED, nbt.getBoolean("IsSheared"));
                }
                if (nbt.contains("IsButchered")) {
                    cow.getDataTracker().set(IS_BUTCHERED, nbt.getBoolean("IsButchered"));
                }
                if (nbt.contains("RegrowTicks")) {
                    cow.getDataTracker().set(CowDataTrackers.REGROW_TIMER, nbt.getInt("RegrowTicks"));
                }
                if (nbt.contains("RegenTicks")) {
                    cow.getDataTracker().set(CowDataTrackers.REGEN_TIMER, nbt.getInt("RegenTicks"));
                }
            }
        } catch (Exception e) {
            Shearable_cows.LOGGER.error("Failed to read cow NBT", e);
        }
    }

    /**
     * Modifies the cow's loot drops depending on its state
     *
     * @param world          The current world
     * @param damageSource   Where the damage is coming  from
     * @param causedByPlayer Whether the damage is caused by the player
     * @param ci             Callback Info
     */
    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void modifyDrops(ServerWorld world, DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
        if ((Object) this instanceof CowEntity cow) {
            int dropCount = determineDropCount(cow);
            ItemStack goodieStack;

            // Fire aspect check
            boolean killedByFireAspect = false;
            if (damageSource.getAttacker() instanceof LivingEntity attacker) {
                ItemStack weapon = attacker.getMainHandStack();
                RegistryEntry<Enchantment> fireAspect = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT);
                if (EnchantmentHelper.getLevel(fireAspect, weapon) > 0) {
                    killedByFireAspect = true;
                }
            }

            if (cow.getDataTracker().get(IS_BUTCHERED))
                goodieStack = new ItemStack(Items.BONE, dropCount);
            else if (cow.getDataTracker().get(IS_SHEARED) && (damageSource.isIn(DamageTypeTags.IS_FIRE) || cow.isOnFire() || killedByFireAspect))
                goodieStack = new ItemStack(Items.COOKED_BEEF, dropCount);
            else if (cow.getDataTracker().get(IS_SHEARED))
                goodieStack = new ItemStack(Items.BEEF, dropCount);
            else return;

            ItemEntity drop = new ItemEntity(world, cow.getX(), cow.getY() + 1, cow.getZ(), goodieStack);
            world.spawnEntity(drop);
            drop.setVelocity(drop.getVelocity().add((cow.getRandom().nextFloat() - cow.getRandom().nextFloat()) * 0.1F, cow.getRandom().nextFloat() * 0.05F, (cow.getRandom().nextFloat() - cow.getRandom().nextFloat()) * 0.1F));
            ci.cancel(); // Prevent normal loot from dropping
        }
    }

    /**
     * Determines how much leather/beef ought to be dropped
     *
     * @return integer of how much leather/beef to be dropped
     */
    @Unique
    private int determineDropCount(CowEntity cow) {
        float chance = cow.getRandom().nextFloat();
        if (chance < 0.45f) {
            return 1;
        } else if (chance < 0.90f) {
            return 2;
        } else {
            return 3;
        }
    }
}
