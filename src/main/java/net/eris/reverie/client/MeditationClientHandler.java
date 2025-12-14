package net.eris.reverie.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.eris.reverie.ReverieMod;
import net.eris.reverie.capability.MeditationProvider;
import net.eris.reverie.client.model.SpiritOrbModel;
import net.eris.reverie.init.ReverieModItems;
import net.eris.reverie.init.ReverieModSounds;
import net.eris.reverie.network.packet.ServerboundToggleMeditationPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import com.mojang.math.Axis;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = ReverieMod.MODID, value = Dist.CLIENT)
public class MeditationClientHandler {

    public static final KeyMapping MEDITATION_KEY = new KeyMapping("key.reverie.meditate", GLFW.GLFW_KEY_M, "key.categories.reverie");

    private static boolean isAudioMuffled = false;
    private static final Map<SoundSource, Double> originalVolumes = new HashMap<>();
    private static SimpleSoundInstance activeAmbience = null;

    private static SpiritOrbModel cachedOrbModel;
    private static final ResourceLocation ORB_TEXTURE = new ResourceLocation(ReverieMod.MODID, "textures/particle/spirit_orb.png");

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (MEDITATION_KEY.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;

            boolean isMeditating = player.getCapability(MeditationProvider.PLAYER_MEDITATION)
                    .map(MeditationProvider.PlayerMeditation::isMeditating).orElse(false);

            if (isMeditating) {
                ReverieMod.PACKET_HANDLER.sendToServer(new ServerboundToggleMeditationPacket());
                updateClientState(player, false);
            } else {
                boolean hasSeal = player.getMainHandItem().is(ReverieModItems.BOAR_MONK_SEAL.get()) ||
                        player.getOffhandItem().is(ReverieModItems.BOAR_MONK_SEAL.get());

                if (hasSeal) {
                    ReverieMod.PACKET_HANDLER.sendToServer(new ServerboundToggleMeditationPacket());
                    updateClientState(player, true);
                } else {
                    player.displayClientMessage(Component.translatable("reverie.message.meditation_need_seal"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
            if (cap.isMeditating()) {
                PoseStack poseStack = event.getPoseStack();
                float time = (float)player.tickCount + event.getPartialTick();

                float bobOffset = Mth.sin(time * 0.08F) * 0.1F;
                poseStack.translate(0, bobOffset, 0);

                renderSpiritOrbs(poseStack, event.getMultiBufferSource(), event.getPackedLight(), time, player, event.getPartialTick());
            }
        });
    }

    public static void renderSpiritOrbs(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float time, Player player, float partialTicks) {
        if (cachedOrbModel == null) {
            cachedOrbModel = new SpiritOrbModel(Minecraft.getInstance().getEntityModels().bakeLayer(SpiritOrbModel.LAYER_LOCATION));
        }

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(ORB_TEXTURE));

        int orbCount = 6;
        float radius = 1.3F;
        float speed = 0.05F;

        // DRIFT FIX (Ters Rotasyon)
        float bodyRot = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot);
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(bodyRot - 180.0F));

        for (int i = 0; i < orbCount; i++) {
            poseStack.pushPose();

            float angle = (time * speed) + (i * ((float)Math.PI * 2 / orbCount));
            float x = Mth.cos(angle) * radius;
            float z = Mth.sin(angle) * radius;

            // --- AYARLANDI: DAHA YUKARIDA (1.7F) ---
            float y = 1.7F + Mth.sin((time * 0.1F) + i) * 0.25F;

            poseStack.translate(x, y, z);

            // --- AYARLANDI: DAHA BÜYÜK (1.0F) ---
            float scale = 1.0F;
            poseStack.scale(scale, -scale, scale);

            float selfRot = time * 4.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(selfRot));

            cachedOrbModel.renderToBuffer(poseStack, vertexConsumer, 15728880, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
            if (cap.isMeditating()) {
                spawnAmbientParticles(player);

                player.setNoGravity(true);
                double targetY = cap.getOriginY() + 3.0;
                if (player.getY() < targetY) {
                    player.setDeltaMovement(0, 0.08, 0);
                } else {
                    player.setDeltaMovement(0, 0, 0);
                    if (player.getY() > targetY + 0.1) player.setPos(player.getX(), targetY, player.getZ());
                }

                if (!isAudioMuffled) muffleAudio(mc);
                if (activeAmbience == null || !mc.getSoundManager().isActive(activeAmbience)) {
                    activeAmbience = SimpleSoundInstance.forLocalAmbience(ReverieModSounds.MEDITATION_AMBIANCE.get(), 1.0F, 1.0F);
                    mc.getSoundManager().play(activeAmbience);
                }
            } else {
                if (player.isNoGravity() && !player.getAbilities().flying) player.setNoGravity(false);
                if (isAudioMuffled) {
                    unmuffleAudio(mc);
                    if (activeAmbience != null) {
                        mc.getSoundManager().stop(activeAmbience);
                        activeAmbience = null;
                    }
                }
            }
        });
    }

    private static void spawnAmbientParticles(LocalPlayer player) {
        RandomSource r = player.getRandom();
        if (player.getDeltaMovement().y > 0.01) {
            for (int i = 0; i < 2; i++) {
                player.level().addParticle(ParticleTypes.CLOUD, player.getX() + (r.nextDouble() - 0.5)*0.5, player.getY() - 0.2, player.getZ() + (r.nextDouble() - 0.5)*0.5, 0, -0.05, 0);
            }
        }
        if (r.nextFloat() < 0.1F) {
            player.level().addParticle(ParticleTypes.ENCHANT, player.getX() + (r.nextDouble()-0.5)*2.0, player.getY()+r.nextDouble()*2.0, player.getZ()+(r.nextDouble()-0.5)*2.0, 0, 0.05, 0);
        }
    }

    private static void updateClientState(Player player, boolean meditating) {
        player.getCapability(MeditationProvider.PLAYER_MEDITATION).ifPresent(cap -> {
            cap.setMeditating(meditating);
            if (meditating) cap.setOriginY(player.getY());
        });
    }

    private static void muffleAudio(Minecraft mc) {
        originalVolumes.clear();
        for (SoundSource source : SoundSource.values()) {
            originalVolumes.put(source, mc.options.getSoundSourceOptionInstance(source).get());
        }
        for (SoundSource source : SoundSource.values()) {
            if (source != SoundSource.MASTER && source != SoundSource.AMBIENT && source != SoundSource.RECORDS && source != SoundSource.VOICE) {
                mc.options.getSoundSourceOptionInstance(source).set(0.05);
            }
        }
        isAudioMuffled = true;
    }

    private static void unmuffleAudio(Minecraft mc) {
        for (Map.Entry<SoundSource, Double> entry : originalVolumes.entrySet()) {
            mc.options.getSoundSourceOptionInstance(entry.getKey()).set(entry.getValue());
        }
        isAudioMuffled = false;
    }
}