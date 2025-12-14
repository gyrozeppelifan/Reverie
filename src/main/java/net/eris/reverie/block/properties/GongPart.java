package net.eris.reverie.block.properties;

import net.minecraft.util.StringRepresentable;

public enum GongPart implements StringRepresentable {
    TOP_LEFT("top_left"),
    TOP_RIGHT("top_right"),
    BOTTOM_LEFT("bottom_left"),
    BOTTOM_RIGHT("bottom_right");

    private final String name;

    GongPart(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}