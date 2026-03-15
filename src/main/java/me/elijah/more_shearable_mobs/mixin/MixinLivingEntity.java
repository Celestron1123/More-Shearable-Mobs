/**
 * Mixin methods for passive entities, utilizing methods not
 * available to certain mob classes in particular.
 *
 * @author Elijah Potter
 * @date 10/10/2025
 */

package me.elijah.more_shearable_mobs.mixin;

import me.elijah.more_shearable_mobs.More_shearable_mobs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.elijah.more_shearable_mobs.ShearDataTrackers.*;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {

    /**
     * Adds custom data trackers to mobs (chickens)
     *
     * @param builder Data tracker
     * @param ci      Unused
     */
    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void injectMobShearedTracker(DataTracker.Builder builder, CallbackInfo ci) {
        if ((Object) this instanceof ChickenEntity) {
            builder.add(IS_CHICK_SHEARED, false);
            builder.add(IS_CHICK_BUTCHERED, false);
            builder.add(REGROW_CHICK_TIMER, 0);
            builder.add(REGEN_CHICK_TIMER, 0);
        }
    }

    /**
     * Every tick, the mob's timer is checked and decremented
     * until 0 to restore the skin.
     *
     * @param ci Unused
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if ((Object) this instanceof CowEntity cow) {
//            cow.setCustomName(Text.literal("Regen: " + (cow.getDataTracker().get(REGEN_COW_TIMER) / 20) + "s   " + "Regrow: " + (cow.getDataTracker().get(REGROW_COW_TIMER) / 20) + "s"));
//            cow.setCustomNameVisible(true);
            if (cow.getDataTracker().get(IS_COW_BUTCHERED)) {
                int ticks = cow.getDataTracker().get(REGEN_COW_TIMER);
                if (ticks > 0) {
                    cow.getDataTracker().set(REGEN_COW_TIMER, ticks - 1);
                } else {
                    cow.getDataTracker().set(IS_COW_BUTCHERED, false);
                }
            } else if (cow.getDataTracker().get(IS_COW_SHEARED)) {
                int ticks = cow.getDataTracker().get(REGROW_COW_TIMER);
                if (ticks > 0) {
                    cow.getDataTracker().set(REGROW_COW_TIMER, ticks - 1);
                } else {
                    cow.getDataTracker().set(IS_COW_SHEARED, false);
                }
            }
        } else if ((Object) this instanceof ChickenEntity chick) {
            if (chick.getDataTracker().get(IS_CHICK_BUTCHERED)) {
                int ticks = chick.getDataTracker().get(REGEN_CHICK_TIMER);
                if (ticks > 0) {
                    chick.getDataTracker().set(REGEN_CHICK_TIMER, ticks - 1);
                } else {
                    chick.getDataTracker().set(IS_CHICK_BUTCHERED, false);
                }
            } else if (chick.getDataTracker().get(IS_CHICK_SHEARED)) {
                int ticks = chick.getDataTracker().get(REGROW_CHICK_TIMER);
                if (ticks > 0) {
                    chick.getDataTracker().set(REGROW_CHICK_TIMER, ticks - 1);
                } else {
                    chick.getDataTracker().set(IS_CHICK_SHEARED, false);
                }
            }
        } else if ((Object) this instanceof PigEntity pig) {
            if (pig.getDataTracker().get(IS_PIG_BUTCHERED)) {
                int ticks = pig.getDataTracker().get(REGEN_PIG_TIMER);
                if (ticks > 0) {
                    pig.getDataTracker().set(REGEN_PIG_TIMER, ticks - 1);
                } else {
                    pig.getDataTracker().set(IS_PIG_BUTCHERED, false);
                }
            }
        } else if ((Object) this instanceof SheepEntity sheep) {
            if (sheep.getDataTracker().get(IS_SHEEP_BUTCHERED)) {
                int ticks = sheep.getDataTracker().get(REGEN_SHEEP_TIMER);
                if (ticks > 0) {
                    sheep.getDataTracker().set(REGEN_SHEEP_TIMER, ticks - 1);
                } else {
                    sheep.getDataTracker().set(IS_SHEEP_BUTCHERED, false);
                }
            }
        }
    }

    /**
     * Writes custom NBT data about mobs (chickens) and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void onWriteNbt(WriteView nbt, CallbackInfo ci) {
        try {
            if ((Object) this instanceof ChickenEntity chick) {
                nbt.putBoolean("IsChickSheared", chick.getDataTracker().get(IS_CHICK_SHEARED));
                nbt.putBoolean("IsChickButchered", chick.getDataTracker().get(IS_CHICK_BUTCHERED));
                nbt.putInt("RegrowChickTicks", chick.getDataTracker().get(REGROW_CHICK_TIMER));
                nbt.putInt("RegenChickTicks", chick.getDataTracker().get(REGEN_CHICK_TIMER));
            }
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to write chicken NBT", e);
        }
    }

    /**
     * Reads custom NBT data about mobs (chickens) and their shear-states
     * to persist between loads
     *
     * @param nbt NBT writer
     * @param ci  Unused
     */
    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void onReadNbt(ReadView nbt, CallbackInfo ci) {
        try {
            if ((Object) this instanceof ChickenEntity chick) {
                chick.getDataTracker().set(IS_CHICK_SHEARED, nbt.getBoolean("IsChickSheared", false));
                chick.getDataTracker().set(IS_CHICK_BUTCHERED, nbt.getBoolean("IsChickButchered", false));
                chick.getDataTracker().set(REGROW_CHICK_TIMER, nbt.getInt("RegrowChickTicks", 0));
                chick.getDataTracker().set(REGEN_CHICK_TIMER, nbt.getInt("RegenChickTicks", 0));
            }
        } catch (Exception e) {
            More_shearable_mobs.LOGGER.error("Failed to read chicken NBT", e);
        }
    }

    /**
     * Modifies the mob's loot drops depending on its state
     *
     * @param world          The current world
     * @param damageSource   Where the damage is coming from
     * @param causedByPlayer Whether the damage is caused by the player
     * @param ci             Callback Info
     */
    @Inject(method = "dropLoot", at = @At("HEAD"), cancellable = true)
    private void modifyDrops(ServerWorld world, DamageSource damageSource, boolean causedByPlayer, CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity mob) {
            int dropCount = determineDropCount(mob);
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

            // Looting check
            int lootingLvl = 0;
            if (damageSource.getAttacker() instanceof LivingEntity attacker) {
                ItemStack weapon = attacker.getMainHandStack();
                RegistryEntry<Enchantment> loot = world.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
                lootingLvl = EnchantmentHelper.getLevel(loot, weapon);
            }

            dropCount += lootingLvl;

            if (mob instanceof CowEntity cow) {
                if (cow.getDataTracker().get(IS_COW_BUTCHERED))
                    goodieStack = new ItemStack(Items.BONE, dropCount);
                else if (cow.getDataTracker().get(IS_COW_SHEARED) && (damageSource.isIn(DamageTypeTags.IS_FIRE) || cow.isOnFire() || killedByFireAspect))
                    goodieStack = new ItemStack(Items.COOKED_BEEF, dropCount);
                else if (cow.getDataTracker().get(IS_COW_SHEARED))
                    goodieStack = new ItemStack(Items.BEEF, dropCount);
                else return;
            } else if (mob instanceof ChickenEntity chick) {
                if (chick.getDataTracker().get(IS_CHICK_BUTCHERED))
                    goodieStack = new ItemStack(Items.BONE, dropCount);
                else if (chick.getDataTracker().get(IS_CHICK_SHEARED) && (damageSource.isIn(DamageTypeTags.IS_FIRE) || chick.isOnFire() || killedByFireAspect))
                    goodieStack = new ItemStack(Items.COOKED_CHICKEN, 1 + lootingLvl);
                else if (chick.getDataTracker().get(IS_CHICK_SHEARED))
                    goodieStack = new ItemStack(Items.CHICKEN, 1 + lootingLvl);
                else return;
            } else if (mob instanceof PigEntity pig) {
                if (pig.getDataTracker().get(IS_PIG_BUTCHERED))
                    goodieStack = new ItemStack(Items.BONE, dropCount);
                else return;
            } else if (mob instanceof SheepEntity sheep) {
                if (sheep.getDataTracker().get(IS_SHEEP_BUTCHERED))
                    goodieStack = new ItemStack(Items.BONE, dropCount);
                else return;
            } else return;

            ItemEntity drop = new ItemEntity(world, mob.getX(), mob.getY() + 1, mob.getZ(), goodieStack);
            world.spawnEntity(drop);
            drop.setVelocity(drop.getVelocity().add((mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.1F, mob.getRandom().nextFloat() * 0.05F, (mob.getRandom().nextFloat() - mob.getRandom().nextFloat()) * 0.1F));
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

