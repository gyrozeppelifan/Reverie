package net.eris.reverie.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.eris.reverie.events.BoarMonkEvents;
import net.minecraft.client.model.PigModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.Pig;
import org.joml.Matrix4f;

public class PigSpiritualBeamLayer extends RenderLayer<Pig, PigModel<Pig>> {

    public PigSpiritualBeamLayer(RenderLayerParent<Pig, PigModel<Pig>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Pig pig, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Dönüşüm Tag'i yoksa çizme
        if (!pig.getPersistentData().contains(BoarMonkEvents.TAG_TRANSFORMING)) return;

        int timer = pig.getPersistentData().getInt(BoarMonkEvents.TAG_TRANSFORMING);

        // Son 100 tick kala başla (Patlamaya doğru)
        if (timer < 100 && timer > 0) {

            // Animasyon ilerlemesi (0.0 -> 1.0)
            float progress = (100 - timer) / 100.0F;

            // Işın Sayısı: Zamanla artar (Patlamaya yakın 60 tane olur)
            int beamCount = (int)(progress * 60) + 5;

            // Render Type: LIGHTNING (Yıldırım gibi parlar, texture istemez, saf renk basar)
            VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

            RandomSource random = RandomSource.create(432L); // Sabit seed (Titremeyi kontrol etmek için)

            poseStack.pushPose();
            poseStack.translate(0.0D, -0.5D, 0.0D); // Domuzun gövde merkezi

            for (int i = 0; i < beamCount; i++) {
                poseStack.pushPose();

                // --- RASTGELE YÖN ---
                // Her frame'de biraz değişsin diye timer ekliyoruz
                poseStack.mulPose(Axis.XP.rotationDegrees(random.nextFloat() * 360.0F + (ageInTicks * 2)));
                poseStack.mulPose(Axis.YP.rotationDegrees(random.nextFloat() * 360.0F + (ageInTicks * 2)));
                poseStack.mulPose(Axis.ZP.rotationDegrees(random.nextFloat() * 360.0F + (ageInTicks * 2)));

                // --- UZUNLUK VE GENİŞLİK ---
                // Zamanla uzasınlar
                float length = random.nextFloat() * 10.0F * progress + 2.0F;
                float width = random.nextFloat() * 0.5F * progress + 0.1F;

                Matrix4f matrix = poseStack.last().pose();

                // --- RENK PALETİ (Spiritual: Camgöbeği/Mor/Beyaz) ---
                // Rastgele renk seçimi
                float r, g, b, a;
                if (random.nextBoolean()) {
                    // Camgöbeği (Cyan) Enerji
                    r = 0.0F; g = 1.0F; b = 1.0F; a = 0.6F;
                } else {
                    // Mor (Purple) Enerji
                    r = 0.8F; g = 0.2F; b = 1.0F; a = 0.6F;
                }

                // Merkeze yaklaştıkça beyazlaşsın
                if (random.nextFloat() < 0.2f) {
                    r = 1.0F; g = 1.0F; b = 1.0F; a = 0.8F;
                }

                // --- ÜÇGEN (MIZRAK) ÇİZİMİ ---
                // 3 Nokta: (0,0) -> (Genişlik, Uzunluk) -> (-Genişlik, Uzunluk)
                // Bu bir "V" şekli veya üçgen oluşturur.

                // Merkez Nokta (Domuzun içi)
                addVertex(consumer, matrix, 0.0F, 0.0F, r, g, b, a);

                // Uç Nokta 1
                addVertex(consumer, matrix, -width, length, r, g, b, 0.0F); // Uçlarda alpha 0 (fade out)

                // Uç Nokta 2
                addVertex(consumer, matrix, width, length, r, g, b, 0.0F); // Uçlarda alpha 0

                // Ters yüzünü de çizelim ki her açıdan görünsün (Lightning culling yapmaz ama garanti olsun)
                addVertex(consumer, matrix, 0.0F, 0.0F, r, g, b, a);
                addVertex(consumer, matrix, width, length, r, g, b, 0.0F);
                addVertex(consumer, matrix, -width, length, r, g, b, 0.0F);

                poseStack.popPose();
            }

            poseStack.popPose();
        }
    }

    // Vertex Çizici Yardımcı
    private static void addVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float r, float g, float b, float a) {
        consumer.vertex(matrix, x, y, 0.0F) // Z hep 0, çünkü poseStack ile döndürüyoruz
                .color(r, g, b, a)
                .uv2(15728880) // Full Bright (Karanlıkta Parlar)
                .endVertex();
    }
}