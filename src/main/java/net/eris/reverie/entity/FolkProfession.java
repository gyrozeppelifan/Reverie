package net.eris.reverie.entity;

public enum FolkProfession {
    UNEMPLOYED(0), BARKEEPER(1), GUNSMITH(2), BANKER(3),
    STABLE_MASTER(4), BOUNTY_CLERK(5), TAILOR(6);
    public final int id;
    FolkProfession(int id) { this.id = id; }
}