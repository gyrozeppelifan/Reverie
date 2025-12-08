package net.eris.reverie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.entity.StitchedEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT)
public class ReverieDebugHandler {

    // Serverdan gelen yolları burada saklıyoruz: Map<EntityID, List<BlockPos>>
    private static final Map<Integer, List<BlockPos>> debugPaths = new HashMap<>();

    // Paket geldiğinde bu metod çalışır ve yolu günceller
    public static void updatePath(int entityId, List<BlockPos> path) {
        if (path == null || path.isEmpty()) {
            debugPaths.remove(entityId);
        } else {
            debugPaths.put(entityId, path);
        }
    }

    private static boolean isDebugging(Player player) {
        return player.isCreative() &&
                (player.getMainHandItem().is(Items.AMETHYST_SHARD) || player.getOffhandItem().is(Items.AMETHYST_SHARD));
    }

    // --- 1. DÜNYA RENDER (Yollar ve Çizgiler) ---
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !isDebugging(player)) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines()); // Özel hat çizimi

        Vec3 cameraPos = event.getCamera().getPosition();

        // Çizgileri kalınlaştır
        RenderSystem.lineWidth(4.0F);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof StitchedEntity stitched) {
                // Kayıtlı yolu çiz (Varsa)
                if (debugPaths.containsKey(stitched.getId())) {
                    renderSavedPath(poseStack, buffer, cameraPos, debugPaths.get(stitched.getId()));
                }

                renderTargetLine(stitched, poseStack, buffer, cameraPos);
                renderAttackRange(stitched, poseStack, buffer, cameraPos);
            }
        }

        bufferSource.endBatch(RenderType.lines());
        RenderSystem.lineWidth(1.0F);
    }

    // Serverdan gelen BlockPos listesini çizgi olarak çiz
    private static void renderSavedPath(PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos, List<BlockPos> pathPoints) {
        if (pathPoints.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();

        for (int i = 0; i < pathPoints.size() - 1; i++) {
            BlockPos p1 = pathPoints.get(i);
            BlockPos p2 = pathPoints.get(i + 1);

            // Yeşil, Kalın Çizgi
            // +0.5 ile bloğun merkezinden merkeze çiziyoruz
            buffer.vertex(matrix, p1.getX() + 0.5f, p1.getY() + 0.5f, p1.getZ() + 0.5f)
                    .color(0, 255, 0, 255).normal(0, 1, 0).endVertex();
            buffer.vertex(matrix, p2.getX() + 0.5f, p2.getY() + 0.5f, p2.getZ() + 0.5f)
                    .color(0, 255, 0, 255).normal(0, 1, 0).endVertex();
        }
        poseStack.popPose();
    }

    private static void renderTargetLine(StitchedEntity entity, PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos) {
        if (entity.getTarget() == null) return;
        Entity target = entity.getTarget();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();

        // Kırmızı Çizgi (Hedef)
        buffer.vertex(matrix, (float)entity.getX(), (float)entity.getEyeY(), (float)entity.getZ())
                .color(255, 0, 0, 255).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix, (float)target.getX(), (float)target.getEyeY(), (float)target.getZ())
                .color(255, 0, 0, 255).normal(0, 1, 0).endVertex();

        poseStack.popPose();
    }

    private static void renderAttackRange(StitchedEntity entity, PoseStack poseStack, VertexConsumer buffer, Vec3 cameraPos) {
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x + entity.getX(), -cameraPos.y + entity.getY(), -cameraPos.z + entity.getZ());
        Matrix4f matrix = poseStack.last().pose();
        float range = 2.5f;

        for (int i = 0; i <= 360; i += 10) {
            float x1 = range * Mth.sin(i * Mth.DEG_TO_RAD);
            float z1 = range * Mth.cos(i * Mth.DEG_TO_RAD);
            float x2 = range * Mth.sin((i + 10) * Mth.DEG_TO_RAD);
            float z2 = range * Mth.cos((i + 10) * Mth.DEG_TO_RAD);

            buffer.vertex(matrix, x1, 0.1f, z1).color(255, 50, 50, 255).normal(0, 1, 0).endVertex();
            buffer.vertex(matrix, x2, 0.1f, z2).color(255, 50, 50, 255).normal(0, 1, 0).endVertex();
        }
        poseStack.popPose();
    }

    // --- 2. KAFA ÜSTÜ BİLGİLERİ ---
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof StitchedEntity stitched)) return;
        Player player = Minecraft.getInstance().player;
        if (player == null || !isDebugging(player)) return;

        event.setResult(net.minecraftforge.eventbus.api.Event.Result.DENY);

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0, stitched.getBbHeight() + 0.75, 0);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        int y = 0;

        drawSafeText(poseStack, "State: " + stitched.getState(), y, 0x00FF00); y -= 10;
        drawSafeText(poseStack, "HP: " + (int)stitched.getHealth() + "/" + (int)stitched.getMaxHealth(), y, 0xFF0000); y -= 10;

        String ownerInfo = stitched.isTame() ? (stitched.getOwnerUUID() != null ? "Owned" : "Tamed (No Owner!)") : "Wild";
        drawSafeText(poseStack, "[" + ownerInfo + "]", y, stitched.isTame() ? 0x55FFFF : 0xFFA500); y -= 10;

        String targetName = stitched.getTarget() != null ? stitched.getTarget().getName().getString() : "None";
        drawSafeText(poseStack, "Target: " + targetName, y, 0xFFFF00); y -= 10;

        // Path Durumu (Client tarafında map'ten kontrol et)
        boolean hasPath = debugPaths.containsKey(stitched.getId());
        drawSafeText(poseStack, "Path: " + (hasPath ? "TRACKING" : "NO PATH"), y, hasPath ? 0x00FF00 : 0xFF0000); y -= 10;

        poseStack.popPose();
    }

    private static void drawSafeText(PoseStack poseStack, String text, int y, int color) {
        var font = Minecraft.getInstance().font;
        float x = -font.width(text) / 2.0f;
        int opacity = (int)(0.4f * 255.0f) << 24;
        font.drawInBatch(text, x, y, color, true, poseStack.last().pose(),
                Minecraft.getInstance().renderBuffers().bufferSource(),
                net.minecraft.client.gui.Font.DisplayMode.SEE_THROUGH, opacity, 15728880);
    }

    // NBT Röntgeni
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide && isDebugging(event.getEntity()) && event.getTarget() instanceof StitchedEntity stitched) {
            CompoundTag nbt = new CompoundTag();
            stitched.saveWithoutId(nbt);
            ReverieMod.LOGGER.info("STITCHED NBT: " + nbt);
            event.getEntity().sendSystemMessage(Component.literal("§dNBT Loglara yazildi."));
            event.setCanceled(true);
        }
    }
}