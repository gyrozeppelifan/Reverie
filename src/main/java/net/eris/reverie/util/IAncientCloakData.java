package net.eris.reverie.util;

public interface IAncientCloakData {
    // Ancient Cloak (Zaten vardı)
    boolean reverie$hasAncientCloak();
    void reverie$setAncientCloak(boolean hasCloak);

    // YENİ: Drunken Rage (Bunu ekliyoruz)
    boolean reverie$hasDrunkenRage();
    void reverie$setDrunkenRage(boolean hasRage);

    // YENİ: Zapped (Çarpılma) Verisi
    boolean reverie$hasZapped();
    void reverie$setZapped(boolean hasZapped);
}
