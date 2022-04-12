package dev.mayuna.lostarkbot.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.mayuna.lostarkbot.objects.core.LanguagePack;
import dev.mayuna.lostarkbot.util.Utils;
import dev.mayuna.lostarkbot.util.logging.Logger;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LanguageManager {

    public static final String LANG_FOLDER = "./lang/";

    private final static @Getter List<LanguagePack> loadedLanguages = new ArrayList<>();

    /**
     * Gets {@link LanguagePack} by its lang code
     *
     * @param langCode Lang code
     *
     * @return Nullable {@link LanguagePack} (null, if no language has specified lang code)
     */
    public static LanguagePack getLanguageByCode(String langCode) {
        for (LanguagePack languagePack : loadedLanguages) {
            if (languagePack.is(langCode)) {
                return languagePack;
            }
        }

        return null;
    }

    /**
     * Loads all languages from data storage
     */
    public static void load() {
        Logger.info("Loading languages...");
        loadedLanguages.clear();

        File langFolder = new File(LANG_FOLDER);
        if (!langFolder.exists()) {
            if (!langFolder.mkdirs()) {
                Logger.error("Could not create lang folder!");
                return;
            }
        }

        File[] langFiles = langFolder.listFiles();

        if (langFiles == null) {
            Logger.error("Unable to list files in lang folder!");
            return;
        }

        Gson gson = Utils.getGson();

        for (File langFile : langFiles) {
            try {
                MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(langFile);
                JsonObject jsonObject = mayuJson.getJsonObject();

                LanguagePack languagePack = gson.fromJson(jsonObject, LanguagePack.class);

                if (languagePack.isValid()) {
                    Logger.debug("Loaded " + languagePack.getLangName() + " (" + languagePack.getLangCode() + ") language");
                    loadedLanguages.add(languagePack);
                } else {
                    Logger.error("Language file " + langFile.getName() + " has empty fields! " + languagePack);
                }
            } catch (Exception exception) {
                Logger.throwing(exception);
                Logger.error("Could not load lang file: " + langFile.getName());
            }
        }

        Logger.success("Loaded " + loadedLanguages.size() + " languages!");
    }

    /**
     * Gets default language pack (en) even if it does not exist
     *
     * @return Not-null {@link LanguagePack}
     */
    public static LanguagePack getDefaultLanguage() {
        LanguagePack languagePack = new LanguagePack();

        languagePack.setLangCode("en");
        languagePack.setLangName("English");
        languagePack.setTitle("Lost Ark - Server Dashboard");
        languagePack.setCurrentPlayers("Currently **{players}** in-game players");
        languagePack.setOnline("Online");
        languagePack.setBusy("Busy");
        languagePack.setFull("Full");
        languagePack.setMaintenance("Maintenance");
        languagePack.setCentralEurope("Central Europe");
        languagePack.setEuropeWest("Europe West");
        languagePack.setEastNorthAmerica("East North America");
        languagePack.setSouthAmerica("South America");
        languagePack.setWestNorthAmerica("West North America");
        languagePack.setFavorite("Favorite");
        languagePack.setNoServers("No servers available. Probably bug or some kind of maintenance.");
        languagePack.setNotFound("Not found");
        languagePack.setUpdateFooter("This message will be updated every 5 minutes");

        return languagePack;
    }
}
