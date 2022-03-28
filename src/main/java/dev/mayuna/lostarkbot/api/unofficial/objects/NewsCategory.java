package dev.mayuna.lostarkbot.api.unofficial.objects;

import lombok.Getter;

public enum NewsCategory {
    UPDATES("updates"),
    EVENTS("events"),
    RELEASE_NOTES("release-notes"),
    GENERAL("general");

    private final @Getter String id;

    NewsCategory(String id) {
        this.id = id;
    }

    public static NewsCategory fromString(String string) {
        for (NewsCategory newsCategory : NewsCategory.values()) {
            if (newsCategory.name().equalsIgnoreCase(string)) {
                return newsCategory;
            } else if (newsCategory.id.equalsIgnoreCase(string)) {
                return newsCategory;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        switch (this) {
            case UPDATES -> {
                return "Updates";
            }
            case EVENTS -> {
                return "Events";
            }
            case RELEASE_NOTES -> {
                return "Release notes";
            }
            case GENERAL -> {
                return "General";
            }
        }

        return "Unknown";
    }
}
