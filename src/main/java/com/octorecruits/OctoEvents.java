package com.octorecruits;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
}
