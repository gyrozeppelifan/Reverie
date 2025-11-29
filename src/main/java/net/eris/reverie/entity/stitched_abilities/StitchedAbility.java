package net.eris.reverie.entity.stitched_abilities;

import net.eris.reverie.entity.StitchedEntity;

public interface StitchedAbility {
    // Yetenek başladığında (Kumandaya basınca) bir kere çalışır
    void start(StitchedEntity entity);

    // Yetenek devam ederken her tick (saniyenin 20'de 1'i) çalışır
    // Geriye 'true' dönerse yetenek devam ediyor demektir, 'false' dönerse biter.
    boolean tick(StitchedEntity entity);

    // Yetenek bittiğinde veya iptal edildiğinde çalışır (Temizlik için)
    void stop(StitchedEntity entity);

    // Bu yetenek şu an kullanılabilir mi? (Cooldown, hedef kontrolü vb.)
    boolean canUse(StitchedEntity entity);

    // Yeteneğin süresi (Tick cinsinden)
    int getDuration();
}