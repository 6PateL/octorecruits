package com.octorecruits;

import net.minecraftforge.common.ForgeConfigSpec;

public class OctoConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLED;
    public static final ForgeConfigSpec.DoubleValue CLIMB_SPEED;
    public static final ForgeConfigSpec.IntValue CHASE_RANGE;
    public static final ForgeConfigSpec.BooleanValue USE_AIR;
    public static final ForgeConfigSpec.DoubleValue AIR_PER_SECOND;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("rig");
        ENABLED = b.comment("Recruits wearing a Tentacle Rig (Create: Octo Arms) in the chest slot climb walls.")
                .define("enabled", true);
        CLIMB_SPEED = b.comment("Climb speed in blocks per tick (a spider climbs at 0.2).")
                .defineInRange("climbSpeed", 0.2D, 0.05D, 0.6D);
        CHASE_RANGE = b.comment("A recruit with a rig walks straight at an enemy up to this far away when it has no ordinary path to it (it is on a roof, a cliff...). 0 turns it off.")
                .defineInRange("chaseRange", 24, 0, 64);
        USE_AIR = b.comment("Climbing uses the air of the worn rig (the rig is a backtank). Off by default, because recruits cannot refill it.")
                .define("useAir", false);
        AIR_PER_SECOND = b.comment("Air used per second of climbing when useAir is on.")
                .defineInRange("airPerSecond", 5.0D, 0.0D, 100.0D);
        b.pop();
        SPEC = b.build();
    }
}
