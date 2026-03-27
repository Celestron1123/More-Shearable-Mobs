/**
 * Mixin methods for passive entities, utilizing methods not
 * available to certain mob classes in particular.
 *
 * @author Elijah Potter
 * @date 03/25/2026
 */

package me.elijah.more_shearable_mobs.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    /**
     * Every tick, the mob's timer is checked and decremented
     * until 0 to restore the skin.
     *
     * @param ci Unused
     */
    @Inject(method = "tick", at = @At("HEAD"), remap = false)
    private void onTick(CallbackInfo ci) {
        if ((Object) this instanceof Cow cow) {
//            cow.setCustomName(Text.literal("Regen: " + (cow.getDataTracker().get(REGEN_COW_TIMER) / 20) + "s   " + "Regrow: " + (cow.getDataTracker().get(REGROW_COW_TIMER) / 20) + "s"));
//            cow.setCustomNameVisible(true);
            if (cow.getEntityData().get(IS_COW_BUTCHERED)) {
                int ticks = cow.getEntityData().get(REGEN_COW_TIMER);
                if (ticks > 0) {
                    cow.getEntityData().set(REGEN_COW_TIMER, ticks - 1);
                } else {
                    cow.getEntityData().set(IS_COW_BUTCHERED, false);
                }
            } else if (cow.getEntityData().get(IS_COW_SHEARED)) {
                int ticks = cow.getEntityData().get(REGROW_COW_TIMER);
                if (ticks > 0) {
                    cow.getEntityData().set(REGROW_COW_TIMER, ticks - 1);
                } else {
                    cow.getEntityData().set(IS_COW_SHEARED, false);
                }
            }
        } else if ((Object) this instanceof Chicken chick) {
            if (chick.getEntityData().get(IS_CHICK_BUTCHERED)) {
                int ticks = chick.getEntityData().get(REGEN_CHICK_TIMER);
                if (ticks > 0) {
                    chick.getEntityData().set(REGEN_CHICK_TIMER, ticks - 1);
                } else {
                    chick.getEntityData().set(IS_CHICK_BUTCHERED, false);
                }
            } else if (chick.getEntityData().get(IS_CHICK_SHEARED)) {
                int ticks = chick.getEntityData().get(REGROW_CHICK_TIMER);
                if (ticks > 0) {
                    chick.getEntityData().set(REGROW_CHICK_TIMER, ticks - 1);
                } else {
                    chick.getEntityData().set(IS_CHICK_SHEARED, false);
                }
            }
        } else if ((Object) this instanceof Pig pig) {
            if (pig.getEntityData().get(IS_PIG_BUTCHERED)) {
                int ticks = pig.getEntityData().get(REGEN_PIG_TIMER);
                if (ticks > 0) {
                    pig.getEntityData().set(REGEN_PIG_TIMER, ticks - 1);
                } else {
                    pig.getEntityData().set(IS_PIG_BUTCHERED, false);
                }
            }
        } else if ((Object) this instanceof Sheep sheep) {
            if (sheep.getEntityData().get(IS_SHEEP_BUTCHERED)) {
                int ticks = sheep.getEntityData().get(REGEN_SHEEP_TIMER);
                if (ticks > 0) {
                    sheep.getEntityData().set(REGEN_SHEEP_TIMER, ticks - 1);
                } else {
                    sheep.getEntityData().set(IS_SHEEP_BUTCHERED, false);
                }
            }
        }
    }

    /**
     * Modifies the mob's loot drops depending on its state
     *
     * @param level          The current world
     * @param damageSource   Where the damage is coming from
     * @param causedByPlayer Whether the damage is caused by the player
     * @param ci             Callback Info
     */
    @Inject(method = "dropFromLootTable", at = @At("HEAD"), cancellable = true, remap = false)
    private void modifyDrops(ServerLevel level, DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity mob) {
            int dropCount = determineDropCount(mob);
            ItemStack goodieStack;

            // Fire aspect check
            boolean killedByFireAspect = false;
            if (damageSource.getEntity() instanceof LivingEntity attacker) {
                ItemStack weapon = attacker.getMainHandItem();
                Holder<Enchantment> fireAspect = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FIRE_ASPECT);
                if (EnchantmentHelper.getItemEnchantmentLevel(fireAspect, weapon) > 0) {
                    killedByFireAspect = true;
                }
            }

            // Looting check
            int lootingLvl = 0;
            if (damageSource.getEntity() instanceof LivingEntity attacker) {
                ItemStack weapon = attacker.getMainHandItem();
                Holder<Enchantment> loot = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
                lootingLvl = EnchantmentHelper.getItemEnchantmentLevel(loot, weapon);
            }

            dropCount += lootingLvl;

            if (mob instanceof Cow cow) {
                if (cow.getEntityData().get(IS_COW_BUTCHERED))
                    goodieStack = new ItemStack(Items.BONE, dropCount);
                else if (cow.getEntityData().get(IS_COW_SHEARED) && (damageSource.is(DamageTypeTags.IS_FIRE) || cow.isOnFire() || killedByFireAspect))
                    goodieStack = new ItemStack(Items.COOKED_BEEF, dropCount);
                else if (cow.getEntityData().get(IS_COW_SHEARED))
                    goodieStack = new ItemStack(Items.BEEF, dropCount);
                else return;
            } else if (mob instanceof Chicken chick) {
                if (chick.getEntityData().get(IS_CHICK_BUTCHERED))
                    goodieStack = new ItemStack(Items.BONE, dropCount);
                else if (chick.getEntityData().get(IS_CHICK_SHEARED) && (damageSource.is(DamageTypeTags.IS_FIRE) || chick.isOnFire() || killedByFireAspect))
                    goodieStack = new ItemStack(Items.COOKED_CHICKEN, 1 + lootingLvl);
                else if (chick.getEntityData().get(IS_CHICK_SHEARED))
                    goodieStack = new ItemStack(Items.CHICKEN, 1 + lootingLvl);
                else return;
            } else if (mob instanceof Pig pig) {
                if (pig.getEntityData().get(IS_PIG_BUTCHERED))
                    goodieStack = new ItemStack(Items.BONE, dropCount);
                else return;
            } else if (mob instanceof Sheep sheep) {
                if (sheep.getEntityData().get(IS_SHEEP_BUTCHERED))
                    goodieStack = new ItemStack(Items.BONE, dropCount);
                else return;
            } else return;

            ItemEntity drop = new ItemEntity(level, mob.getX(), mob.getY() + 1, mob.getZ(), goodieStack);
            level.addFreshEntity(drop);
            drop.setDeltaMovement(drop.getDeltaMovement().add((mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.1F, mob.getRandom().nextFloat() * 0.05F, (mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.1F));
            ci.cancel(); // Prevent normal loot from dropping
        }
    }

    /**
     * Determines how much loot ought to be dropped
     *
     * @return integer of how much loot to be dropped
     */
    @Unique
    private int determineDropCount(LivingEntity mob) {
        float chance = mob.getRandom().nextFloat();
        if (chance < 0.45f) {
            return 1;
        } else if (chance < 0.90f) {
            return 2;
        } else {
            return 3;
        }
    }
}