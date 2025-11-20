package net.eris.reverie.item;

import net.eris.reverie.entity.GoblinFlagEntity;
import net.eris.reverie.init.ReverieModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GoblinFlagItem extends Item {

    public GoblinFlagItem() {
        super(new Item.Properties().stacksTo(1));
    }

    // "use" yerine "useOn" kullanıyoruz. Bu, bir bloğa tıklandığında çalışır.
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        // Client tarafında sadece animasyon oynat, işlemi Server yapsın
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        // 1. Tıklanan bloğun konumunu al
        BlockPos clickedPos = context.getClickedPos();
        // 2. Tıklanan yüzü al (Örn: Bloğun üstüne tıkladıysan UP, yanına tıkladıysan NORTH vs.)
        Direction clickedFace = context.getClickedFace();

        // 3. Spawn konumu: Tıklanan bloğun yanı/üstü
        BlockPos spawnPos = clickedPos.relative(clickedFace);

        // 4. Entity'yi tam merkeze hizalamak için +0.5 ekliyoruz
        double x = spawnPos.getX() + 0.5;
        double y = spawnPos.getY(); // Y zaten tabanda olmalı
        double z = spawnPos.getZ() + 0.5;

        // Çakışma kontrolü (İsteğe bağlı ama iyi pratik)
        AABB box = new AABB(x - 0.5, y, z - 0.5, x + 0.5, y + 2, z + 0.5);
        if (!level.getEntitiesOfClass(GoblinFlagEntity.class, box).isEmpty()) {
            return InteractionResult.FAIL;
        }

        // Bayrağı oluştur
        GoblinFlagEntity flag = new GoblinFlagEntity(ReverieModEntities.GOBLIN_FLAG.get(), level);

        // Konumu ayarla
        flag.setPos(x, y, z);

        // Rotasyonu ayarla (Oyuncuya baksın)
        // context.getRotation() oyuncunun baktığı yönü verir (0, 90, 180, 270 gibi)
        float yaw = context.getHorizontalDirection().toYRot();
        // Veya oyuncunun tam dönüş açısı olsun istersen:
        flag.setYRot(player.getYRot());

        // Owner ayarla
        flag.setOwnerUUID(player.getUUID());
        flag.setOwnerName(player.getName().getString());

        // Dünyaya ekle
        level.addFreshEntity(flag);

        // Item'ı eksilt (Creative değilse)
        if (!player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        // Cooldown ekle (Opsiyonel)
        player.getCooldowns().addCooldown(this, 40);

        return InteractionResult.CONSUME; // Başarılı işlem
    }
}