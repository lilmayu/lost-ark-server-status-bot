package dev.mayuna.lostarkbot.console.commands;

import dev.mayuna.lostarkbot.console.commands.generic.AbstractConsoleCommand;
import dev.mayuna.lostarkbot.managers.LanguageManager;
import dev.mayuna.lostarkbot.objects.LanguagePack;
import dev.mayuna.lostarkbot.util.logging.Logger;

public class LangConsoleCommand extends AbstractConsoleCommand {

    public LangConsoleCommand() {
        this.name = "lang";
    }

    @Override
    public void execute(String arguments) {
        String[] args = arguments.split(" ");

        if (args.length == 0) {
            Logger.error("Invalid syntax! lang <list|load|view> [langCode]");
            return;
        }

        switch (args[0]) {
            case "list" -> {
                Logger.info("=== Loaded Languages - " + LanguageManager.getLoadedLanguages().size() + " ===");
                for (LanguagePack languagePack : LanguageManager.getLoadedLanguages()) {
                    Logger.info("[" + languagePack.getLangCode() + "]: " + languagePack.getLangName());
                }
            }
            case "load" -> {
                LanguageManager.load();
            }
            case "view" -> {
                if (args.length < 2) {
                    Logger.error("Invalid syntax! lang view <langCode>");
                    return;
                }

                String langCode = args[1];
                Logger.info("[" + langCode + "]: " + LanguageManager.getLanguageByCode(langCode));
            }
            default -> {
                Logger.error("Invalid syntax! lang <list|load|view> [langCode]");
            }
        }
    }
}
