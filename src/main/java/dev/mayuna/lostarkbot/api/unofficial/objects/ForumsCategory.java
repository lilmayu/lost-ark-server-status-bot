package dev.mayuna.lostarkbot.api.unofficial.objects;

import lombok.Getter;

public enum ForumsCategory {
    MAINTENANCE("Maintenance"),
    DOWNTIME("Downtime");

    private final @Getter String id;

    ForumsCategory(String id) {
        this.id = id;
    }

    public static ForumsCategory fromString(String string) {
        for (ForumsCategory forumsCategory : ForumsCategory.values()) {
            if (forumsCategory.name().equalsIgnoreCase(string)) {
                return forumsCategory;
            } else if (forumsCategory.id.equalsIgnoreCase(string)) {
                return forumsCategory;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        switch (this) {
            case MAINTENANCE -> {
                return "Maintenance";
            }
            case DOWNTIME -> {
                return "Downtime";
            }
        }

        return "Unknown";
    }
}
