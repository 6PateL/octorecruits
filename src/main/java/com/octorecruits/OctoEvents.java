package com.octorecruits;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OctoEvents {
    @SubscribeEvent
    public void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractRecruitEntity recruit)) {
            return;
        }
        for (WrappedGoal g : recruit.goalSelector.getAvailableGoals()) {
            if (g.getGoal() instanceof OctoClimbGoal) {
                return;
            }
        }
        recruit.goalSelector.addGoal(1, new OctoClimbGoal(recruit));
    }

    /** The tentacles catch the fall: a recruit wearing the rig takes no fall damage. */
    @SubscribeEvent
    public void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof AbstractRecruitEntity recruit && OctoConfig.NO_FALL_DAMAGE.get()
                && OctoClimbGoal.wearsRig(recruit)) {
            event.setDamageMultiplier(0.0F);
        }
    }
}
