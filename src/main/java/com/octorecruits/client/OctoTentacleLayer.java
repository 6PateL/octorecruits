package com.octorecruits.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.octorecruits.OctoConfig;
import dev.tentaworks.client.TentacleRenderer;
import dev.tentaworks.rig.TentacleRigItem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * Draws the Octo Arms tentacles on a recruit wearing the rig, with the same renderer the mod uses for the player.
 * The arms stay folded on the back; when the recruit is off the ground next to a wall they unfold and reach for it.
 * Client side only, a few ray casts per frame for a recruit that wears a rig, nothing for everybody else.
 */
public class OctoTentacleLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final Map<Integer, float[]> STATE = new HashMap<>(); // id -> {deploy, lastAge}

    public OctoTentacleLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack ps, MultiBufferSource buffers, int light, T entity, float limbSwing, float limbSwingAmount,
                       float partialTick, float age, float netHeadYaw, float headPitch) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!OctoConfig.ENABLED.get() || !(chest.getItem() instanceof TentacleRigItem rig)
                || !(getParentModel() instanceof HumanoidModel<?> model) || entity.isInvisible()) {
            return;
        }
        float yaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        Vec3 at = entity.getPosition(partialTick);

        // Body space for the tips: the same chain the renderer built for the model, then into the rig's own space.
        PoseStack chain = new PoseStack();
        chain.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        chain.scale(-1.0F, -1.0F, 1.0F);
        chain.translate(0.0D, -1.501D, 0.0D);
        model.body.translateAndRotate(chain);
        TentacleRenderer.toTankSpace(chain);
        Matrix4f toTank = new Matrix4f(chain.last().pose()).invert();
        Matrix4f fromTank = chain.last().pose();

        double rad = Math.toRadians(yaw);
        Vec3 forward = new Vec3(-Math.sin(rad), 0.0D, Math.cos(rad));
        Vector3f[] tips = new Vector3f[4];
        Vector3f[] normals = new Vector3f[4];
        float[] attach = new float[4];
        boolean anyWall = false;
        boolean airborne = !entity.onGround() && !entity.isInWaterOrBubble();
        if (airborne) {
            for (int i = 0; i < 4; i++) {
                Vector3f side = fromTank.transformDirection((i & 1) == 0 ? -1.0F : 1.0F, 0.0F, 0.0F, new Vector3f());
                Vec3 lateral = new Vec3(side.x, 0.0D, side.z).normalize();
                double row = i < 2 ? 0.55D : 0.0D;
                double sway = Math.sin(age * 0.35D + i * 1.6D) * 0.3D;
                Vec3 from = at.add(0.0D, 0.9D + row, 0.0D);
                Vec3 to = from.add(forward.scale(1.3D)).add(lateral.scale(0.45D)).add(0.0D, sway - 0.1D, 0.0D);
                BlockHitResult hit = entity.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, entity));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    Vec3 tip = hit.getLocation().subtract(at);
                    Vec3 n = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
                    tips[i] = toTank.transformPosition((float) tip.x, (float) tip.y, (float) tip.z, new Vector3f());
                    normals[i] = toTank.transformDirection((float) n.x, (float) n.y, (float) n.z, new Vector3f()).normalize();
                    attach[i] = 1.0F;
                    anyWall = true;
                }
            }
        }

        float[] st = STATE.computeIfAbsent(entity.getId(), k -> new float[] {0.0F, age});
        float dt = Mth.clamp(age - st[1], 0.0F, 5.0F);
        st[1] = age;
        float target = anyWall ? 1.0F : 0.0F;
        st[0] += Mth.clamp(target - st[0], -dt * 0.15F, dt * 0.15F);
        if (STATE.size() > 128) {
            STATE.clear();
        }

        ps.pushPose();
        model.body.translateAndRotate(ps);
        TentacleRenderer.toTankSpace(ps);
        TentacleRenderer.drawPosed(ps, buffers, rig.tier(), tips, normals, attach, st[0], age, light);
        ps.popPose();
    }
}
