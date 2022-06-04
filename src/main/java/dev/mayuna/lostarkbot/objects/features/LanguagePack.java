package dev.mayuna.lostarkbot.objects.features;

import com.google.gson.annotations.Expose;
import dev.mayuna.lostarkfetcher.objects.api.other.LostArkRegion;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.stream.Stream;

public class LanguagePack {

    private @Getter @Setter @Expose String langCode; // cz
    private @Getter @Setter @Expose String langName; // Čeština

    private @Getter @Setter @Expose String title; // Lost Ark - Server Dashboard
    private @Getter @Setter @Expose String currentPlayers; // Currently {players} in-game players
    private @Getter @Setter @Expose String online;
    private @Getter @Setter @Expose String busy;
    private @Getter @Setter @Expose String full;
    private @Getter @Setter @Expose String maintenance;

    private @Getter @Setter @Expose String centralEurope;
    private @Getter @Setter @Expose String europeWest;
    private @Getter @Setter @Expose String eastNorthAmerica;
    private @Getter @Setter @Expose String southAmerica;
    private @Getter @Setter @Expose String westNorthAmerica;
    private @Getter @Setter @Expose String favorite;

    private @Getter @Setter @Expose String noServers; // No servers available. Probably bug or some kind of maintenance.
    private @Getter @Setter @Expose String updateFooter;

    public String getTranslatedRegionName(LostArkRegion region) {
        switch (region) {
            case NORTH_AMERICA_WEST -> {
                return westNorthAmerica;
            }
            case NORTH_AMERICA_EAST -> {
                return eastNorthAmerica;
            }
            case EUROPE_CENTRAL -> {
                return centralEurope;
            }
            case SOUTH_AMERICA -> {
                return southAmerica;
            }
            case EUROPE_WEST -> {
                return europeWest;
            }
        }

        return "Unknown region";
    }

    public boolean is(String langCode) {
        return this.langCode.equalsIgnoreCase(langCode);
    }

    public boolean isValid() {
        return !Stream.of(langCode,
                          langName,
                          title,
                          currentPlayers,
                          online,
                          busy,
                          full,
                          maintenance,
                          centralEurope,
                          europeWest,
                          eastNorthAmerica,
                          southAmerica,
                          westNorthAmerica,
                          favorite,
                          noServers,
                          updateFooter
        ).allMatch(Objects::isNull);
    }

    @Override
    public String toString() {
        return "LanguagePack{" +
                "langCode='" + langCode + '\'' +
                ", langName='" + langName + '\'' +
                ", title='" + title + '\'' +
                ", currentPlayers='" + currentPlayers + '\'' +
                ", online='" + online + '\'' +
                ", busy='" + busy + '\'' +
                ", full='" + full + '\'' +
                ", maintenance='" + maintenance + '\'' +
                ", centralEurope='" + centralEurope + '\'' +
                ", europeWest='" + europeWest + '\'' +
                ", eastNorthAmerica='" + eastNorthAmerica + '\'' +
                ", southAmerica='" + southAmerica + '\'' +
                ", westNorthAmerica='" + westNorthAmerica + '\'' +
                ", favorite='" + favorite + '\'' +
                ", noServers='" + noServers + '\'' +
                ", updateFooter='" + updateFooter + '\'' +
                '}';
    }
}
