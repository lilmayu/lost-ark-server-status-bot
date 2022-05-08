package dev.mayuna.lostarkbot.objects.other;

import dev.mayuna.lostarkbot.util.logging.Logger;
import lombok.Getter;

public enum LostArkRegion {

    WEST_NORTH_AMERICA("West North America"),
    EAST_NORTH_AMERICA("East North America"),
    CENTRAL_EUROPE("Central Europe"),
    SOUTH_AMERICA("South America"),
    EUROPE_WEST("Europe West");

    private final @Getter String formattedName;

    LostArkRegion(String formattedName) {
        this.formattedName = formattedName;
    }

    public static String getCorrect(String string) {
        for (LostArkRegion region : values()) {
            if (region.getFormattedName().equalsIgnoreCase(string) || region.name().equalsIgnoreCase(string)) {
                return region.name();
            }
        }

        return null;
    }

    public static String getCorrectFormatted(String string) {
        for (LostArkRegion region : values()) {
            if (region.getFormattedName().equalsIgnoreCase(string) || region.name().equalsIgnoreCase(string)) {
                return region.getFormattedName();
            }
        }

        return null;
    }
}
