package com.octorecruits;

import com.simibubi.create.content.equipment.armor.BacktankItem;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;

/**
 * A recruit wearing the Octo Arms Tentacle Rig climbs walls like a spider. No use button and no per-tick searching:
 * the goal only looks at a few fields of the recruit (is it pressed against a wall while moving?) and, when it has an
 * enemy on higher ground it cannot path to, walks straight at it so that the wall stops it and the climb begins.
 */
public class OctoClimbGoal extends Goal {
    private static final double GRAVITY_COMPENSATION = 0.0784D;

    private final AbstractRecruitEntity mob;
    private Item checkedItem;
    private boolean checkedRig;
    private int climbTicks;
    private int chaseTicks;
    private int chaseCooldownUntil;
    private int losCheckedAt = -100;
    private boolean losCached;

    public OctoClimbGoal(AbstractRecruitEntity mob) {
        this.mob = mob;
        // No flags: this goal never blocks the recruit's own goals (attacking, following, holding position...).
        this.setFlags(EnumSet.noneOf(Flag.class));
    }

    private boolean wearsRig() {
        ItemStack stack = mob.getItemBySlot(EquipmentSlot.CHEST);
        Item item = stack.getItem();
        if (item != checkedItem) {
            checkedItem = item;
            checkedRig = item instanceof BacktankItem
                    && ForgeRegistries.ITEMS.getKey(item) != null
                    && "tentaworks".equals(ForgeRegistries.ITEMS.getKey(item).getNamespace());
        }
        if (!checkedRig) {
            return false;
        }
        return !OctoConfig.USE_AIR.get() || BacktankUtil.getAir(stack) > 0.0F;
    }

    private boolean wantsToMove() {
        return mob.getNavigation().isInProgress();
    }

    private boolean pressedAgainstWall() {
        if (!mob.horizontalCollision || mob.isPassenger() || mob.isInWaterOrBubble() || mob.isSleeping()) {
            return false;
        }
        if (wantsToMove()) {
            BlockPos goal = mob.getNavigation().getTargetPos();
            // Do not climb up when the route leads down.
            return goal == null || goal.getY() >= mob.getBlockY() - 1;
        }
        return chaseTarget() != null;
    }

    /** An enemy above us that the ground pathfinder cannot reach: go straight at it. */
    private LivingEntity chaseTarget() {
        int range = OctoConfig.CHASE_RANGE.get();
        LivingEntity target = mob.getTarget();
        if (range <= 0 || target == null || !target.isAlive() || mob.getNavigation().isInProgress()) {
            return null;
        }
        double dy = target.getY() - mob.getY();
        if (dy < 1.5D || dy > 20.0D || mob.distanceToSqr(target) > (double) range * range) {
            return null;
        }
        if (mob.tickCount < chaseCooldownUntil) {
            return null;
        }
        // Only an enemy the recruit can actually see: never climb towards something behind a floor or a wall.
        if (mob.tickCount - losCheckedAt >= 10) {
            losCheckedAt = mob.tickCount;
            losCached = mob.hasLineOfSight(target);
        }
        return losCached ? target : null;
    }

    @Override
    public boolean canUse() {
        if (!OctoConfig.ENABLED.get() || !wearsRig()) {
            return false;
        }
        return pressedAgainstWall() || chaseTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void stop() {
        climbTicks = 0;
        chaseTicks = 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = chaseTarget();
        if (target != null) {
            mob.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
            if (++chaseTicks > 200) {
                // Could not get there in 10 seconds: give up for a while instead of pressing at a wall forever.
                chaseTicks = 0;
                chaseCooldownUntil = mob.tickCount + 200;
            }
        } else {
            chaseTicks = 0;
        }
        if (!mob.horizontalCollision) {
            return;
        }
        Vec3 d = mob.getDeltaMovement();
        double up = OctoConfig.CLIMB_SPEED.get() + GRAVITY_COMPENSATION;
        if (d.y < up) {
            mob.setDeltaMovement(d.x, up, d.z);
        }
        mob.fallDistance = 0.0F;
        if (OctoConfig.USE_AIR.get() && ++climbTicks >= 20) {
            climbTicks = 0;
            drainAir();
        }
    }

    private void drainAir() {
        ItemStack stack = mob.getItemBySlot(EquipmentSlot.CHEST);
        float air = BacktankUtil.getAir(stack);
        float left = Math.max(0.0F, air - OctoConfig.AIR_PER_SECOND.get().floatValue());
        stack.getOrCreateTag().putFloat("Air", left);
    }
}
