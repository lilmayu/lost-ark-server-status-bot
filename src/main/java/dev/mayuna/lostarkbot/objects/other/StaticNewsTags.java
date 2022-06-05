package dev.mayuna.lostarkbot.objects.other;

import lombok.Getter;

public enum StaticNewsTags {
    ACADEMY("Academy"),
    EVENTS("Events"),
    GENERAL("General"),
    RELEASE_NOTES("Release Notes"),
    SHOWCASE("Showcase"),
    UPDATES("Updates");

    private final @Getter String displayName;

    StaticNewsTags(String displayName) {
        this.displayName = displayName;
    }

    public static StaticNewsTags get(String string) {
        for (StaticNewsTags newsTag : StaticNewsTags.values()) {
            if (newsTag.name().equalsIgnoreCase(string) || newsTag.getDisplayName().equalsIgnoreCase(string)) {
                return newsTag;
            }
        }

        return null;
    }
}
